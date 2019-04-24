package io.github.sboyanovich.parsergenerator.data;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, WHITESPACE);
        }
    },
    DOT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DOT);
        }
    },
    VERTICAL_BAR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, VERTICAL_BAR);
        }
    },
    EQUALS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, EQUALS);
        }
    },
    LPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LPAREN);
        }
    },
    RPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RPAREN);
        }
    },
    OP_MULTIPLY {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OP_MULTIPLY);
        }
    },
    OP_PLUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OP_PLUS);
        }
    }
}
