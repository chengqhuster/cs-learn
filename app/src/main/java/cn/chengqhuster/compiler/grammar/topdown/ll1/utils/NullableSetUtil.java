package cn.chengqhuster.compiler.grammar.topdown.ll1.utils;

import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;

import java.util.HashSet;
import java.util.Set;

public class NullableSetUtil {

    /**
     * 获取 NULLABLE 集，不动点算法
     * NULLABLE 集用于指导后续的 FIRST 和 FOLLOW 集的构造
     * 当然也可以直接将 空串 加入到 FIRST 集中（注意 FOLLOW 集中没有空串）
     * 以上两者是等价的
     */
    public static Set<String> getNullable(ContextFreeGrammar cfg) {
        Set<String> nullableSet = new HashSet<>();
        boolean flag = true;
        while (flag) {
            flag = false;
            for (ContextFreeGrammar.Production production : cfg.productions) {
                boolean nullable = true;
                // symbols 为空列表，或者列表元素都在 nullable 集中，则相应的 nonTerminator 是 nullable
                for (String symbol : production.symbols) {
                    if (!nullableSet.contains(symbol)) {
                        nullable = false;
                        break;
                    }
                }

                // 集合有改变，继续下一次迭代
                if (nullable) {
                    flag = nullableSet.add(production.nonTerminator);
                }
            }
        }

        return nullableSet;
    }
}
