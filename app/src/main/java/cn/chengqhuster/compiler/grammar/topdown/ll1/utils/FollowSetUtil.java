package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.chengqhuster.compiler.grammar.GrammarUtil.END_TERMINATOR;

public class FollowSetUtil {

    /**
     * 在 NULLABLE 集、FIRST 集的基础上
     * 获取 FOLLOW 集（跟随集），不动点算法思想
     */
    public static Map<String, Set<String>> getFollowSet(ContextFreeGrammar cfg,
                                                        Set<String> nullableSet,
                                                        Map<String, Set<String>> firstSetMap) {

        Map<String, Set<String>> followSetMap = cfg.nonTerminators.stream()
                .collect(Collectors.toMap(Function.identity(), it -> new HashSet<>()));

        // 起始符号的 FOLLOW 集合一定包含终止符
        followSetMap.get(cfg.start).add(END_TERMINATOR);

        boolean flag = true;
        while (flag) {
            flag = false;
            for (ContextFreeGrammar.Production production : cfg.productions) {
                // FIX-BUG 必须新建集合，不能直接赋值引用
                Set<String> temp = new HashSet<>(followSetMap.get(production.nonTerminator));
                // 逆序遍历
                for (int i = production.symbols.size() - 1; i >= 0; i--) {
                    String symbol = production.symbols.get(i);
                    if (cfg.terminatorIndexMap.containsKey(symbol)) {
                        temp = new HashSet<>();
                        temp.add(symbol);
                    }
                    if (cfg.nonTerminatorIndexMap.containsKey(symbol)) {
                        flag = followSetMap.get(symbol).addAll(temp) || flag;
                        if (nullableSet.contains(symbol)) {
                            temp.addAll(firstSetMap.get(symbol));
                        } else {
                            temp = firstSetMap.get(symbol);
                        }
                    }
                }
            }
        }

        return followSetMap;
    }
}
