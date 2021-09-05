package cn.chengqhuster.compiler.grammar.bottomup.data;

import javafx.util.Pair;

import java.util.List;

/**
 * LR 分析文法里面项集的概念，项可以分为内核项以及非内核项
 *
 * 项：文法 G 中的一个生产式和位于它的体中某处的一点（可以用两个数字表示）
 * 内核项：包括初始项 S -> ·S' 以及点不在最左端的所有项
 * 非内核项：除了初始项 S -> ·S' 外的点不在最左端的所有项
 */
public class ItemGroup {

    private String coreTag;

    public List<Pair<Integer, Integer>> coreItems;

    public List<Pair<Integer, Integer>> nonCoreItems;

    public ItemGroup(List<Pair<Integer, Integer>> coreItems, List<Pair<Integer, Integer>> nonCoreItems) {
        this.coreItems = coreItems;
        this.nonCoreItems = nonCoreItems;
    }

    public String getCoreTag () {
        if (this.coreTag == null) {
            this.coreItems.sort((a, b) -> {
                if (a.getKey().equals(b.getKey())) {
                    return a.getValue() - b.getValue();
                } else {
                    return a.getKey() - b.getKey();
                }
            });
            StringBuilder sb = new StringBuilder();
            this.coreItems.forEach(it -> sb.append(it.getKey()).append("-").append(it.getValue()).append(","));
            this.coreTag = sb.substring(0, sb.length() - 1);
        }
        return this.coreTag;
    }
}
