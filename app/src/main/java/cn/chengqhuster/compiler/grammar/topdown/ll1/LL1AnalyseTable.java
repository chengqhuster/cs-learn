package cn.chengqhuster.compiler.grammar.topdown.ll1;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.SelectSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FollowSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.NullableSetUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LL1 语法分析表
 * 由相关文法生成
 */
public class LL1AnalyseTable {

    public static int STATE_ERROR = -1;

    private final ContextFreeGrammar cfg;

    // x 方向为终结符编号，y 方向为非终结符编号，数组的元素为产生式的编号
    private final int[][] table;

    public LL1AnalyseTable(ContextFreeGrammar cfg) {
        this.cfg = cfg;
        Set<String> nullableSet = NullableSetUtil.getNullable(cfg);
        Map<String, Set<String>> firstSet = FirstSetUtil.getFirstSet(cfg, nullableSet);
        Map<String, Set<String>> followSet = FollowSetUtil.getFollowSet(cfg, nullableSet, firstSet);
        Map<Integer, Set<String>> selectSet = SelectSetUtil.getSelectSetUtil(cfg, nullableSet, firstSet, followSet);

        // 由生成式的 FIRST 集生成分析表，终结符集增加终止符号
        cfg.terminators.add(ContextFreeGrammar.END_TERMINATOR);
        cfg.terminatorIndexMap.put(ContextFreeGrammar.END_TERMINATOR, cfg.terminators.size() - 1);
        this.table = new int[cfg.nonTerminators.size()][cfg.terminators.size()];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[0].length; j++) {
                this.table[i][j] = STATE_ERROR;
            }
        }
        for (Map.Entry<Integer, Set<String>> entry : selectSet.entrySet()) {
            int x = cfg.nonTerminatorIndexMap.get(cfg.productions.get(entry.getKey()).nonTerminator);
            for (String terminator : entry.getValue()) {
                int y = cfg.terminatorIndexMap.get(terminator);
                if (this.table[x][y] != STATE_ERROR && this.table[x][y] != entry.getKey()) {
                    throw new RuntimeException("文法冲突，分析文法不符合 LL1 规范");
                }
                this.table[x][y] = entry.getKey();
            }
        }
    }

    public boolean grammarCheck(List<String> tokens) {
        List<String> analyseTokens = new ArrayList<>(tokens);
        analyseTokens.add(ContextFreeGrammar.END_TERMINATOR);
        // token 检测
        if (analyseTokens.stream().anyMatch(it -> !this.cfg.terminatorIndexMap.containsKey(it)
                        && !this.cfg.nonTerminatorIndexMap.containsKey(it))) {
            return false;
        }

        LinkedList<String> stack = new LinkedList<>();
        stack.push(ContextFreeGrammar.END_TERMINATOR);
        stack.push(this.cfg.start);
        int pos = 0;
        while (!ContextFreeGrammar.END_TERMINATOR.equals(stack.peek())) {
            if (analyseTokens.get(pos).equals(stack.peek())) {
                // token 匹配，一定是终结符
                pos += 1;
                stack.pop();
            } else if (this.cfg.terminatorIndexMap.containsKey(stack.peek())) {
                // 终结符未匹配
                return false;
            } else {
                int state = this.table[this.cfg.nonTerminatorIndexMap.get(stack.peek())]
                        [this.cfg.terminatorIndexMap.get(analyseTokens.get(pos))];
                if (state == -1) {
                    // 没有合法的跳转状态（对应产生式）
                    return false;
                } else {
                    // 有对应的产生式，逆序压入产生式的右部
                    stack.pop();
                    for (int i = this.cfg.productions.get(state).symbols.size() - 1; i >= 0 ; i--) {
                        stack.push(this.cfg.productions.get(state).symbols.get(i));
                    }
                }
            }
        }
        // 栈顶为终止符，匹配成功
        return true;
    }
}
