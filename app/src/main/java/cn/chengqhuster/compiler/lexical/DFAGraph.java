package cn.chengqhuster.compiler.lexical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 确定有限状态自动机, 图形式表示
 */
public class DFAGraph implements FiniteAutomaton {

    /**
     * 起始状态节点
     */
    DFANode start;

    /**
     * 状态数量
     */
    int count;

    /**
     * 有限状态自动机的转移函数可以看作是一个有向图
     *
     * 确定有限状态自动机的转移条件不可以为空字符, 转移的结果是确定的一个状态
     */
    static class DFANode {

        /**
         * 是否为接受状态
         */
        boolean accept;

        /**
         * 编号
         */
        int value;

        Map<Character, DFANode> nextNodes = new HashMap<>();

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DFANode)) {
                return false;
            }
            DFANode other = (DFANode) obj;
            return other.value == this.value;
        }
    }

    private DFAGraph(DFANode start, int count) {
        this.start = start;
        this.count = count;
    }

    /**
     * 最长匹配
     *
     * @param str
     * @return
     */
    @Override
    public boolean match(String str) {
        throw new UnsupportedOperationException();
    }

    /**
     * hopcroft 算法, DFA 的最小化, 基于等价类的思想
     * @param dfaGraph
     * @return
     */
    public static DFAGraph hopcroft(DFAGraph dfaGraph) {
        // 状态映射
        Map<DFAGraph.DFANode, Set<DFAGraph.DFANode>> trans = splitByAcceptState(dfaGraph);

        boolean flag = true;
        while (flag) {
            Set<String> visited = new HashSet<>();
            flag = false;
            for (Set<DFAGraph.DFANode> nodes : trans.values()) {
                if (nodes.size() == 1) {
                    continue;
                }
                String tag = dfaStateSetTag(nodes);
                if (visited.contains(tag)) {
                    continue;
                }
                visited.add(tag);

                Set<Character> chars = dfaStateSetPathChar(nodes);
                for (char c : chars) {
                    flag = spiltByChar(trans, nodes, c);
                    if (flag) {
                        break;
                    }
                }
            }
        }

        // 构建最小 DFA 节点
        Map<String, DFAGraph.DFANode> minDfaNodeMap = new HashMap<>();
        int number = 0;
        for (Set<DFAGraph.DFANode> nodes : trans.values()) {
            String tag = dfaStateSetTag(nodes);
            if (!minDfaNodeMap.containsKey(tag)) {
                DFAGraph.DFANode node = new DFANode();
                node.value = number++;
                minDfaNodeMap.put(tag, node);
            }
        }

        // 构建最小 DFA 状态转移
        Set<Integer> nodeVisited = new HashSet<>();
        Queue<DFAGraph.DFANode> queue = new LinkedList<>();
        queue.offer(dfaGraph.start);

        // bfs
        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                DFAGraph.DFANode node = queue.poll();
                nodeVisited.add(node.value);
                String setTag = dfaStateSetTag(trans.get(node));
                if (node.accept) {
                    minDfaNodeMap.get(setTag).accept = true;
                }
                for (Map.Entry<Character, DFAGraph.DFANode> entry : node.nextNodes.entrySet()) {
                    minDfaNodeMap.get(setTag).nextNodes.put(entry.getKey(),
                            minDfaNodeMap.get(dfaStateSetTag(trans.get(entry.getValue()))));
                    if (!nodeVisited.contains(entry.getValue().value)) {
                        queue.offer(entry.getValue());
                    }
                }
            }
        }

        return new DFAGraph(minDfaNodeMap.get(dfaStateSetTag(trans.get(dfaGraph.start))), number);
    }

    private static Map<DFAGraph.DFANode, Set<DFAGraph.DFANode>> splitByAcceptState(DFAGraph dfa) {
        Map<DFAGraph.DFANode, Set<DFAGraph.DFANode>> trans = new HashMap<>();

        Set<Integer> visited = new HashSet<>();
        Queue<DFAGraph.DFANode> queue = new LinkedList<>();
        queue.offer(dfa.start);
        Set<DFAGraph.DFANode> acceptSet = new HashSet<>();
        Set<DFAGraph.DFANode> notAcceptSet = new HashSet<>();

        // bfs
        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                DFAGraph.DFANode node = queue.poll();
                visited.add(node.value);
                if (node.accept) {
                    acceptSet.add(node);
                    trans.put(node, acceptSet);
                } else {
                    notAcceptSet.add(node);
                    trans.put(node, notAcceptSet);
                }
                for (DFAGraph.DFANode next : node.nextNodes.values()) {
                    if (!visited.contains(next.value)) {
                        queue.offer(next);
                    }
                }
            }
        }

        return trans;
    }

    /**
     * dfa 状态集合内每个状态跳转的字符条件集合
     * @param nodes
     * @return
     */
    private static Set<Character> dfaStateSetPathChar(Collection<DFAGraph.DFANode> nodes) {
        return nodes.stream()
                .map(it -> it.nextNodes.keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 将 dfa 状态集合按照编号排序, 用编号组成的字串作为状态集合的唯一标识
     * @param nodes
     * @return
     */
    private static String dfaStateSetTag(Collection<DFAGraph.DFANode> nodes) {
        return nodes.stream()
                .map(it -> it.value)
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + b);
    }

    /**
     * 通过字符 c 来分割状态集合
     * @param setMap
     * @param nodes
     * @param c
     * @return 存在划分时返回 true
     */
    private static boolean spiltByChar(Map<DFAGraph.DFANode, Set<DFAGraph.DFANode>> setMap,
                                    Set<DFAGraph.DFANode> nodes,
                                    char c) {
        Map<DFAGraph.DFANode, String> node2stateTag = new HashMap<>();
        for (DFAGraph.DFANode node : nodes) {
            if (node.nextNodes.containsKey(c)) {
                node2stateTag.put(node, dfaStateSetTag(setMap.get(node.nextNodes.get(c))));
            } else {
                node2stateTag.put(node, "");
            }
        }
        Set<String> tags = new HashSet<>(node2stateTag.values());
        if (tags.size() == 1) {
            // 无须分割
            return false;
        } else {
            // 需要分割
            for (String tag : tags) {
                Set<DFAGraph.DFANode> splitSet = new HashSet<>();
                for (Map.Entry<DFAGraph.DFANode, String> entry : node2stateTag.entrySet()) {
                    if (tag.equals(entry.getValue())) {
                        splitSet.add(entry.getKey());
                        setMap.put(entry.getKey(), splitSet);
                    }
                }
            }
            return true;
        }
    }

    /**
     * 子集构造法, 将 NFA 转换为 DFA
     *
     * @param nfa
     * @return
     */
    public static DFAGraph subsetConstruct(NFA nfa) {
        // number 编号
        int number = 0;
        // 通过 key 维护 nfa 到 dfa 的状态映射关系
        Map<String, List<NFA.NFANode>> visited = new HashMap<>();
        Map<String, DFAGraph.DFANode> dfaTran = new HashMap<>();
        // bfs
        Queue<List<NFA.NFANode>> queue = new LinkedList<>();
        // nfa 起始状态
        List<NFA.NFANode> t0 = epClosure(nfa.start);
        String tag0 = nfaStateSetTag(t0);
        visited.put(tag0, t0);
        DFAGraph.DFANode node0 = new DFAGraph.DFANode();
        node0.value = number++;
        dfaTran.put(tag0, node0);

        queue.offer(t0);
        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                List<NFA.NFANode> t = queue.poll();
                String tTag = nfaStateSetTag(t);
                for (char c : nfaStateSetPathChar(t)) {
                    List<NFA.NFANode> ti = epClosure(nfaMove(t, c));
                    String tiTag = nfaStateSetTag(ti);
                    if (!visited.containsKey(tiTag)) {
                        queue.offer(ti);
                        visited.put(tiTag, ti);
                        DFAGraph.DFANode nodeI = new DFAGraph.DFANode();
                        nodeI.value = number++;
                        dfaTran.put(tiTag, nodeI);
                    }
                    // dfa 状态转移
                    dfaTran.get(tTag).nextNodes.put(c, dfaTran.get(tiTag));
                }
            }
        }

        // nfa 只有一个接受状态 endNode, 任何包含该 node 的集合映射到 dfa 的状态都是接受状态
        for (String tag : visited.keySet()) {
            for (NFA.NFANode nfaNode : visited.get(tag)) {
                if (nfaNode.value == nfa.end.value) {
                    dfaTran.get(tag).accept = true;
                }
            }
        }

        return new DFAGraph(dfaTran.get(tag0), number);
    }

    /**
     * 将 nfa 状态集合按照编号排序, 用编号组成的字串作为状态集合的唯一标识
     * @param nodes
     * @return
     */
    private static String nfaStateSetTag(List<NFA.NFANode> nodes) {
        return nodes.stream()
                .map(it -> it.value)
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + b);
    }

    /**
     * 将 nfa 状态集合内每个状态跳转的字符条件集合, 不包含空串条件
     * @param nodes
     * @return
     */
    private static Set<Character> nfaStateSetPathChar(List<NFA.NFANode> nodes) {
        return nodes.stream().map(it -> it.c).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 从某个状态节点开始的空串 (伊普斯弄) 闭包, 能够到达的状态集合
     * @return
     */
    private static List<NFA.NFANode> epClosure(NFA.NFANode a) {
        return epClosure(Collections.singletonList(a));
    }

    /**
     * 从某个状态集合开始的空串 (伊普斯弄) 闭包, 能够到达的状态集合
     * @return
     */
    private static List<NFA.NFANode> epClosure(List<NFA.NFANode> nodes) {
        // bfs
        Set<Integer> visited = new HashSet<>();
        Queue<NFA.NFANode> queue = new LinkedList<>();
        List<NFA.NFANode> res = new ArrayList<>();
        nodes.forEach(it -> {
            visited.add(it.value);
            queue.offer(it);
        });

        while (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
                NFA.NFANode node = queue.poll();
                res.add(node);
                if (node.c == null) {
                    if (node.nodeA != null && !visited.contains(node.nodeA.value)) {
                        visited.add(node.nodeA.value);
                        queue.offer(node.nodeA);
                    }
                    if (node.nodeB != null && !visited.contains(node.nodeB.value)) {
                        visited.add(node.nodeB.value);
                        queue.offer(node.nodeB);
                    }
                }
            }
        }

        return res;
    }

    /**
     * 从某个状态集合开始字符 c 转换, 能够到达的状态集合
     * @param nodes
     * @param c
     * @return
     */
    private static List<NFA.NFANode> nfaMove(List<NFA.NFANode> nodes, char c) {
        // 非空串字符 c 状态转移, 只可能转换到 nodeA (了解 thompson nfa 构造过程及性质)
        return nodes.stream().filter(it -> it.c != null && it.c == c).map(it -> it.nodeA).collect(Collectors.toList());
    }
}
