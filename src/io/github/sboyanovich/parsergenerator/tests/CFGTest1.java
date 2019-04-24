package io.github.sboyanovich.parsergenerator.tests;

import io.github.sboyanovich.parsergenerator.aux.*;

import java.util.List;

public class CFGTest1 {
    public static void main(String[] args) {
        CFGrammarBuilder cfgb = new CFGrammarBuilder(2, 2, 0);
        List<String> ntNames = List.of("A", "B");
        List<String> tNames = List.of("x", "y");
        CFGProduction p1 = new CFGProduction(
                0, new UAString(List.of(
                new UnifiedAlphabetSymbol(0, true),
                new UnifiedAlphabetSymbol(1, false),
                new UnifiedAlphabetSymbol(0, false)
        ))
        );
        CFGProduction p2 = new CFGProduction(
                1, new UAString(List.of(new UnifiedAlphabetSymbol(1, true)))
        );
        CFGProduction p3 = new CFGProduction(1, new UAString(List.of()));

        cfgb.addProduction(p1);
        cfgb.addProduction(p2);
        cfgb.addProduction(p3);

        CFGrammar cfg = cfgb.build();

        String text = cfg.toString(
                tNames::get,
                ntNames::get
        );

        System.out.println(text);
    }
}
