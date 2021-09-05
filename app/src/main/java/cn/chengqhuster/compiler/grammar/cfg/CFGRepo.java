package cn.chengqhuster.compiler.grammar.cfg;

import java.util.Arrays;
import java.util.Collections;

/**
 * 用于分析的文法
 */
public class CFGRepo {

    /**
     * 加法和乘法组成的表达式，存在左递归，适用于 LR 分析
     *
     * E  -> E + T
     *     | T
     * T  -> T * F
     *     | F
     * F  -> ( E )
     *     | id
     */
    public static ContextFreeGrammar PLUS_MULTIPLY_EP1_CFG = new ContextFreeGrammar();

    static {
        PLUS_MULTIPLY_EP1_CFG.putTerminators(Arrays.asList("id", "*", "+", "(", ")"));
        PLUS_MULTIPLY_EP1_CFG.putNonTerminators(Arrays.asList("E", "T", "F"));
        PLUS_MULTIPLY_EP1_CFG.start = "E";
        PLUS_MULTIPLY_EP1_CFG.productions = Arrays.asList(
                new ContextFreeGrammar.Production("E", Arrays.asList("E", "+", "T")),
                new ContextFreeGrammar.Production("E", Collections.singletonList("T")),
                new ContextFreeGrammar.Production("T", Arrays.asList("T", "*", "F")),
                new ContextFreeGrammar.Production("T", Collections.singletonList("F")),
                new ContextFreeGrammar.Production("F", Arrays.asList("(", "E", ")")),
                new ContextFreeGrammar.Production("F", Collections.singletonList("id"))
        );
    }

    /**
     * 加法和乘法组成的表达式，适用于 LL1 分析 (无二义性、消除了 EP1 的左递归、无 LL1 文法冲突)
     *
     * E  -> T E'
     * E' -> + T E'
     *     |
     * T  -> F T'
     * T' -> * F T'
     *     |
     * F -> id
     */
    public static ContextFreeGrammar PLUS_MULTIPLY_EP2_CFG = new ContextFreeGrammar();

    static {
        PLUS_MULTIPLY_EP2_CFG.putTerminators(Arrays.asList("id", "*", "+", "(", ")"));
        PLUS_MULTIPLY_EP2_CFG.putNonTerminators(Arrays.asList("E", "E'", "T", "T'", "F"));
        PLUS_MULTIPLY_EP2_CFG.start = "E";
        PLUS_MULTIPLY_EP2_CFG.productions = Arrays.asList(
                new ContextFreeGrammar.Production("E", Arrays.asList("T", "E'")),
                new ContextFreeGrammar.Production("E'", Arrays.asList("+", "T", "E'")),
                new ContextFreeGrammar.Production("E'", Collections.emptyList()),
                new ContextFreeGrammar.Production("T", Arrays.asList("F", "T'")),
                new ContextFreeGrammar.Production("T'", Arrays.asList("*", "F", "T'")),
                new ContextFreeGrammar.Production("T'", Collections.emptyList()),
                new ContextFreeGrammar.Production("F", Arrays.asList("(", "E", ")")),
                new ContextFreeGrammar.Production("F", Collections.singletonList("id"))
        );
    }
}
