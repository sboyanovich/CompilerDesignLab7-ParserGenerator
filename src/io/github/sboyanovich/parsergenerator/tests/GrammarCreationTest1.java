package io.github.sboyanovich.parsergenerator.tests;

import io.github.sboyanovich.parsergenerator.*;
import io.github.sboyanovich.parsergenerator.data.DomainsWithStringAttribute;
import io.github.sboyanovich.parsergenerator.data.SimpleDomains;
import io.github.sboyanovich.parsergenerator.data.StateTags;
import io.github.sboyanovich.parsergenerator.generated.BaseGrammar;
import io.github.sboyanovich.parsergenerator.misc.CFGrammar;
import io.github.sboyanovich.parsergenerator.misc.UnifiedAlphabetSymbol;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.sboyanovich.parsergenerator.Utility.inverseMap;
import static io.github.sboyanovich.parsergenerator.Utility.writeToFile;
import static io.github.sboyanovich.parsergenerator.data.CommonCharClasses.alphanumerics;
import static io.github.sboyanovich.parsergenerator.data.CommonCharClasses.letters;
import static io.github.sboyanovich.parsergenerator.data.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

/**
 * GENERATING PARSER FOR GRAMMAR GRAMMAR USING GENERATED META-GRAMMAR PARSER
 */
public class GrammarCreationTest1 {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        //alphabetSize = 2 * Short.MAX_VALUE + 1;
        alphabetSize = 256; // special case hack for faster recognizer generation

        NFA slWhitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t"))
                .positiveIteration();

        NFA whitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t", "\n"))
                .union(acceptThisWord(alphabetSize, List.of("\r", "\n")))
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA lettersNFA = acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = acceptsAllTheseSymbols(alphabetSize, alphanumerics);
        NFA underscoreNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("_"));

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.union(underscoreNFA).iteration())
                .setAllFinalStatesTo(IDENTIFIER);

        NFA kwAxiomNFA = acceptThisWord(alphabetSize, List.of("a", "x", "i", "o", "m"));
        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("));
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"));

        NFA axmDeclNFA = lparenNFA
                .concatenation(kwAxiomNFA)
                .concatenation(slWhitespaceNFA)
                .concatenation(identifierNFA)
                .concatenation(rparenNFA)
                .setAllFinalStatesTo(AXM_DECL);

        NFA nonTerminalNFA = lparenNFA
                .concatenation(identifierNFA)
                .concatenation(rparenNFA)
                .setAllFinalStatesTo(NON_TERMINAL);

        NFA equalsNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("="))
                .setAllFinalStatesTo(EQUALS);

        NFA vbarNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("|"))
                .setAllFinalStatesTo(VERTICAL_BAR);

        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("."))
                .setAllFinalStatesTo(DOT);

        NFA specialNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(".", "|", "=", "(", ")"));

        NFA arithmOpNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("*", "+", "-", "/"));
        NFA escapeNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("\\"));

        NFA terminalNFA = identifierNFA
                .union(arithmOpNFA)
                .union(escapeNFA.concatenation(specialNFA))
                .setAllFinalStatesTo(StateTags.TERMINAL);

        /// LEXICAL STRUCTURE

        /*
        // AUX
            IDENTIFIER      = ([a-z][A-Z])([a-z][A-Z][0-9][_])*
            SPECIAL         = [.|=()]

        // TOKEN TYPES
            TERMINAL        = {IDENTIFIER} | [*+-/] | (\\{SPECIAL})
            AXM_DECL        = \(axiom[ \t]+{IDENTIFIER}]\)
            NON_TERMINAL    = \({IDENTIFIER}\)
            EQUALS          = =
            VERTICAL_BAR    = |
            DOT             = \.
        */

        NFA lang = whitespaceNFA
                .union(terminalNFA)
                .union(axmDeclNFA)
                .union(nonTerminalNFA)
                .union(equalsNFA)
                .union(vbarNFA)
                .union(dotNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                TERMINAL,
                AXM_DECL,
                NON_TERMINAL,
                DOT,
                VERTICAL_BAR,
                EQUALS
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        System.out.println("NFA has " + lang.getNumberOfStates() + " states.");

        Instant start = Instant.now();
        LexicalRecognizer recognizer = Utility.createRecognizer(lang, priorityMap);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Recognizer built in " + timeElapsed + "ms!\n");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("l7test3.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        int errCount = 0;

        List<Token> tokensToParse = new ArrayList<>();

        Token t = scanner.nextToken();
        while (t.getTag() != Domain.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                tokensToParse.add(t);
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
        }
        tokensToParse.add(t);

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }
        List<String> nonTerminalNamesList = BaseGrammar.getNonTerminalNames();

        Function<Integer, String> nonTerminalNames = nonTerminalNamesList::get;
        int axiom = BaseGrammar.getAxiom();
        Map<String, Domain> domainNames = Map.of(
                "TERMINAL", DomainsWithStringAttribute.TERMINAL,
                "NON_TERMINAL", DomainsWithStringAttribute.NON_TERMINAL,
                "AXM_DECL", DomainsWithStringAttribute.AXM_DECL,
                "\\.", SimpleDomains.DOT,
                "\\|", SimpleDomains.VERTICAL_BAR,
                "\\=", SimpleDomains.EQUALS
        );
        Map<Integer, Domain> interpretationMap = BaseGrammar.getTermInterpretation(domainNames);
        Map<Domain, Integer> tnMap = inverseMap(interpretationMap);
        Function<Domain, Integer> terminalNumbering = tnMap::get;
        Function<Integer, Domain> interpretation = interpretationMap::get;
        int[][] table = BaseGrammar.getPredictionTable();
        Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> rules = BaseGrammar.getRules();

        if (errCount == 0) {
            try {
                ParseTree derivation =
                        io.github.sboyanovich.parsergenerator.Utility
                                .parse(
                                        tokensToParse,
                                        nonTerminalNames,
                                        axiom,
                                        terminalNumbering,
                                        interpretation,
                                        table,
                                        rules
                                );

                CFGrammar grammar = new GrammarCreator(derivation).createGrammar();

                List<String> useless = grammar.getExplicitlyUselessNonTerminals().stream()
                        .map(i -> grammar.getNativeNtai().apply(i))
                        .collect(Collectors.toList());
                if (!useless.isEmpty()) {
                    throw new AppException("There are useless nonterminals in this grammar: " + useless.toString());
                }

                String grammarString = grammar.toString();

                System.out.println();
                System.out.println(grammarString);
                System.out.println();

                String className = "BaseGrammar";
                String gen = io.github.sboyanovich.parsergenerator.Utility.grammarAsClass(grammar, className);
                writeToFile(
                        "src/io/github/sboyanovich/parsergenerator/generated/" + className + ".java",
                        gen
                );

            } catch (ParseException e) {
                System.out.println(e.getMessage());
                System.out.println("There is a syntax error in the input. Parsing cannot proceed.");
            } catch (GrammarCreationException e) {
                System.out.println(e.getMessage());
                System.out.println("Grammar could not be created.");
            } catch (PredictionTableCreationException e) {
                System.out.println(e.getMessage());
            } catch (AppException e) {
                System.out.println(e.getMessage());
                System.out.println("App cannot proceed.");
            }
        } else {
            System.out.println("There are lexical errors in the input. Parsing cannot begin.");
        }
    }
}
