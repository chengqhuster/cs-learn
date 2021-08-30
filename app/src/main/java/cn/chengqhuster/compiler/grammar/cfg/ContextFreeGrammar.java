package cn.chengqhuster.compiler.grammar.cfg;

import java.util.Set;

public class ContextFreeGrammar {

    /**
     * 终结符集合
     */
    public Set<String> terminators;

    /**
     * 非终结符集合
     */
    public Set<String> nonTerminators;

    /**
     * 开始符号 start 属于 nonTerminators
     */
    public String start;

    /**
     * 产生式规则
     */
    public Production[] productions;

    public static class Production {

        public String nonTerminator;

        public String[] symbols;

        public Production(String nonTerminator, String[] symbols) {
            this.nonTerminator = nonTerminator;
            this.symbols = symbols;
        }

        public String toTag() {
            StringBuilder sb = new StringBuilder(nonTerminator);
            for (String symbol : symbols) {
                sb.append(symbol);
            }
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return toTag().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Production)) {
                return false;
            }
            Production other = (Production) obj;
            return other.nonTerminator.equals(this.nonTerminator) && other.toTag().equals(this.toTag());
        }
    }
}
