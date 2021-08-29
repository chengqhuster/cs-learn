package cn.chengqhuster.compiler.lexical;

import cn.chengqhuster.compiler.re.ReExp;
import cn.chengqhuster.compiler.re.ReUtils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Thompson 算法实现的 NFA
 *
 */
public class NFA implements FiniteAutomaton {

    /**
     * 初始状态
     */
    NFANode start;

    /**
     * 接受状态
     */
    NFANode end;

    /**
     * 有限状态自动机的状态转移可以看作是一个有向图
     *
     * 非确定有限状态自动机的转移条件可以为空字符, 或者转移的结果是一个状态集而不是确定的一个状态
     */
    static class NFANode {

        /**
         * 状态编号, 可 NFA 构造完成后再遍历编号
         */
        int value;

        /**
         * 转移条件, null 代表空串
         */
        Character c;

        /**
         * 转移状态节点
         * 除了接受状态都存在本节点
         */
        NFANode nodeA;

        /**
         * 转移条件为空串时可能存在本节点
         */
        NFANode nodeB;
    }

    private NFA(NFANode start, NFANode end) {
        this.start = start;
        this.end = end;
    }

    /**
     * NFA的匹配方法比较复杂, 先不实现
     * @param str
     * @return
     */
    @Override
    public boolean match(String str) {
        return false;
    }

    /**
     * Thompson 算法, 根据正则表达式 re 递归的生成 NFA
     *
     * 1. N(re) 的状态数最多为 re 中出现的运算符和运算分量的总数的两倍
     * 2. N(re) 有且仅有一个开始状态和接受状态
     * 3. N(re) 初接受状态外每个状态要么有一条标号为字符集中符号的出边, 或者有两条标号为空串的出边
     * @param re
     */
    public static NFA thompson(String re) {
        String filledRe = ReUtils.fillExplicitConcatOperator(re);
        String postFixRe = ReUtils.infixToPostfix(filledRe);

        LinkedList<NFA> stack = new LinkedList<>();
        for (int i = 0; i < postFixRe.length(); i++) {
            char c = postFixRe.charAt(i);
            if (c == ReExp.CONNECT_OPERATOR) {
                NFA a = stack.pop();
                NFA b = stack.pop();
                // b 先入栈, 放在前面
                stack.push(connect(b, a));
            } else if (c == ReExp.UNION_OPERATOR) {
                NFA a = stack.pop();
                NFA b = stack.pop();
                stack.push(union(b, a));
            } else if (c == ReExp.CLOSURE_OPERATOR) {
                NFA a = stack.pop();
                stack.push(closure(a));
            } else {
                stack.push(construct(c));
            }
        }

        NFA nfa = stack.pop();
        numberNfa(nfa);
        return nfa;
    }

    // 编号
    private static void numberNfa(NFA nfa) {
        int number = 1;
        Queue<NFANode> queue = new LinkedList<>();
        queue.add(nfa.start);
        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                NFA.NFANode node = queue.poll();
                if (node.value > 0) {
                    continue;
                }
                node.value = number;
                number++;
                if (node.nodeA != null && node.nodeA.value == 0) {
                    queue.offer(node.nodeA);
                }
                if (node.nodeB != null && node.nodeB.value == 0) {
                    queue.offer(node.nodeB);
                }
            }
        }
    }

    /**
     * 连接不生成中间状态节点, 只增加一条空串边
     * @param a
     * @param b
     * @return
     */
    private static NFA connect(NFA a, NFA b) {
        // 增加空串边, 编译原理 中科大 华保健
        a.end.nodeA = b.start;

        /*
         也可合并节点, 编译原理 第二版本 P01

         a.end.c = b.start.c;
         a.end.value = b.start.value;
         a.end.nodeA = b.start.nodeA;
         a.end.nodeB = b.start.nodeB;
        */

        return new NFA(a.start, b.end);
    }

    /**
     * 选择会增加两状态个节点
     * @param a
     * @param b
     * @return
     */
    private static NFA union(NFA a, NFA b) {
        NFA.NFANode start = new NFA.NFANode(), end = new NFA.NFANode();
        start.nodeA = a.start;
        start.nodeB = b.start;
        a.end.nodeA = end;
        b.end.nodeA = end;
        return new NFA(start, end);
    }

    /**
     * 闭包会增加两个状态节点
     * @param a
     * @return
     */
    private static NFA closure(NFA a) {
        NFA.NFANode start = new NFA.NFANode(), end = new NFA.NFANode();
        start.nodeA = a.start;
        start.nodeB = end;
        a.end.nodeA = a.start;
        a.end.nodeB = end;
        return new NFA(start, end);
    }

    /**
     * 单基础字符组装 nfa
     * @param c
     * @return
     */
    private static NFA construct(char c) {
        NFA.NFANode end = new NFA.NFANode();
        NFA.NFANode start = new NFA.NFANode();
        start.c = c;
        start.nodeA = end;
        return new NFA(start, end);
    }
}
