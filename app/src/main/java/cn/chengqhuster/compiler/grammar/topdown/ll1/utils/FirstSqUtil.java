package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.chengqhuster.compiler.grammar.GrammarUtil.END_TERMINATOR;

public class FirstSqUtil {

    /**
     * 返回给定串的 FIRST 集
     */
    public static Set<String> getFirstSet(List<String> symbols,
                            ContextFreeGrammar cfg,
                            Set<String> nullableSet,
                            Map<String, Set<String>> firstSetMap) {
        Set<String> firstSet = new HashSet<>();
        for (String symbol : symbols) {
            if (cfg.terminators.contains(symbol) || END_TERMINATOR.equals(symbol)) {
                firstSet.add(symbol);
                return firstSet;
            }
            if (cfg.nonTerminators.contains(symbol)) {
                firstSet.addAll(firstSetMap.get(symbol));
                if (!nullableSet.contains(symbol)) {
                    return firstSet;
                }
            }
        }
        return firstSet;
    }
}
