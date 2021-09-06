package cn.chengqhuster.compiler.grammar.bottomup.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LR0ItemClosureUtil {

    /**
     * 计算输入项集的闭包项，返回的项集不包含输入项
     * 工作表算法
     * @param cfg
     * @param coreItem
     * @return
     */
    public static List<Pair<Integer, Integer>> getNonCoreItem(ContextFreeGrammar cfg,
                                                              List<Pair<Integer, Integer>> coreItem) {
        Set<String> visited = new HashSet<>();
        LinkedList<String> queue = new LinkedList<>(getNextNonTerminators(cfg, coreItem));
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                String nonTerminator = queue.poll();
                if (!visited.contains(nonTerminator)) {
                    visited.add(nonTerminator);
                    List<Pair<Integer, Integer>> items = new ArrayList<>();
                    for (int j = 0; j < cfg.productions.size(); j++) {
                        if (nonTerminator.equals(cfg.productions.get(j).nonTerminator)) {
                            items.add(new ImmutablePair<>(j , 0));
                        }
                    }
                    getNextNonTerminators(cfg, items).stream()
                            .filter(it -> !visited.contains(it))
                            .forEach(queue::offer);
                    res.addAll(items);
                }
            }
        }
        return res;
    }

    private static Set<String> getNextNonTerminators(ContextFreeGrammar cfg,
                                                      List<Pair<Integer, Integer>> items) {
        Set<String> nonTerminators = new HashSet<>();
        for (Pair<Integer, Integer> pair : items) {
            List<String> symbols = cfg.productions.get(pair.getLeft()).symbols;
            if (symbols.size() > pair.getRight() && cfg.nonTerminatorIndexMap.containsKey(symbols.get(pair.getRight()))) {
                nonTerminators.add(symbols.get(pair.getRight()));
            }
        }
        return nonTerminators;
    }
}
