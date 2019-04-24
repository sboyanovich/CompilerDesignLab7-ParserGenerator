package io.github.sboyanovich.parsergenerator.data;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public enum StateTags implements StateTag {
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    },
    DOT {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DOT;
        }
    },
    VERTICAL_BAR {
        @Override
        public Domain getDomain() {
            return SimpleDomains.VERTICAL_BAR;
        }
    },
    EQUALS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.EQUALS;
        }
    },
    TERMINAL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.TERMINAL;
        }
    },
    NON_TERMINAL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.NON_TERMINAL;
        }
    },
    AXM_DECL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.AXM_DECL;
        }
    },
    LPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LPAREN;
        }
    },
    RPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RPAREN;
        }
    },
    OP_MULTIPLY {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OP_MULTIPLY;
        }
    },
    OP_PLUS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OP_PLUS;
        }
    },
    INTEGER_LITERAL {
        @Override
        public Domain getDomain() {
            return DomainsWithIntegerAttribute.INTEGER_LITERAL;
        }
    },
    // AUX, NEEDED JUST FOR ONE TEST
    IDENTIFIER {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.IDENTIFIER;
        }
    }
}
