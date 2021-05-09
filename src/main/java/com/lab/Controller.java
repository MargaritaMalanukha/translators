package com.lab;

import com.lab.interpreter.Interpreter;
import com.lab.lexicalAnalyzer.LexicalAnalyzer;
import com.lab.syntaxAnalyzer.SyntaxAnalyzer;

public class Controller {

    public static void process(String input) {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(input);
        lexicalAnalyzer.analyze();

        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexicalAnalyzer);
        boolean isTranslationSuccessful = syntaxAnalyzer.postfixTranslator();

        if (isTranslationSuccessful) {
            Interpreter interpreter = new Interpreter(syntaxAnalyzer);
            System.out.println("Postfix Code:");
            syntaxAnalyzer.postfixCode.forEach(System.out::println);
            boolean isInterpretationSuccessful = interpreter.postfixProcessing();
            if (isInterpretationSuccessful) {
                System.out.println("Interpretation finished successfully!");
                System.out.println("Program output:");
                interpreter.outputList.forEach(System.out::print);
            } else {
                System.out.println("Mistake on interpreter's stage.");
                System.out.println("Exiting the program...");
            }
        } else {
            System.out.println("Mistake on translator's stage. Cannot start interpretation.");
            System.out.println("Exiting the program...");
        }

    }

}
