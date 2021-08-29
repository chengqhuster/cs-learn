package cn.chengqhuster.compiler.lexical;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 确定有限状态自动机, 转换表表示
 * 相比较图形表示的状态转移方式, 转换表访问的效率更高, 内存结构紧凑,
 * 但是冗余了所有状态之间的跳转信息, 数据结构空间占用可能比较大
 */
public class DFATable implements FiniteAutomaton {

    private static final int ASCII_TOTAL = 256;

    // 错误状态
    private static final int ERROR_STATE = -1;

    // 列方向为状态, 行方向为字符, 数组元素表示该行状态在接受该列字符后跳转的状态
    private int[][] stateTable;

    private DFATable(int stateCount) {
        this.stateTable = new int[stateCount][ASCII_TOTAL];
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < ASCII_TOTAL; j++) {
                // 状态初始欢
                stateTable[i][j] = ERROR_STATE;
            }
        }
    }

    @Override
    public boolean match(String str) {
        return false;
    }

    /**
     * dfaTable 的转换表初始化的时候最好知道状态的数量, 所以可以从构造好的 dfaGraph 转换
     * 当然也可以在 nfa 通过子集构造法构造 dfa 的时候创建, 需要维护中间行向量信息, 再聚合到数组, 这里不做实现
     * @param dfaGraph
     * @return
     */
    public static DFATable ofDFAGraph(DFAGraph dfaGraph) {
        DFATable dfaTable = new DFATable(dfaGraph.count);

        // bfs
        Set<Integer> visited = new HashSet<>();
        Queue<DFAGraph.DFANode> queue = new LinkedList<>();
        queue.offer(dfaGraph.start);

        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                DFAGraph.DFANode node = queue.poll();
                visited.add(node.value);
                for (Map.Entry<Character, DFAGraph.DFANode> entry : node.nextNodes.entrySet()) {
                    dfaTable.stateTable[node.value][entry.getKey()] = entry.getValue().value;
                    if (!visited.contains(entry.getValue().value)) {
                        queue.offer(entry.getValue());
                    }
                }
            }
        }

        return dfaTable;
    }
}
