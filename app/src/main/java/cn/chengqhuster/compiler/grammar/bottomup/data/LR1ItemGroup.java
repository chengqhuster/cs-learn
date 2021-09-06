package cn.chengqhuster.compiler.grammar.bottomup.data;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * LR1 分析文法里面项集的概念，相比较 LR0 的项集，增加了一个终止符的信息
 *
 */
public class LR1ItemGroup {

    private String coreTag;

    public List<Triple<Integer, Integer, String>> coreItems;

    public List<Triple<Integer, Integer, String>>  nonCoreItems;

    public LR1ItemGroup(List<Triple<Integer, Integer, String>> coreItems, List<Triple<Integer, Integer, String>> nonCoreItems) {
        this.coreItems = coreItems;
        this.nonCoreItems = nonCoreItems;
    }

    public String getCoreTag () {
        if (this.coreTag == null) {
            this.coreItems.sort((a, b) -> {
                if (a.getLeft().equals(b.getLeft()) && a.getMiddle().equals(b.getMiddle())) {
                    return a.getRight().compareTo(b.getRight());
                } else if (a.getLeft().equals(b.getLeft())) {
                    return a.getMiddle() - b.getMiddle();
                } else {
                    return a.getLeft() - b.getLeft();
                }
            });
            StringBuilder sb = new StringBuilder();
            this.coreItems.forEach(it -> sb.append(it.getLeft()).append("-").append(it.getMiddle()).append("-").append(it.getRight()).append(","));
            this.coreTag = sb.substring(0, sb.length() - 1);
        }
        return this.coreTag;
    }
}
