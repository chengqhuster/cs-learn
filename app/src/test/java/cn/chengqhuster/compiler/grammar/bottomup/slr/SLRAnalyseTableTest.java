package cn.chengqhuster.compiler.grammar.bottomup.slr;

import cn.chengqhuster.compiler.grammar.cfg.CFGRepo;
import cn.chengqhuster.compiler.grammar.cfg.ContextFreeGrammar;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SLRAnalyseTableTest {

    @Test
    void checkGrammar() {
        ContextFreeGrammar lrCFG = CFGRepo.PLUS_MULTIPLY_EP1_CFG;
        SLRAnalyseTable analyseTable = new SLRAnalyseTable(lrCFG);

        String epA = "id * ( id + id ) + ( id + id ) * ( id + id )";
        assertTrue(analyseTable.grammarCheck(Arrays.asList(epA.split(" "))));

        String epB = "id * ( id + id ) + ( id + id * ( id + id )";
        assertFalse(analyseTable.grammarCheck(Arrays.asList(epB.split(" "))));

        String epC = "id * ( id + id ) + ( id + id ) * ( id1 + id )";
        assertFalse(analyseTable.grammarCheck(Arrays.asList(epC.split(" "))));
    }
}