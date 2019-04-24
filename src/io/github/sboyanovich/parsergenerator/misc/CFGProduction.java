package io.github.sboyanovich.parsergenerator.misc;

public class CFGProduction {
    private int nonTerminal;
    private UAString rhs;

    public CFGProduction(int nonTerminal, UAString rhs) {
        this.nonTerminal = nonTerminal;
        this.rhs = rhs;
    }

    public int getNonTerminal() {
        return nonTerminal;
    }

    public UAString getRhs() {
        return rhs;
    }
}
