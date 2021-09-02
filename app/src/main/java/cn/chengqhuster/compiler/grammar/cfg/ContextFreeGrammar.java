package cn.chengqhuster.compiler.grammar.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextFreeGrammar {

    public static final String END_TERMINATOR = "$";

    /**
     * 终结符集合
     */
    public List<String> terminators;

    /**
     * 终结符在集合的 index 位置 map
     */
    public Map<String, Integer> terminatorIndexMap;

    /**
     * 非终结符集合
     */
    public List<String> nonTerminators;

    /**
     * 非终结符在集合的 index 位置 map
     */
    public Map<String, Integer> nonTerminatorIndexMap;

    /**
     * 开始符号 start 属于 nonTerminators
     */
    public String start;

    /**
     * 产生式规则
     */
    public List<Production> productions;

    public void putTerminators(List<String> terminators) {
        this.terminators = new ArrayList<>(terminators);
        this.terminatorIndexMap = new HashMap<>();
        for (int i = 0; i < this.terminators.size(); i++) {
            this.terminatorIndexMap.put(this.terminators.get(i), i);
        }
    }

    public void putNonTerminators(List<String> nonTerminators) {
        this.nonTerminators = new ArrayList<>(nonTerminators);
        this.nonTerminatorIndexMap = new HashMap<>();
        for (int i = 0; i < this.nonTerminators.size(); i++) {
            this.nonTerminatorIndexMap.put(this.nonTerminators.get(i), i);
        }
    }

    public static class Production {

        public String nonTerminator;

        public List<String> symbols;

        public Production(String nonTerminator, List<String> symbols) {
            this.nonTerminator = nonTerminator;
            this.symbols = symbols;
        }
    }
}
