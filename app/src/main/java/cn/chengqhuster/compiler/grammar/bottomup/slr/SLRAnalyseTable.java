package cn.chengqhuster.compiler.grammar.bottomup.slr;

import cn.chengqhuster.compiler.grammar.bottomup.data.ItemGroup;
import cn.chengqhuster.compiler.grammar.bottomup.utils.AugmentedGrammarUtil;
import cn.chengqhuster.compiler.grammar.bottomup.utils.ItemClosureUtil;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FollowSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.NullableSetUtil;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SLRAnalyseTable {

    public static final int STATE_ERROR = -1;
    public static final int TYPE_SHIFT = 1;
    public static final int TYPE_REDUCE = 2;

    private ContextFreeGrammar cfg;

    // x 方向为状态（项集），y 方向为终结符编号，数组的元素执行的动作，可以是移入，也可是规约，所以用一个一位数组表示
    private int[][][] actionTable;

    // x 方向为状态（项集），y 方向为非终结符编号，数组的元素跳转的下一个状态
    private int[][] gotoTable;

    /**
     * 上下文无关文法转换为 SLR 分析表
     * @param cfg
     */
    public SLRAnalyseTable(ContextFreeGrammar cfg) {
        // 转换为增广文法
        ContextFreeGrammar augmentedCfg = AugmentedGrammarUtil.augmentedGrammar(cfg);
        this.cfg = augmentedCfg;
        // 获取 FOLLOW 集，用于指导生成分析表
        Set<String> nullableSet = NullableSetUtil.getNullable(augmentedCfg);
        Map<String, Set<String>> firstSet = FirstSetUtil.getFirstSet(augmentedCfg, nullableSet);
        Map<String, Set<String>> followSet = FollowSetUtil.getFollowSet(augmentedCfg, nullableSet, firstSet);
        // 工作表的思想构建 SLA 的 DFA，每个状态都是项集，项集包含内核项和非内核项
        Map<Integer, int[][]> actionItemMap = new HashMap<>();
        Map<Integer, int[]> gotoItemMap = new HashMap<>();
        List<Pair<Integer, Integer>> initCoreItems = Collections.singletonList(new Pair<>(0, 0));
        ItemGroup initGroup = new ItemGroup(initCoreItems, ItemClosureUtil.getNonCoreItem(augmentedCfg, initCoreItems));
        // 状态（项集）信息
        List<ItemGroup> itemGroups = new ArrayList<>();
        Map<String, Integer> coreTag2Num = new HashMap<>();
        coreTag2Num.put(initGroup.getCoreTag(), 0);
        itemGroups.add(initGroup);
        // 工作表算法
        Set<String> visited = new HashSet<>();
        LinkedList<ItemGroup> queue = new LinkedList<>();
        queue.offer(initGroup);

        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                ItemGroup itemGroup = queue.poll();
                if (!visited.contains(itemGroup.getCoreTag())) {
                    visited.add(itemGroup.getCoreTag());
                    int[][] actionItem = getInitActionItem(augmentedCfg);
                    int[] gotoItem = getInitGotoItem(augmentedCfg);
                    // 计算规约
                    for (Pair<Integer, Integer> coreItem : itemGroup.coreItems) {
                        ContextFreeGrammar.Production p = augmentedCfg.productions.get(coreItem.getKey());
                        if (p.symbols.size() == coreItem.getValue()) {
                            // 根据 FOLLOW 集进行规约
                            for (String terminator : followSet.get(p.nonTerminator)) {
                                emptyActionStateCheck(actionItem[augmentedCfg.terminatorIndexMap.get(terminator)]);
                                actionItem[augmentedCfg.terminatorIndexMap.get(terminator)] = new int[]{TYPE_REDUCE, coreItem.getKey()};
                            }
                        }
                    }

                    Map<String, List<Pair<Integer, Integer>>> nextCoreItems = getNextCoreItems(augmentedCfg, itemGroup);
                    for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : nextCoreItems.entrySet()) {
                        ItemGroup nextGroup = new ItemGroup(entry.getValue(),
                                ItemClosureUtil.getNonCoreItem(augmentedCfg, entry.getValue()));
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

    public boolean grammarCheck(List<String> tokens) {
        List<String> analyseTokens = new ArrayList<>(tokens);
        analyseTokens.add(ContextFreeGrammar.END_TERMINATOR);
        // token 检测
        if (!analyseTokens.stream().allMatch(it -> this.cfg.terminatorIndexMap.containsKey(it)
                || this.cfg.nonTerminatorIndexMap.containsKey(it))) {
            return false;
        }

        // 栈上面只保存状态信息
        LinkedList<Integer> stack = new LinkedList<>();
        stack.push(0);
        int pos = 0;
        while (true) {
            String terminal = analyseTokens.get(pos);
            int[] action = this.actionTable[stack.peek()][this.cfg.terminatorIndexMap.get(terminal)];
            if (action[0] == TYPE_SHIFT) {
                // SHIFT
                pos += 1;
                stack.push(action[1]);
            } else if (action[0] == TYPE_REDUCE) {
                // 接受状态
                if (action[1] == 0) {
                    return true;
                }
                ContextFreeGrammar.Production p = cfg.productions.get(action[1]);
                // REDUCE
                for (int i = 0; i < p.symbols.size(); i++) {
                    stack.pop();
                }
                // GOTO
                int gotoState = this.gotoTable[stack.peek()][cfg.nonTerminatorIndexMap.get(p.nonTerminator)];
                if (gotoState == STATE_ERROR) {
                    return false;
                }
                stack.push(gotoState);
            } else {
                return false;
            }
        }
    }

    private void emptyActionStateCheck(int[] action) {
        if (action != null && action[0] != STATE_ERROR) {
            throw new RuntimeException("文法冲突，分析文法不符合 SLR 规范");
        }
    }

    private void emptyGotoStateCheck(int state) {
        if (state != STATE_ERROR) {
            throw new RuntimeException("文法冲突，分析文法不符合 SLR 规范");
        }
    }

    private int[][] getInitActionItem(ContextFreeGrammar cfg) {
        int[][] res = new int[cfg.terminators.size()][2];
        for (int i = 0; i < res.length; i++) {
            res[i][0] = STATE_ERROR;
        }
        return res;
    }

    private int[] getInitGotoItem(ContextFreeGrammar cfg) {
        int[] res = new int[cfg.nonTerminatorIndexMap.size()];
        Arrays.fill(res, STATE_ERROR);
        return res;
    }

    private Map<String, List<Pair<Integer, Integer>>> getNextCoreItems(ContextFreeGrammar cfg, ItemGroup itemGroup) {
        Map<String, List<Pair<Integer, Integer>>> res = new HashMap<>();
        List<Pair<Integer, Integer>> items = new ArrayList<>(itemGroup.coreItems);
        items.addAll(itemGroup.nonCoreItems);
        for (Pair<Integer, Integer> item : items) {
            ContextFreeGrammar.Production p = cfg.productions.get(item.getKey());
            if (item.getValue() < p.symbols.size()) {
                String symbol = p.symbols.get(item.getValue());
                if (!res.containsKey(symbol)) {
                    res.put(symbol, new ArrayList<>());
                }
                res.get(symbol).add(new Pair<>(item.getKey(), item.getValue() + 1));
            }
        }
        return res;
    }
}
