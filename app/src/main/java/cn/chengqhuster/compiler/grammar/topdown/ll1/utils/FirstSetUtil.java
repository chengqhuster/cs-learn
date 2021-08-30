package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirstSetUtil {

    /**
     * 在 NULLABLE 集的基础上
     * 获取 FIRST 集，不动点算法思想
     */
    public static Map<String, Set<String>> getFirstSet(ContextFreeGrammar cfg, Set<String> nullableSet) {

        Map<String, Set<String>> firstSetMap = cfg.nonTerminators.stream()
                .collect(Collectors.toMap(Function.identity(), it -> new HashSet<>()));
        boolean flag = true;
        while (flag) {
            flag = false;
            for (ContextFreeGrammar.Production production : cfg.productions) {
                Set<String> firstSet = firstSetMap.get(production.nonTerminator);
                for (String symbol : production.symbols) {
                    if (cfg.terminators.contains(symbol)) {
                        // 注意 or 熔断
                        flag = firstSet.add(symbol) || flag;
                        break;
                    }
                    if (cfg.nonTerminators.contains(symbol)) {
                        flag = firstSet.addAll(firstSetMap.get(symbol)) || flag;
                        if (!nullableSet.contains(symbol)) {
                            break;
                        }
                    }
                }
            }
        }

        return firstSetMap;
    }
}
