package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Set;

public class NullableSetUtil {

    /**
     * 获取 NULLABLE 集，不动点算法
     */
    public static Set<String> getNullable(ContextFreeGrammar cfg) {
        Set<String> res = new HashSet<>();
        boolean flag = true;
        while (flag) {
            flag = false;
            for (ContextFreeGrammar.Production production : cfg.productions) {
                boolean nullable = true;
                for (String symbol : production.symbols) {
                    if (!res.contains(symbol)) {
                        nullable = false;
                        break;
                    }
                }

                if (nullable) {
                    flag = res.add(production.nonTerminator);
                }
            }
        }

        return res;
    }
}
