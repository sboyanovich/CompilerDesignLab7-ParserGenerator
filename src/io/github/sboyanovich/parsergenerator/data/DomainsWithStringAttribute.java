package io.github.sboyanovich.parsergenerator.data;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    // TODO: Add escape removal
    TERMINAL {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, TERMINAL, attribute(text, fragment));
        }
    },
    NON_TERMINAL {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, NON_TERMINAL, attribute(text, fragment));
        }

        @Override
        public String attribute(Text text, Fragment fragment) {
            String all = super.attribute(text, fragment);
            return all.substring(1, all.length() - 1);
        }
    },
    AXM_DECL {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String all = super.attribute(text, fragment);
            String[] split = all.split("[ \t]+");
            String name = split[1];
            return name.substring(0, name.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, AXM_DECL, attribute(text, fragment));
        }
    },
    // AUX, NEEDED JUST FOR ONE TEST
    IDENTIFIER {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, IDENTIFIER, attribute(text, fragment));
        }
    };

    @Override
    public String attribute(Text text, Fragment fragment) {
        return Utility.getTextFragmentAsString(text, fragment);
    }
}
