package io.github.sboyanovich.parsergenerator.misc;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

// no validation at all for now
public class CFGrammarBuilder {
    private int nonTerminalAlphabetSize;
    private int terminalAlphabetSize;
    private int axiom;

    private Map<Integer, Set<UAString>> rules;

    public CFGrammarBuilder(
            int nonTerminalAlphabetSize,
            int terminalAlphabetSize,
            int axiom
    ) {
        this.nonTerminalAlphabetSize = nonTerminalAlphabetSize;
        this.terminalAlphabetSize = terminalAlphabetSize;
        this.axiom = axiom;
        this.rules = new HashMap<>();
    }

    public int getNonTerminalAlphabetSize() {
        return nonTerminalAlphabetSize;
    }

    public int getTerminalAlphabetSize() {
        return terminalAlphabetSize;
    }

    public int getAxiom() {
        return axiom;
    }

    public void addProduction(CFGProduction production) {
        int nonTerminal = production.getNonTerminal();
        if (!this.rules.containsKey(nonTerminal)) {
            this.rules.put(nonTerminal, new LinkedHashSet<>());
        }
        Set<UAString> rhss = this.rules.get(nonTerminal);
        rhss.add(production.getRhs());
    }

    public CFGrammar build(Function<Integer, String> nativeTai,
                           Function<Integer, String> nativeNtai) {
        return new CFGrammar(
                this.nonTerminalAlphabetSize,
                this.terminalAlphabetSize,
                this.axiom,
                this.rules,
                nativeTai,
                nativeNtai
        );
    }

    public CFGrammar build() {
        return new CFGrammar(
                this.nonTerminalAlphabetSize,
                this.terminalAlphabetSize,
                this.axiom,
                this.rules
        );
    }
}
