package cn.chengqhuster.compiler.grammar.topdown.ll1;

import cn.chengqhuster.compiler.grammar.cfg.CFGRepo;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FirstSqSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.FollowSetUtil;
import cn.chengqhuster.compiler.grammar.topdown.ll1.utils.NullableSetUtil;

import java.util.Map;
import java.util.Set;

public class LL1APP {

    public static void main(String[] args) {
        ContextFreeGrammar cfg = CFGRepo.PLUS_MULTIPLY_EP2_CFG;
        Set<String> nullableSet = NullableSetUtil.getNullable(cfg);
        Map<String, Set<String>> firstSet = FirstSetUtil.getFirstSet(cfg, nullableSet);
        Map<String, Set<String>> followSet = FollowSetUtil.getFollowSet(cfg, nullableSet, firstSet);
        Map<Integer, Set<String>> firstSqSet = FirstSqSetUtil.getFirstSqSetUtil(cfg, nullableSet, firstSet, followSet);

        System.out.println(firstSqSet);
    }
}
