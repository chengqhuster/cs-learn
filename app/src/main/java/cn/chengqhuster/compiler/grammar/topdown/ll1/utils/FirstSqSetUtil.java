package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstSqSetUtil {

    /**
     * 在 NULLABLE 集、FIRST 集、FOLLOW 集的基础上
     * 获取 FIRST_SQ 集（推导式的 first 集），不动点算法思想
     */
    public static Map<Integer, Set<String>> getFirstSqSetUtil(ContextFreeGrammar cfg,
                                                             Set<String> nullableSet,
                                                             Map<String, Set<String>> firstSetMap,
                                                             Map<String, Set<String>> followSetMap) {

        Map<Integer, Set<String>> firstSqSetMap = new HashMap<>();
        List<ContextFreeGrammar.Production> productions = cfg.productions;
        for (int i = 0; i < productions.size(); i++) {
            Set<String> firstSqSet = new HashSet<>();
            boolean allNullable = true;
            for (String symbol : productions.get(i).symbols) {
                if (cfg.terminatorIndexMap.containsKey(symbol)) {
                    firstSqSet.add(symbol);
                    allNullable = false;
                    break;
                }
                if (cfg.nonTerminatorIndexMap.containsKey(symbol)) {
                    firstSqSet.addAll(firstSetMap.get(symbol));
                    if (!nullableSet.contains(symbol)) {
                        allNullable = false;
                        break;
                    }
                }
            }
            if (allNullable) {
                firstSqSet.addAll(followSetMap.get(productions.get(i).nonTerminator));
            }
            firstSqSetMap.put(i, firstSqSet);
        }

        return firstSqSetMap;
    }
}
