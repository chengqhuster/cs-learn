package cn.chengqhuster.compiler.grammar;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrammarUtil {

    public static final String START_NON_TERMINATOR = "#START#";

    /**
     * 终止符号，非语法解析目标符号串自身的符号，用于分析过程
     */
    public static final String END_TERMINATOR = "$END$";

    /**
     * 增广文法
     * 对于以 S 为开始符号的文法 G，它的增广文法 G' 就是在 G 中增加新的开始符号 S' 和产生式 S' -> S
     * 引入的目的是确保 G' 的开始符号 S' 不会出现在产生式的右侧，从而告诉语法分析器何时应该停止语法分析
     */
    public static ContextFreeGrammar augmentedGrammar(ContextFreeGrammar cfg) {
        ContextFreeGrammar augmentedCfg = new ContextFreeGrammar();
        augmentedCfg.start = START_NON_TERMINATOR;

        List<String> terminators = new ArrayList<>(cfg.terminators);
        augmentedCfg.putTerminators(terminators);

        List<String> nonTerminators = new ArrayList<>();
        nonTerminators.add(START_NON_TERMINATOR);
        nonTerminators.addAll(cfg.nonTerminators);
        augmentedCfg.putNonTerminators(nonTerminators);

        List<ContextFreeGrammar.Production> productions = new ArrayList<>();
        productions.add(new ContextFreeGrammar.Production(START_NON_TERMINATOR, Collections.singletonList(cfg.start)));
        productions.addAll(cfg.productions);
        augmentedCfg.productions = productions;

        return augmentedCfg;
    }

    /**
     * 给 grammar 添加终止符（属于终结符，同时也一定属于开始符号的 FOLLOW 集）
     */
    public static void endGrammar(ContextFreeGrammar cfg) {
        cfg.terminators.add(END_TERMINATOR);
        cfg.terminatorIndexMap.put(END_TERMINATOR, cfg.terminators.size() - 1);
    }
}
