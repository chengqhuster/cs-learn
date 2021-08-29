package cn.chengqhuster.compiler.re;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * 处理正则表达式的工具类
 * 只支持最基础的正则形式 (选择、连接和闭包), 没有语法糖, 字符集属于 ascii 码,
 */
public class ReUtils {

    /**
     * 填充隐式的连接符号 · , 填充规则如下, y轴是左边符号, x轴是右边符号
     * l   l  *  l  (  l  )  l  |  l  a  l
     * -----------------------------------
     * l * l  \  l  ·  l  \  l  \  l  ·  l
     * -----------------------------------
     * l ( l  \  l  \  l  \  l  \  l  \  l
     * -----------------------------------
     * l ) l  \  l  ·  l  \  l  \  l  ·  l
     * -----------------------------------
     * l | l  \  l  \  l  \  l  \  l  \  l
     * -----------------------------------
     * l a l  \  l  ·  l  \  l  \  l  ·  l
     * -----------------------------------
     *
     * @param re
     * @return
     */
    public static String fillExplicitConcatOperator(String re) {
        // 填充后长度不超过原来的两倍
        char[] cs = new char[re.length() * 2];
        int pos = 0;
        for (int i = 0; i < re.length(); i++) {
            char a = re.charAt(i);
            cs[pos++] = a;
            if (a == ReExp.GROUP_LEFT_OPERATOR || a == ReExp.UNION_OPERATOR) {
                continue;
            }
            if (i + 1 < re.length()) {
                char b = re.charAt(i + 1);
                if (b != ReExp.CLOSURE_OPERATOR && b != ReExp.UNION_OPERATOR && b != ReExp.GROUP_RIGHT_OPERATOR) {
                    cs[pos++] = ReExp.CONNECT_OPERATOR;
                }
            }
        }
        return new String(Arrays.copyOf(cs, pos));
    }

    /**
     * 返回后缀表达式 (中缀表达式不利于分析运算符的优先级)
     * 正则运算符优先级从小到大顺序为 CONNECT CLOSURE UNION
     * 表达式里面括号的优先级最高, 并且支持嵌套
     *
     * 这里使用了栈的结构, 具体过程如下:
     * 1. 遇到普通字符, 直接输出
     * 2. 遇到左括号, 直接入栈
     * 3. 遇到右括号, 将栈元素弹出直到遇见左括号为止, 左括号弹出不输出
     * 4. 遇到限定符, 弹出优先级大于或等于该限定符号的栈顶元素, 将该限定符入栈
     * 5. 遇到字串结束, 将栈元素全部弹出
     * @param filledRe 填充了连接符号的正则表达式
     * @return
     */
    public static String infixToPostfix(String filledRe) {
        char[] cs = new char[filledRe.length()];
        int pos = 0;
        LinkedList<Character> stack = new LinkedList<>();
        for (int i = 0; i < filledRe.length(); i++) {
            char c = filledRe.charAt(i);
            if (c == ReExp.GROUP_LEFT_OPERATOR) {
                stack.push(c);
            } else if (c == ReExp.GROUP_RIGHT_OPERATOR) {
                while (stack.peek() != ReExp.GROUP_LEFT_OPERATOR) {
                    cs[pos++] = stack.pop();
                }
                stack.pop();
            } else if (c == ReExp.CLOSURE_OPERATOR) {
                // 右单目运算符直接入存储表
                cs[pos++] = c;
            } else if (ReExp.OPERATOR_PRIORITY.containsKey(c)) {
                while (ReExp.OPERATOR_PRIORITY.containsKey(stack.peek())
                        && ReExp.OPERATOR_PRIORITY.get(stack.peek()) > ReExp.OPERATOR_PRIORITY.get(c)) {
                    cs[pos++] = stack.pop();
                }
                stack.push(c);
            } else {
                cs[pos++] = c;
            }
        }
        while (!stack.isEmpty()) {
            cs[pos++] = stack.pop();
        }
        return new String(Arrays.copyOf(cs, pos));
    }
}
