package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirstSqSetUtil {

    /**
     * 在 NULLABLE 集、FIRST 集、FOLLOW 集的基础上
     * 获取 FIRST_SQ 集（推导式的 first 集），不动点算法思想
     */
    public static Map<String, Set<String>> getFirstSqSetUtil(ContextFreeGrammar cfg,
                                                             Set<String> nullableSet,
                                                             Map<String, Set<String>> firstSetMap,
                                                             Map<String, Set<String>> followSetMap) {

        Map<String, Set<String>> firstSqSetMap = Arrays.stream(cfg.productions)
                .map(ContextFreeGrammar.Production::toTag)
                .collect(Collectors.toMap(Function.identity(), it -> new HashSet<>()));
        for (ContextFreeGrammar.Production production : cfg.productions) {
            Set<String> firstSqSet = firstSqSetMap.get(production.toTag());
            boolean allNullable = true;
            for (String symbol : production.symbols) {
                if (cfg.terminators.contains(symbol)) {
                    firstSqSet.add(symbol);
                    allNullable = false;
                    break;
                }
                if (cfg.nonTerminators.contains(symbol)) {
                    firstSqSet.addAll(firstSetMap.get(symbol));
                    if (!nullableSet.contains(symbol)) {
                        allNullable = false;
                        break;
                    }
                }
            }
            if (allNullable) {
                firstSqSet.addAll(followSetMap.get(production.nonTerminator));
            }
        }

        return firstSqSetMap;
    }
}
