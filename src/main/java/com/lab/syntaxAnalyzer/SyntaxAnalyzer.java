package com.lab.syntaxAnalyzer;

import com.lab.lexicalAnalyzer.LexicalAnalyzer;
import com.lab.lexicalAnalyzer.pojo.IdentifierData;
import com.lab.lexicalAnalyzer.pojo.LexerData;
import com.lab.lexicalAnalyzer.pojo.ValueData;
import com.lab.syntaxAnalyzer.exceptions.ParserException;

//todo пофиксить отсутствие ошибки при присваивании без инициализации
//todo In, Out, ForStatement, IfStatement и всё, что ниже уровнем
public class SyntaxAnalyzer {

    private int counter = 0;
    private final LexicalAnalyzer lexicalAnalyzer;
    private final String indent1 = "";
    private final String indent2 = "\t";
    private final String indent3 = "\t\t";
    private final String indent4 = "\t\t\t";
    private final String indent5 = "\t\t\t\t";
    private final String indent6 = "\t\t\t\t\t";
    private final String indent7 = "\t\t\t\t\t\t";
    private final String indent8 = "\t\t\t\t\t\t\t";

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    /**
     * first level function
     */

    public void parseProgram(){
        try {
            parseToken("program", "keyword", indent1);
            String programName = getTableOfSymbolsElement().getLexeme();
            parseToken(programName, "id", indent1);
            parseToken("{", "braces_op", indent1);
            parseStatementList();
            parseToken("}", "braces_op", indent1);
            System.out.println("Parser success status: 1.");
        } catch (ParserException e) {
            System.out.println(e.getMessage());
            System.out.println("Parser success status: 0.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Parser ERROR: No closing braces found!");
            System.out.println("Parser success status: 0.");
        }
    }

    /**
     * main parser
     */

    private boolean parseToken(String lexeme, String token, String indent) throws ParserException {
        if (counter > lexicalAnalyzer.tableOfSymbols.size()) {
            failParse(counter, lexeme, token);
        }

        LexerData lexerData = getTableOfSymbolsElement();

        counter++;

        if (lexerData.getLexeme().equals(lexeme) && lexerData.getToken().equals(token)) {
            printLine(lexerData, indent);
            return true;
        } else {
            failParse(lexerData.getNumLine(), lexeme, token, lexerData);
            return false;
        }
    }

    /**
     * second level function
     */

    //StatementList = Statement { Statement }
    private void parseStatementList() throws ParserException {
        System.out.println(indent2 + "parseStatementList(): ");
        while (parseStatement());
    }

    /**
     * third level function
     */

    //Statement = (Assign | Inp | Out | ForStatement | IfStatement) ‘;’
    private boolean parseStatement() throws ParserException {
        System.out.println(indent3 + "parseStatement(): ");
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getToken().equals("id") || lexerData.getLexeme().equals("int")
        || lexerData.getLexeme().equals("real") || lexerData.getLexeme().equals("bool")) {
            parseAssign();
            parseToken(";", "punct", indent3);
            return true;
        } else if (lexerData.getLexeme().equals("read")) {
            parseRead();
            parseToken(";", "punct", indent3);
            return true;
        } else if (lexerData.getLexeme().equals("print")) {
            parsePrint();
            parseToken(";", "punct", indent3);
            return true;
        } else if (lexerData.getLexeme().equals("for")) {
            parseFor();
            parseToken(";", "punct", indent3);
            return true;
        } else if (lexerData.getLexeme().equals("if")) {
            parseIf();
            parseToken(";", "punct", indent3);
            return true;
        } else if (lexicalAnalyzer.tableOfSymbols.size() == counter+1) {
            return false;
        } else {
            System.out.println(lexicalAnalyzer.tableOfSymbols.size());
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
            return false;
        }
    }

    /**
     * fourth level functions
     */

    //Assign = Ident ’=’ Expression
    private void parseAssign() throws ParserException {
        parseInit();
        System.out.println(indent4 + "parseAssign():");
        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        if (parseToken("=", "assign_op", indent4)) {
            parseExpression();
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }
    private void parseRead() { }
    private void parsePrint() { }
    private void parseFor() { }
    private void parseIf() { }

    /**
     * fifth level functions
     */
    //Expression = ArithmExpression | BoolExpr
    private void parseExpression() throws ParserException {
        System.out.println(indent5 + "parseExpression():");
       // LexerData lexerData = getTableOfSymbolsElement();
        parseArithmExpression();
    }

    private void parseInit() throws ParserException {

        LexerData lexerData = getTableOfSymbolsElement();
        boolean isCurrentSymbolAnInitKeyword = lexerData.getLexeme().equals("int") || lexerData.getLexeme().equals("real") ||
                lexerData.getLexeme().equals("bool");
        int identCounter = 1;
        identCounter += counter;
        LexerData ident = lexicalAnalyzer.tableOfSymbols.get(identCounter);
        boolean isNextAnIdent = lexicalAnalyzer.identifiers
                .stream()
                .anyMatch(e -> e.getIdentifier().equals(ident.getLexeme()));
        boolean isNextIdentInitialized = lexicalAnalyzer.identifiers
                .stream()
                .anyMatch(e -> e.getNumChar() < ident.getNumChar() && e.getIdentifier().equals(ident.getLexeme()));
        //если первая лексема инициализирует, а второй нету в таблице идентификаторов
        if (isCurrentSymbolAnInitKeyword && !isNextAnIdent) {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
        //если первая лексема инициализирует, вторая - идентификатор, который уже был инициализирован
        if (isCurrentSymbolAnInitKeyword && isNextAnIdent && isNextIdentInitialized) {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
        //если первая лексема не инициализирует, вторая - идентификатор, который не был инициализирован
        if (!isCurrentSymbolAnInitKeyword && isNextAnIdent && !isNextIdentInitialized) {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
        if (isCurrentSymbolAnInitKeyword) {

            parseToken(lexerData.getLexeme(), lexerData.getToken(), indent5);
        }

    }

    /**
     * sixth+ level functions
     */
    //ArithmExpression = Term {(’+’ | ’-’) Term}
    private void parseArithmExpression() throws ParserException {
        System.out.println(indent6 + "parseArithmExpression():");
        parseTerm();
        boolean isCorrect = true;
        while (isCorrect) {
            LexerData lexerData = getTableOfSymbolsElement();
            if (lexerData.getToken().equals("add_op")) {
                counter++;
                printLine(lexerData, indent6);
                parseTerm();
            } else {
                isCorrect = false;
            }
        }
    }
    //Term = Factor {(’*’|’/’) Factor}
    private void parseTerm() throws ParserException {
        System.out.println(indent7 + "parseTerm():");
        parseFactor();
        boolean isCorrect = true;
        while (isCorrect) {
            LexerData lexerData = getTableOfSymbolsElement();
            if (lexerData.getToken().equals("mult_op")) {
                counter++;
                printLine(lexerData, indent7);
                parseFactor();
            } else {
                isCorrect = false;
            }
        }
    }

    //Factor = Id | Literal | ’(’ ArithmExpression ’)’
    private void parseFactor() throws ParserException {
        System.out.println(indent8 + "parseFactor():");
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getToken().equals("id") || lexicalAnalyzer.values
                .contains(new ValueData(counter, lexerData.getLexeme()))) {
            counter++;
            printLine(lexerData, indent8);
        } else if (lexerData.getToken().equals("(")) {
            counter++;
            parseArithmExpression();
            parseToken(")", "braces_op", indent8);
            printLine(lexerData, indent8);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    /**
     * Tools
     */

    private void printLine(LexerData lexerData, String indent) {
        System.out.println(indent + "parseToken: in line " + lexerData.getNumLine() +
                " lexeme: " + lexerData.getLexeme() +
                " token: " + lexerData.getToken());
    }

    private void failParse(int numLine, String lexeme, String token) throws ParserException {
        String message = "Parser ERROR: In line " + numLine +
                " unknown element with lexeme (" + lexeme + ") and token (" + token + ").";
        throw new ParserException(message);
    }

    private void failParse(int numLine, String lexeme, String token, LexerData lexerData) throws ParserException {
        String message = "Parser ERROR: In line " + numLine +
                " unknown element with lexeme (" + lexerData.getLexeme() + ") and token ("
                + lexerData.getToken() + ")." + " Expected: (" + lexeme + ") (" + token + ").";
        throw new ParserException(message);
    }

    private LexerData getTableOfSymbolsElement() {
        return lexicalAnalyzer.tableOfSymbols.get(counter);
    }

}
