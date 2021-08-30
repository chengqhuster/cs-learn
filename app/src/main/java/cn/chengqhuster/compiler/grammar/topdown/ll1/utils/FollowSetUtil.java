package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        boolean flag = true;
        while (flag) {
            flag = false;
            for (ContextFreeGrammar.Production production : cfg.productions) {
                // 逆序遍历
                Set<String> temp = followSetMap.get(production.nonTerminator);
                for (int i = production.symbols.length - 1; i >= 0; i--) {
                    String symbol = production.symbols[i];
                    if (cfg.terminators.contains(symbol)) {
                        temp = new HashSet<>();
                        temp.add(symbol);
                    }
                    if (cfg.nonTerminators.contains(symbol)) {
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
