package cn.chengqhuster.compiler.grammar.bottomup.lr1;

import cn.chengqhuster.compiler.grammar.GrammarUtil;
import cn.chengqhuster.compiler.grammar.bottomup.LRAnalyseTable;
import cn.chengqhuster.compiler.grammar.bottomup.data.LR1ItemGroup;
import cn.chengqhuster.compiler.grammar.bottomup.utils.LR1ItemClosureUtil;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.NullableSetUtil;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.chengqhuster.compiler.grammar.GrammarUtil.END_TERMINATOR;

public class LR1AnalyseTable extends LRAnalyseTable {

    /**
     * 上下文无关文法转换为 LR1 分析表
     * 和 SLR 的过程几乎相同，区别是 CLOSURE 的计算以及规约的条件
     * @param cfg
     */
    public LR1AnalyseTable(ContextFreeGrammar cfg) {
        // 转换为增广文法
        ContextFreeGrammar augmentedCfg = GrammarUtil.augmentedGrammar(cfg);
        GrammarUtil.endGrammar(augmentedCfg);
        this.cfg = augmentedCfg;
        // 获取 FIRST 集，用于指导生成分析表
        Set<String> nullableSet = NullableSetUtil.getNullable(augmentedCfg);
        Map<String, Set<String>> firstSet = FirstSetUtil.getFirstSet(augmentedCfg, nullableSet);
        // 终结符集增加终止符号
        // 工作表的思想构建 LR1 的 DFA，每个状态都是项集，项集包含内核项和非内核项
        Map<Integer, int[][]> actionItemMap = new HashMap<>();
        Map<Integer, int[]> gotoItemMap = new HashMap<>();
        List<Triple<Integer, Integer, String>> initCoreItems = Collections.singletonList(new ImmutableTriple<>(0, 0, END_TERMINATOR));
        LR1ItemGroup initGroup = new LR1ItemGroup(initCoreItems, LR1ItemClosureUtil.getNonCoreItem(augmentedCfg, nullableSet, firstSet, initCoreItems));
        // 状态（项集）信息
        List<LR1ItemGroup> itemGroups = new ArrayList<>();
        Map<String, Integer> coreTag2Num = new HashMap<>();
        coreTag2Num.put(initGroup.getCoreTag(), 0);
        itemGroups.add(initGroup);
        // 工作表算法
        Set<String> visited = new HashSet<>();
        LinkedList<LR1ItemGroup> queue = new LinkedList<>();
        queue.offer(initGroup);

        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                LR1ItemGroup itemGroup = queue.poll();
                if (!visited.contains(itemGroup.getCoreTag())) {
                    visited.add(itemGroup.getCoreTag());
                    int[][] actionItem = getInitActionItem(augmentedCfg);
                    int[] gotoItem = getInitGotoItem(augmentedCfg);
                    // 计算规约
                    for (Triple<Integer, Integer, String> coreItem : itemGroup.coreItems) {
                        ContextFreeGrammar.Production p = augmentedCfg.productions.get(coreItem.getLeft());
                        if (p.symbols.size() == coreItem.getMiddle()) {
                            // 根据终止符进行规约
                            emptyActionStateCheck(actionItem[augmentedCfg.terminatorIndexMap.get(coreItem.getRight())]);
                            actionItem[augmentedCfg.terminatorIndexMap.get(coreItem.getRight())] = new int[]{TYPE_REDUCE, coreItem.getLeft()};
                        }
                    }

                    Map<String, List<Triple<Integer, Integer, String>>> nextCoreItems = getNextCoreItems(augmentedCfg, itemGroup);
                    for (Map.Entry<String, List<Triple<Integer, Integer, String>>> entry : nextCoreItems.entrySet()) {
                        LR1ItemGroup nextGroup = new LR1ItemGroup(entry.getValue(),
                                LR1ItemClosureUtil.getNonCoreItem(augmentedCfg, nullableSet, firstSet, entry.getValue()));
                        if (!coreTag2Num.containsKey(nextGroup.getCoreTag())) {
                            coreTag2Num.put(nextGroup.getCoreTag(), itemGroups.size());
                            itemGroups.add(nextGroup);
                        }
                        queue.offer(nextGroup);
                        // GOTO 表
                        if (augmentedCfg.nonTerminatorIndexMap.containsKey(entry.getKey())) {
                            emptyGotoStateCheck(gotoItem[augmentedCfg.nonTerminatorIndexMap.get(entry.getKey())]);
                            gotoItem[augmentedCfg.nonTerminatorIndexMap.get(entry.getKey())] = coreTag2Num.get(nextGroup.getCoreTag());
                        }
                        // ACTION 表
                        if (augmentedCfg.terminatorIndexMap.containsKey(entry.getKey())) {
                            emptyActionStateCheck(actionItem[augmentedCfg.terminatorIndexMap.get(entry.getKey())]);
                            actionItem[augmentedCfg.terminatorIndexMap.get(entry.getKey())] =
                                    new int[]{TYPE_SHIFT, coreTag2Num.get(nextGroup.getCoreTag())};
                        }
                    }
                    actionItemMap.put(coreTag2Num.get(itemGroup.getCoreTag()), actionItem);
                    gotoItemMap.put(coreTag2Num.get(itemGroup.getCoreTag()), gotoItem);
                }
            }
        }
        this.actionTable = new int[actionItemMap.size()][][];
        this.gotoTable = new int[gotoItemMap.size()][];
        for (Map.Entry<Integer, int[][]> entry : actionItemMap.entrySet()) {
            actionTable[entry.getKey()] = entry.getValue();
        }
        for (Map.Entry<Integer, int[]> entry : gotoItemMap.entrySet()) {
            gotoTable[entry.getKey()] = entry.getValue();
        }
    }

    private Map<String, List<Triple<Integer, Integer, String>>> getNextCoreItems(ContextFreeGrammar cfg,
                                                                                 LR1ItemGroup itemGroup) {
        Map<String, List<Triple<Integer, Integer, String>>> res = new HashMap<>();
        List<Triple<Integer, Integer, String>> items = new ArrayList<>(itemGroup.coreItems);
        items.addAll(itemGroup.nonCoreItems);
        for (Triple<Integer, Integer, String> item : items) {
            ContextFreeGrammar.Production p = cfg.productions.get(item.getLeft());
            if (item.getMiddle() < p.symbols.size()) {
                String symbol = p.symbols.get(item.getMiddle());
                if (!res.containsKey(symbol)) {
                    res.put(symbol, new ArrayList<>());
                }
                res.get(symbol).add(new ImmutableTriple<>(item.getLeft(), item.getMiddle() + 1, item.getRight()));
            }
        }
        return res;
    }
}
