package cn.chengqhuster.compiler.grammar.topdown.ll1;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Set;

public class LL1CFG {

    /**
     * 加法和乘法组成的表达式，用 LL1 文法表示 (无二义性、无 LL1 文法冲突)
     *
     * E  -> T E'
     * E' -> + T E'
     *     |
     * T  -> F T'
     * T' -> * F T'
     *     |
     * F -> num
     */
    public static ContextFreeGrammar PLUS_MULTIPLY_EP_CFG = new ContextFreeGrammar();

    static {
        Set<String> terminators = new HashSet<>();
        terminators.add("num");
        terminators.add("*");
        terminators.add("+");
        PLUS_MULTIPLY_EP_CFG.terminators = terminators;
        Set<String> nonTerminators = new HashSet<>();
        nonTerminators.add("E");
        nonTerminators.add("E'");
        nonTerminators.add("T");
        nonTerminators.add("T'");
        nonTerminators.add("F");
        PLUS_MULTIPLY_EP_CFG.nonTerminators = nonTerminators;
        PLUS_MULTIPLY_EP_CFG.start = "E";
        PLUS_MULTIPLY_EP_CFG.productions = new ContextFreeGrammar.Production[] {
                new ContextFreeGrammar.Production("E", new String[]{"T", "E'"}),
                new ContextFreeGrammar.Production("E'", new String[]{"+", "T", "E'"}),
                new ContextFreeGrammar.Production("E'", new String[]{}),
                new ContextFreeGrammar.Production("T", new String[]{"F", "T'"}),
                new ContextFreeGrammar.Production("T'", new String[]{"*", "F", "T''"}),
                new ContextFreeGrammar.Production("T'", new String[]{}),
                new ContextFreeGrammar.Production("F", new String[]{"num"}),
        };
    }
}
