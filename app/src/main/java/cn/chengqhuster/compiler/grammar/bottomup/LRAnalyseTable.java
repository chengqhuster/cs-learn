package cn.chengqhuster.compiler.grammar.bottomup;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static cn.chengqhuster.compiler.grammar.GrammarUtil.END_TERMINATOR;

public abstract class LRAnalyseTable {

    public static final int STATE_ERROR = -1;
    public static final int TYPE_SHIFT = 1;
    public static final int TYPE_REDUCE = 2;

    protected ContextFreeGrammar cfg;

    // x 方向为状态（项集），y 方向为终结符编号，数组的元素执行的动作，可以是移入，也可是规约，所以用一个一位数组表示
    protected int[][][] actionTable;

    // x 方向为状态（项集），y 方向为非终结符编号，数组的元素跳转的下一个状态
    protected int[][] gotoTable;

    public boolean grammarCheck(List<String> tokens) {
        List<String> analyseTokens = new ArrayList<>(tokens);
        analyseTokens.add(END_TERMINATOR);
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

    protected void emptyActionStateCheck(int[] action) {
        if (action != null && action[0] != STATE_ERROR) {
            throw new RuntimeException("文法冲突，分析文法不符合 LR1 规范");
        }
    }

    protected void emptyGotoStateCheck(int state) {
        if (state != STATE_ERROR) {
            throw new RuntimeException("文法冲突，分析文法不符合 LR1 规范");
        }
    }

    protected int[][] getInitActionItem(ContextFreeGrammar cfg) {
        int[][] res = new int[cfg.terminators.size()][2];
        for (int i = 0; i < res.length; i++) {
            res[i][0] = STATE_ERROR;
        }
        return res;
    }

    protected int[] getInitGotoItem(ContextFreeGrammar cfg) {
        int[] res = new int[cfg.nonTerminatorIndexMap.size()];
        Arrays.fill(res, STATE_ERROR);
        return res;
    }
}
