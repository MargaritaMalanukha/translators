package com.lab.syntaxAnalyzer;

import com.lab.lexicalAnalyzer.LexicalAnalyzer;
import com.lab.lexicalAnalyzer.pojo.LexerData;
import com.lab.lexicalAnalyzer.pojo.ValueData;
import com.lab.syntaxAnalyzer.exceptions.ParserException;


/**
 * Изменения:
 * 1. Синтаксичный анализатор полностью соответствует грамматике.
 * 2. Переделан Assign как Type Ident | Ident.
 * 3. Переделан Expression как Expression = ArithmExpression | BoolExpression.
 */
public class SyntaxAnalyzer {

    private boolean FSuccess = true;

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

    public boolean postfixTranslator() {
        if (lexicalAnalyzer.isLexerSuccessful()) {
            return parseProgram();
        }
        return false;
    }

    /**
     * first level function
     */

    // Program = program ProgName ProgBody
    // ProgName = Ident
    // Ident = Letter {Letter | Digit}
    // ProgBody = ’{’ StatementList ’}’

    private boolean parseProgram(){
        System.out.println(indent1 + "parseProgram():");
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
            FSuccess = false;
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Parser ERROR: No closing braces found!");
            System.out.println("Parser success status: 0.");
            FSuccess = false;
        }
        return FSuccess;
    }

    /**
     * main parser
     */

    private boolean parseToken(String lexeme, String token, String indent) throws ParserException {
        if (counter > lexicalAnalyzer.tableOfSymbols.size()) {
            failParse(counter, lexeme, token);
        }

        LexerData lexerData = getTableOfSymbolsElement();

        if (lexerData.getLexeme().equals(lexeme) && lexerData.getToken().equals(token)) {
            counter++;
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

    // StatementList = Statement { Statement }
    private void parseStatementList() throws ParserException {
        System.out.println(indent2 + "parseStatementList():");
        boolean isStatementListEmpty = true;
        while (parseStatement()) {
            isStatementListEmpty = false;
        }
        if (isStatementListEmpty) {
            LexerData lexerData = getTableOfSymbolsElement();
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    /**
     * third level function
     */

    // Statement = (Assign | Inp | Out | ForStatement | IfStatement) ‘;’
    private boolean parseStatement() throws ParserException {
        System.out.println(indent3 + "parseStatement(): ");
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getToken().equals("id") || lexerData.getLexeme().equals("int")
        || lexerData.getLexeme().equals("real") || lexerData.getLexeme().equals("bool")) {
            parseAssign();
            parseToken(";", "punct", indent3);
            return true;
        } /*else if (lexerData.getLexeme().equals("read")) {
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
        }*/ else {
            return false;
        }
    }

    /**
     * fourth level functions
     */

    // Assign = (Type Ident | Ident) ’=’ Expression
    private void parseAssign() throws ParserException {
        try {
            parseType();
        } catch (ParserException ignored) { }

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

    // Inp = read ’(’ IdentList ’)’
    private void parseRead() throws ParserException {
        System.out.println(indent4 + "parseRead():");
        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("(")) {
            counter++;
            parseIdentList();
            parseToken(")", "brackets_op", indent4);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // Out = print ’(’ IdentList ’)’
    private void parsePrint() throws ParserException {
        System.out.println(indent4 + "parsePrint():");
        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("(")) {
            counter++;
            parseIdentList();
            parseToken(")", "brackets_op", indent4);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // ForStatement = for ‘(’ IndExpr; BoolExpr; ArithmExpr ‘)’ DoBlock
    private void parseFor() throws ParserException {
        System.out.println(indent4 + "parseFor():");
        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("(")) {
            counter++;
            printLine(lexerData, indent4);
            parseIndExpr();
            parseToken(";", "punct", indent4);
            parseBoolExpr();
            parseToken(";", "punct", indent4);
            parseArithmExpr();
            parseToken(")", "brackets_op", indent4);
            parseDoBlock();
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }
    //IfStatement = if ‘(‘ BoolExpr ‘)’ then DoBlock fi
    private void parseIf() throws ParserException {
        System.out.println(indent4 + "parseIf():");
        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("(")) {
            counter++;
            printLine(lexerData, indent4);
            parseBoolExpression();
            parseToken(")", "brackets_op", indent4);
            parseToken("then", "keyword", indent4);
            parseDoBlock();
            parseToken("fi", "keyword", indent4);
        }
    }

    /**
     * fifth level functions
     */

    // Type = int | real | bool
    private void parseType() throws ParserException {
        System.out.println(indent5 + "parseType():");
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("int") || lexerData.getLexeme().equals("real")
                || lexerData.getLexeme().equals("bool")) {
            counter++;
            printLine(lexerData, indent5);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // Expression = ArithmExpression | BoolExpression
    private void parseExpression() throws ParserException {
        System.out.println(indent5 + "parseExpression():");
        int returnCounterIfParseArithmIsWrong = counter;
        parseArithmExpression();
        LexerData lexerData = getTableOfSymbolsElement();
        if (!lexerData.getLexeme().equals(";")) {
            counter = returnCounterIfParseArithmIsWrong;
            System.out.println(indent5 + "parseExpression: impossible to parse ArithmExpression. Parsing BoolExpression...");
            parseBoolExpression();
        }
    }

    // IdentList = Ident {’,’ Ident}
    private void parseIdentList() throws ParserException {
        System.out.println(indent5 + "parseIdentList():");
        LexerData lexerData = getTableOfSymbolsElement();
        int iteration = 0;
        LexerData finalLexerData = lexerData;
        while (lexerData.getToken().equals("id") || lexicalAnalyzer.values
                .stream().anyMatch(e -> e.getValue().equals(finalLexerData.getLexeme()))) {
            iteration++;
            counter++;
            printLine(lexerData, indent5);

            lexerData = getTableOfSymbolsElement();
            if (!lexerData.getLexeme().equals(",")) {
                break;
            }
            counter++;
            printLine(lexerData, indent5);

            lexerData = getTableOfSymbolsElement();
        }
        if (iteration == 0) failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
    }

    // DoBlock = Statement | ’{’ StatementList ’}’
    private void parseDoBlock() throws ParserException {
        System.out.println(indent5 + "parseDoBlock():");
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("{")) {
            counter++;
            printLine(lexerData, indent5);
            parseStatementList();
            parseToken("}", "braces_op", indent5);
            return;
        }
        parseStatement();
    }

    // IndExpr = Type Ident ’=’ ArithmExpression
    private void parseIndExpr() throws ParserException {
        System.out.println(indent5 + "parseIndExpr():");
        parseType();
        LexerData lexerData = getTableOfSymbolsElement();
        if (lexerData.getToken().equals("id")) {
            counter++;
            printLine(lexerData, indent5);
            parseToken("=", "assign_op", indent5);
            parseArithmExpression();
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }

    }

    // BoolExpr = Ident to ArithmExpression
    private void parseBoolExpr() throws ParserException {
        System.out.println(indent5 + "parseBoolExpr():");
        LexerData lexerData;
        lexerData = getTableOfSymbolsElement();
        parseToken(lexerData.getLexeme(), "id", indent5);
        parseToken("to", "keyword", indent5);
        if (!parseArithmExpression()){
            lexerData = getTableOfSymbolsElement();
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // ArithmExpr = Ident by Ident AddOp | MultOp ArithmExpression
    private void parseArithmExpr() throws ParserException {
        System.out.println(indent5 + "parseArithmExpr():");
        LexerData lexerData = getTableOfSymbolsElement();
        parseToken(lexerData.getLexeme(), "id", indent5);
        parseToken("by", "keyword", indent5);
        parseToken(lexerData.getLexeme(), "id", indent5);

        lexerData = getTableOfSymbolsElement();
        if (lexerData.getToken().equals("add_op") || lexerData.getToken().equals("mult_op")) {
            counter++;
            printLine(lexerData, indent5);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }

        if (!parseArithmExpression()){
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    /**
     * sixth+ level functions
     */

    // ArithmExpression = [Sign] Term | ArithmExpression { AddOp Term }
    // Sign = '-'
    private boolean parseArithmExpression() {
        try {
            System.out.println(indent6 + "parseArithmExpression():");
            try {
                parseToken("-", "add_op", indent6);
            } catch (ParserException ignored) { }

            int returnCounterIfParseArithmIsWrong = counter;
            try {
                parseTerm();
            } catch (ParserException e) {
                counter = returnCounterIfParseArithmIsWrong;
                if (!parseArithmExpression()) {
                    LexerData lexerData = getTableOfSymbolsElement();
                    failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
                }
            }

            LexerData lexerData = getTableOfSymbolsElement();
            while (true) {
               if (lexerData.getToken().equals("add_op")) {
                   counter++;
                   printLine(lexerData, indent6);
                   parseTerm();
                   lexerData = getTableOfSymbolsElement();
               } else {
                   break;
               }
            }
        } catch (ParserException e) {
            return false;
        }
        return true;
    }

    // BoolExpression = BoolConst | (ArithmExpression RelOp ArithmExpression {BoolOp BoolExpression})
    private void parseBoolExpression() throws ParserException {

        System.out.println(indent6 + "parseBoolExpression():");
        LexerData lexerData = getTableOfSymbolsElement();

        if (lexerData.getToken().equals("boolval")) {
            printLine(lexerData, indent6);
            counter++;
        } else if (parseArithmExpression()){
            lexerData = getTableOfSymbolsElement();

            if (!lexerData.getToken().equals("rel_op")) {
                failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
            }
            counter++;
            printLine(lexerData, indent6);
            if (!parseArithmExpression()) {
                lexerData = getTableOfSymbolsElement();
                failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
            }
            lexerData = getTableOfSymbolsElement();

            while (lexerData.getToken().equals("bool_op")) {
                counter++;
                printLine(lexerData, indent6);
                parseBoolExpression();
                lexerData = getTableOfSymbolsElement();
            }
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // Term = Factor { MultOp | ExpOp Factor }
    private void parseTerm() throws ParserException {
        System.out.println(indent7 + "parseTerm():");
        parseFactor();
        while (true) {
            LexerData lexerData = getTableOfSymbolsElement();
            if (lexerData.getToken().equals("mult_op") || lexerData.getToken().equals("exp_op")) {
                counter++;
                printLine(lexerData, indent7);
                parseFactor();
            } else {
                break;
            }
        }
    }

    // Factor = Ident | Literal | ’(’ ArithmExpression ’)’
    private void parseFactor() throws ParserException {
        System.out.println(indent8 + "parseFactor():");
        LexerData lexerData = getTableOfSymbolsElement();

        if ((lexerData.getToken().equals("id"))
                || lexicalAnalyzer.values.contains(new ValueData(counter, lexerData.getLexeme()))) {
            counter++;
            printLine(lexerData, indent8);
        } else if (lexerData.getLexeme().equals("(")) {
            counter++;
            printLine(lexerData, indent8);
            System.out.println(parseArithmExpression());
            System.out.println(getTableOfSymbolsElement().getLexeme());
            parseToken(")", "brackets_op", indent8);
        } else {

            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    /**
     * Tools
     */

    private void printLine(LexerData lexerData, String indent) {
        System.out.println(indent + "parseToken: in line " + lexerData.getNumLine() +
                " (lexeme, token): (" + lexerData.getLexeme() +
                ", " + lexerData.getToken() + ")");
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
