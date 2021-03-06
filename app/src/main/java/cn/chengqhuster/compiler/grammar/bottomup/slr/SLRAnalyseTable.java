package cn.chengqhuster.compiler.grammar.bottomup.slr;

import cn.chengqhuster.compiler.grammar.GrammarUtil;
import cn.chengqhuster.compiler.grammar.bottomup.LRAnalyseTable;
import cn.chengqhuster.compiler.grammar.bottomup.data.LR0ItemGroup;
import cn.chengqhuster.compiler.grammar.bottomup.utils.LR0ItemClosureUtil;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FollowSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.NullableSetUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SLRAnalyseTable extends LRAnalyseTable {

    /**
     * 上下文无关文法转换为 SLR 分析表
     * @param cfg
     */
    public SLRAnalyseTable(ContextFreeGrammar cfg) {
        // 转换为增广文法
        ContextFreeGrammar augmentedCfg = GrammarUtil.augmentedGrammar(cfg);
        GrammarUtil.endGrammar(augmentedCfg);
        this.cfg = augmentedCfg;
        // 获取 FOLLOW 集，用于指导生成分析表
        Set<String> nullableSet = NullableSetUtil.getNullable(augmentedCfg);
        Map<String, Set<String>> firstSet = FirstSetUtil.getFirstSet(augmentedCfg, nullableSet);
        Map<String, Set<String>> followSet = FollowSetUtil.getFollowSet(augmentedCfg, nullableSet, firstSet);
        // 工作表的思想构建 SLA 的 DFA，每个状态都是项集，项集包含内核项和非内核项
        Map<Integer, int[][]> actionItemMap = new HashMap<>();
        Map<Integer, int[]> gotoItemMap = new HashMap<>();
        List<Pair<Integer, Integer>> initCoreItems = Collections.singletonList(new ImmutablePair<>(0, 0));
        LR0ItemGroup initGroup = new LR0ItemGroup(initCoreItems, LR0ItemClosureUtil
                .getNonCoreItem(augmentedCfg, initCoreItems));
        // 状态（项集）信息
        List<LR0ItemGroup> itemGroups = new ArrayList<>();
        Map<String, Integer> coreTag2Num = new HashMap<>();
        coreTag2Num.put(initGroup.getCoreTag(), 0);
        itemGroups.add(initGroup);
        // 工作表算法
        Set<String> visited = new HashSet<>();
        LinkedList<LR0ItemGroup> queue = new LinkedList<>();
        queue.offer(initGroup);

        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                LR0ItemGroup itemGroup = queue.poll();
                if (!visited.contains(itemGroup.getCoreTag())) {
                    visited.add(itemGroup.getCoreTag());
                    int[][] actionItem = getInitActionItem(augmentedCfg);
                    int[] gotoItem = getInitGotoItem(augmentedCfg);
                    // 计算规约
                    for (Pair<Integer, Integer> coreItem : itemGroup.coreItems) {
                        ContextFreeGrammar.Production p = augmentedCfg.productions.get(coreItem.getLeft());
                        if (p.symbols.size() == coreItem.getRight()) {
                            // 根据 FOLLOW 集进行规约
                            for (String terminator : followSet.get(p.nonTerminator)) {
                                emptyActionStateCheck(actionItem[augmentedCfg.terminatorIndexMap.get(terminator)]);
                                actionItem[augmentedCfg.terminatorIndexMap.get(terminator)] = new int[]{TYPE_REDUCE, coreItem.getLeft()};
                            }
                        }
                    }

                    Map<String, List<Pair<Integer, Integer>>> nextCoreItems = getNextCoreItems(augmentedCfg, itemGroup);
                    for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : nextCoreItems.entrySet()) {
                        LR0ItemGroup nextGroup = new LR0ItemGroup(entry.getValue(),
                                LR0ItemClosureUtil.getNonCoreItem(augmentedCfg, entry.getValue()));
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
                            actionItem[augmentedCfg.terminatorIndexMap.get(entry.getKey())] = new int[]{TYPE_SHIFT,
                                    coreTag2Num.get(nextGroup.getCoreTag())};
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

    private Map<String, List<Pair<Integer, Integer>>> getNextCoreItems(ContextFreeGrammar cfg, LR0ItemGroup itemGroup) {
        Map<String, List<Pair<Integer, Integer>>> res = new HashMap<>();
        List<Pair<Integer, Integer>> items = new ArrayList<>(itemGroup.coreItems);
        items.addAll(itemGroup.nonCoreItems);
        for (Pair<Integer, Integer> item : items) {
            ContextFreeGrammar.Production p = cfg.productions.get(item.getLeft());
            if (item.getRight() < p.symbols.size()) {
                String symbol = p.symbols.get(item.getRight());
                if (!res.containsKey(symbol)) {
                    res.put(symbol, new ArrayList<>());
                }
                res.get(symbol).add(new ImmutablePair<>(item.getLeft(), item.getRight() + 1));
            }
        }
        return res;
    }
}
