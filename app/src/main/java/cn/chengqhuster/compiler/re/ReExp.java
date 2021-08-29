package cn.chengqhuster.compiler.re;

import java.util.HashMap;
import java.util.Map;

public class ReExp {

    public static final char GROUP_LEFT_OPERATOR = '(';

    public static final char GROUP_RIGHT_OPERATOR = ')';

    public static final char UNION_OPERATOR = '|';

    public static final char CLOSURE_OPERATOR = '*';

    public static final char CONNECT_OPERATOR = '·';

    public static Map<Character, Integer> OPERATOR_PRIORITY = new HashMap<>();

    static {
        OPERATOR_PRIORITY.put(CONNECT_OPERATOR, 1);
        OPERATOR_PRIORITY.put(CLOSURE_OPERATOR, 2);
        OPERATOR_PRIORITY.put(UNION_OPERATOR, 3);
    }
}
