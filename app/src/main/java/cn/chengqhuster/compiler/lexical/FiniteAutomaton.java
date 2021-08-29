package cn.chengqhuster.compiler.lexical;

/**
 * 有限状态自动机, 接收一个字符串并判断是否匹配
 */
public interface FiniteAutomaton {

    boolean match(String str);
}
