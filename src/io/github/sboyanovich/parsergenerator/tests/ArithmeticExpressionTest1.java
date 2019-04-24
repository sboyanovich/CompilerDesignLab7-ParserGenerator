package io.github.sboyanovich.parsergenerator.tests;

import io.github.sboyanovich.parsergenerator.ArithmeticExpressionCalculator;
import io.github.sboyanovich.parsergenerator.ParseException;
import io.github.sboyanovich.parsergenerator.ParseTree;
import io.github.sboyanovich.parsergenerator.misc.UnifiedAlphabetSymbol;
import io.github.sboyanovich.parsergenerator.generated.ArithmeticExpressionGrammar;
import io.github.sboyanovich.parsergenerator.data.CommonCharClasses;
import io.github.sboyanovich.parsergenerator.data.DomainsWithIntegerAttribute;
import io.github.sboyanovich.parsergenerator.data.SimpleDomains;
import io.github.sboyanovich.parsergenerator.data.StateTags;
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

import static io.github.sboyanovich.parsergenerator.Utility.inverseMap;
import static io.github.sboyanovich.parsergenerator.Utility.parse;
import static io.github.sboyanovich.parsergenerator.data.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class ArithmeticExpressionTest1 {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        //alphabetSize = 2 * Short.MAX_VALUE + 1;
        alphabetSize = 256; // special case hack for faster recognizer generation

        NFA whitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t", "\n"))
                .union(acceptThisWord(alphabetSize, List.of("\r", "\n")))
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA digitNFA = acceptsAllTheseSymbols(alphabetSize, CommonCharClasses.digits);
        NFA intLiteralNFA = digitNFA
                .positiveIteration()
                .setAllFinalStatesTo(StateTags.INTEGER_LITERAL);

        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("))
                .setAllFinalStatesTo(LPAREN);
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"))
                .setAllFinalStatesTo(RPAREN);
        NFA mulNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(OP_MULTIPLY);
        NFA plusNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("+"))
                .setAllFinalStatesTo(StateTags.OP_PLUS);

        NFA lang = whitespaceNFA
                .union(intLiteralNFA)
                .union(lparenNFA)
                .union(rparenNFA)
                .union(mulNFA)
                .union(plusNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                LPAREN,
                RPAREN,
                INTEGER_LITERAL,
                OP_MULTIPLY,
                OP_PLUS
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

        String text = Utility.getText("aeTest.txt");

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

        List<String> nonTerminalNamesList = ArithmeticExpressionGrammar.getNonTerminalNames();
        Function<Integer, String> nonTerminalNames = nonTerminalNamesList::get;
        int axiom = ArithmeticExpressionGrammar.getAxiom();
        Map<String, Domain> domainNames = Map.of(
                "n", DomainsWithIntegerAttribute.INTEGER_LITERAL,
                "\\(", SimpleDomains.LPAREN,
                "\\)", SimpleDomains.RPAREN,
                "*", SimpleDomains.OP_MULTIPLY,
                "+", SimpleDomains.OP_PLUS
        );
        Map<Integer, Domain> interpretationMap =
                ArithmeticExpressionGrammar.getTermInterpretation(domainNames);
        Map<Domain, Integer> tnMap = inverseMap(interpretationMap);
        Function<Domain, Integer> terminalNumbering = tnMap::get;
        Function<Integer, Domain> interpretation = interpretationMap::get;
        int[][] table = ArithmeticExpressionGrammar.getPredictionTable();
        Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> rules =
                ArithmeticExpressionGrammar.getRules();

        if (errCount == 0) {
            try {
                ParseTree derivation =
                        parse(
                                tokensToParse,
                                nonTerminalNames,
                                axiom,
                                terminalNumbering,
                                interpretation,
                                table,
                                rules
                        );

                dot = derivation.toGraphvizDotString(nonTerminalNames);
                System.out.println(dot);
                System.out.println();
                int value = ArithmeticExpressionCalculator.evaluate(derivation);
                System.out.println("value = " + value);

            } catch (ParseException e) {
                System.out.println(e.getMessage());
                System.out.println("There is a syntax error in the input. Parsing cannot proceed.");
            }
        } else {
            System.out.println("There are lexical errors in the input. Parsing cannot begin.");
        }
    }
}
