package cn.chengqhuster.compiler.grammar.bottomup.lr1;

import cn.chengqhuster.compiler.grammar.cfg.CFGRepo;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LR1AnalyseTableTest {

    @Test
    void grammarCheck() {
        ContextFreeGrammar lrCFG = CFGRepo.PLUS_MULTIPLY_EP1_CFG;
        LR1AnalyseTable analyseTable = new LR1AnalyseTable(lrCFG);

        String epA = "id * ( id + id ) + ( id + id ) * ( id + id )";
        assertTrue(analyseTable.grammarCheck(Arrays.asList(epA.split(" "))));

        String epB = "id * ( id + id ) + ( id + id * ( id + id )";
        assertFalse(analyseTable.grammarCheck(Arrays.asList(epB.split(" "))));

        String epC = "id * ( id + id ) + ( id + id ) * ( id1 + id )";
        assertFalse(analyseTable.grammarCheck(Arrays.asList(epC.split(" "))));
    }
}