package com.lab.lexicalAnalyzer;

import com.lab.lexicalAnalyzer.constants.LexerData;

import java.util.ArrayList;
import java.util.Arrays;

import static com.lab.lexicalAnalyzer.constants.AnalyzerTables.*;

/**
 * @author Margarita Malanukha
 */

public class LexicalAnalyzer {

    //текст программы, которые подается на вход анализатора
    private final String programInput;

    //выход лексичного анализатора
    private final ArrayList<LexerData> tableOfSymbols = new ArrayList<>();

    //просмотр выхода лексичного анализатора
    private final ArrayList<String> tableOfSymbolsView = new ArrayList<>();

    //количество строк входной программы
    private int numLine = 1;

    //переменная-счетчик пройденных символов
    private int numChar = -1;

    private boolean isFailed = false;

    private final ArrayList<String> identifiers = new ArrayList<>();

    public LexicalAnalyzer(String programInput) {
        this.programInput = programInput;
    }

    public void analyze() {

        String state = initialState;
        StringBuilder currentLexeme = new StringBuilder();
        while (numChar < programInput.length() - 1) {
            //берем входной символ на обработку
            String current = String.valueOf(nextChar());
            //находим класс символа
            String classOfChar = findClassOfChar(current);
            //обновляем состояние лексемы
            state = nextState(classOfChar, state);
            //если состояние - завершающее, то обработать лексему и вернуться к начальному состоянию.
            if (is_final(state)) {
                processing(currentLexeme.toString(), state, current);
                currentLexeme.delete(0, currentLexeme.length());
                state = initialState;
            }
            //если состояние - начальное, то очистить лексему
            else if (state.equals(initialState)) {
                currentLexeme.delete(0, currentLexeme.length());
            }
            //если состояние - не начальное и не завершающее, то продолжить собирать лексему
            else {
                currentLexeme.append(current);
            }
        }
        tableOfSymbolsView.forEach(System.out::println);
        int status = isFailed ? 0 : 1;
        System.out.println("Lexer success status: " + status);

    }

    private void processing(String lexeme, String state, String current) {
        //Newline
        if (state.equals("13")) {
            numLine++;
        }
        //keyword, boolval, id, realnum, intnum
        else if (Arrays.asList(Fstar).contains(state)) {
            String token = getToken(lexeme);

            if ("keyword".equals(token)) {
                addToOutputTable(numLine, lexeme, token);
            } else if ("boolval".equals(token)) {
                String value = "1";
                if (lexeme.equals("false")) value = "0";
                addToOutputTable(numLine, lexeme, token, value);
            } else if ("none".equals(token) && state.equals("2")) {
                if (!identifiers.contains(lexeme)) {
                    identifiers.add(lexeme);
                }
                int index = identifiers.indexOf(lexeme);
                addToOutputTable(numLine, lexeme, token, index);
            } else {
                addToOutputTable(numLine, lexeme, token, lexeme);
            }
            putCharBack();
        }
        //semicolons, brackets
        else if (state.equals("10") || state.equals("11")) {
            lexeme = lexeme + current;
            String token = getToken(lexeme);
            addToOutputTable(numLine, lexeme, token);
        }
        else if (state.equals("12")) {
            String token = getToken(lexeme);
            if (token.equals("none")) {
                fail(state, lexeme);
            } else {
                addToOutputTable(numLine, lexeme, token);
            }

        } else if (Arrays.asList(Ferror).contains(state)) {
            fail(state, lexeme + current);
        }
    }

    private char nextChar() {
        numChar++;
        return programInput.charAt(numChar);
    }

    private void putCharBack() {
        numChar--;
    }

    private String findClassOfChar(String ch) {
        if (ch.equals(".")) return "dot";
        if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(ch)) return "Letter";
        if ("0123456789".contains(ch)) return "Digit";
        if (" \t".contains(ch)) return "ws";
        if (ch.equals("\n")) return "nl";
        if (ch.equals(";")) return "sc";
        if ("!=*/<>&|".contains(ch)) return "op";
        if ("+-".contains(ch)) return "sign";
        if ("{}()".contains(ch)) return "brackets";
        return "other";
    }

    private String nextState(String currentClass, String state) {
        String newState = findState(currentClass, state);
        if (!newState.equals("-1")) return newState;
        return findState("other", state);
    }

    private String findState(String currentClass, String state) {
        for (String[] strings : stateTransitionFunction) {
            if (strings[0].equals(state) && strings[1].equals(currentClass)) {
                return strings[2];
            }
        }
        return "-1";
    }

    private boolean is_final(String input) {
        for (String str : F) {
            if (str.equals(input)) return true;
        }
        return false;
    }

    private void addToOutputTable(int numLine, String lexeme, String token) {
        tableOfSymbolsView.add(numLine + "        lexeme: " + lexeme + fillWithSpace(lexeme.length(), 20) +
                "token: " + token);
        tableOfSymbols.add(new LexerData(numLine, lexeme, token, true));
    }

    private void addToOutputTable(int numLine, String lexeme, String token, String value) {
        tableOfSymbolsView.add(numLine + "        lexeme: " + lexeme + fillWithSpace(lexeme.length(), 20) +
                "token: " + token + fillWithSpace(token.length(), 15) + "value: " + value);
        tableOfSymbols.add(new LexerData(numLine, lexeme, token, true));
    }

    private void addToOutputTable(int numLine, String lexeme, String token, int id) {
        tableOfSymbolsView.add(numLine + "        lexeme: " + lexeme + fillWithSpace(lexeme.length(), 20) +
                "token: " + token + fillWithSpace(token.length(), 15) + "id: " + id);
        tableOfSymbols.add(new LexerData(numLine, lexeme, token, true));
    }

    private String fillWithSpace(int strLength, int spaceLength) {
        int len = spaceLength > strLength ? spaceLength - strLength : strLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String getToken(String lexeme) {
        return tableOfLanguageTokens.get(lexeme) != null ? tableOfLanguageTokens.get(lexeme) : "none";
    }

    private void fail(String state, String lexeme) {
        String str = "Unexpected token \"" + lexeme + "\" at line " + numLine;
        if (state.equals("102")) str = str + "\nDid you mean \".12345\" (real value)?";
        tableOfSymbolsView.add(str);
        tableOfSymbols.add(new LexerData(numLine, lexeme, state, false));
        isFailed = true;
    }



}
