package cn.chengqhuster.compiler.grammar.bottomup.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSqUtil;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LR1ItemClosureUtil {

    /**
     * 计算输入项集的闭包项，返回的项集不包含输入项
     * 不动点算法
     * @param cfg
     * @param coreItem
     * @return
     */
    public static List<Triple<Integer, Integer, String>> getNonCoreItem(ContextFreeGrammar cfg,
                                                                        Set<String> nullableSet,
                                                                        Map<String, Set<String>> firstSetMap,
                                                                        List<Triple<Integer, Integer, String>> coreItem) {
        Set<Triple<Integer, Integer, String>> items = new HashSet<>(coreItem);
        Set<Triple<Integer, Integer, String>> newItems = new HashSet<>();
        do {
            newItems.clear();
            for (Triple<Integer, Integer, String> item : items) {
                ContextFreeGrammar.Production p = cfg.productions.get(item.getLeft());
                if (p.symbols.size() > item.getMiddle() && cfg.nonTerminators.contains(p.symbols.get(item.getMiddle()))) {
                    List<String> symbols = new ArrayList<>(p.symbols.subList(item.getMiddle() + 1, p.symbols.size()));
                    symbols.add(item.getRight());
                    Set<String> firstSet = FirstSqUtil.getFirstSet(symbols, cfg, nullableSet, firstSetMap);
                    for (String first : firstSet) {
                        for (int i = 0; i < cfg.productions.size(); i++) {
                            ContextFreeGrammar.Production np = cfg.productions.get(i);
                            if (np.nonTerminator.equals(p.symbols.get(item.getMiddle()))) {
                                newItems.add(new ImmutableTriple<>(i, 0, first));
                            }
                        }
                    }
                }
            }
        } while (items.addAll(newItems));
        coreItem.forEach(items::remove);
        return new ArrayList<>(items);
    }
}
