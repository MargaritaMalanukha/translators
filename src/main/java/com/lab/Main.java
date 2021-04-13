package com.lab;

import com.lab.lexicalAnalyzer.LexicalAnalyzer;
import com.lab.syntaxAnalyzer.SyntaxAnalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String input = getFromFile("tests/test10.rty");
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(input);
        lexicalAnalyzer.analyze();
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexicalAnalyzer);
        boolean isSuccessful = syntaxAnalyzer.postfixTranslator();
        if (isSuccessful) {

        } else {
            System.out.println("Mistake on translator's stage. Cannot start interpretation.");
            System.out.println("Exiting the program...");
        }
    }

    public static String getFromFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
