package com.lab.syntaxAnalyzer;

import com.lab.lexicalAnalyzer.LexicalAnalyzer;
import com.lab.lexicalAnalyzer.pojo.IdentifierData;
import com.lab.lexicalAnalyzer.pojo.LabelData;
import com.lab.lexicalAnalyzer.pojo.LexerData;
import com.lab.lexicalAnalyzer.pojo.ValueData;
import com.lab.syntaxAnalyzer.exceptions.ParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;


/**
 * Лабораторная №3:
 * 1. ArithmExpression = [Sign] Term { AddOp Term }, решена проблема со скобками.
 * 2. Написание транслятора для функций BoolExpression, ArithmExpression (Term, Factor)
 */
public class SyntaxAnalyzer {

    private boolean FSuccess = true;

    public ArrayList<LexerData> postfixCode;

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

    public ArrayList<IdentifierData> reservedIdentifiers;
    //используется для переноса постфиксного кода из BoolExpr в ArithmExpr
    private final ArrayList<LexerData> boolExprPostfixCode;
    //используется для переноса переменной из IndExpr в постфиксный код ArithmExpr
    private LexerData prm;
    //используется для переноса exp_op в постфиксный код позже, для формирования правоасоциативной операции
    private ArrayList<LexerData> expOpPostfixCode;

    public ArrayList<LabelData> tableOfLabels;

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        postfixCode = new ArrayList<>();
        tableOfLabels = new ArrayList<>();
        reservedIdentifiers = new ArrayList<>();
        boolExprPostfixCode = new ArrayList<>();
        expOpPostfixCode = new ArrayList<>();
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
        } else {
            return false;
        }
    }

    /**
     * fourth level functions
     */

    // Assign = (Type Ident | Ident) ’=’ Expression
    private void parseAssign() throws ParserException {
        System.out.println(indent4 + "parseAssign():");
        try {
            parseType();
        } catch (ParserException ignored) { }


        LexerData lexerData = getTableOfSymbolsElement();
        counter++;
        printLine(lexerData, indent4);
        //Добавить Ident в ПОЛИЗ
        postfixCode.add(lexerData);

        lexerData = getTableOfSymbolsElement();
        if (lexerData.getLexeme().equals("=")) {
            counter++;
            printLine(lexerData, indent4);
            parseExpression();
            //Добавить AssignOp в ПОЛИЗ
            postfixCode.add(lexerData);
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
            parseIdentList("INP"); //трансляція
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
            parseIdentList("OUT");//трансляція
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
            parseIndExpr(); //трансляция
            parseToken(";", "punct", indent4);
            parseBoolExpr(); //трансляция
            parseToken(";", "punct", indent4);
            parseArithmExpr(); //трансляция

            LabelData m1 = tableOfLabels.get(tableOfLabels.size() - 3);
            LabelData m3 = tableOfLabels.get(tableOfLabels.size() - 1);

            parseToken(")", "brackets_op", indent4);
            parseDoBlock();

            //трансляция parseFor() завершена
            //добавляем в ПОЛИЗ следующие инструкции:
            //m1 JUMP m3 :
            postfixCode.add(new LexerData(m1.getLabel(), "label"));
            postfixCode.add(new LexerData("JUMP", "jump"));
            setValLabel(m3);
            postfixCode.add(new LexerData(m3.getLabel(), "label"));
            postfixCode.add(new LexerData(":", "colon"));
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }
    //IfStatement = if ‘(‘ BoolExpression ‘)’ then DoBlock fi
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

            //добавляем метку для DoBlock (m1 JF)
            lexerData = getTableOfSymbolsElement();
            LabelData labelData = createLabel();
            postfixCode.add(new LexerData(lexerData.getNumLine(), labelData.getLabel(), "label", lexerData.getNumChar()));
            postfixCode.add(new LexerData(lexerData.getNumLine(), "JF", "jf", lexerData.getNumChar()));

            parseDoBlock();
            parseToken("fi", "keyword", indent4);

            //добавляем метку для выхода из If (m1 :)
            lexerData = getTableOfSymbolsElement();
            setValLabel(labelData);
            postfixCode.add(new LexerData(lexerData.getNumLine(), labelData.getLabel(), "label", lexerData.getNumChar()));
            postfixCode.add(new LexerData(lexerData.getNumLine(), ":", "colon", lexerData.getNumChar()));

        }
    }

    private LabelData createLabel() throws ParserException {
        LabelData labelData;
        int number = tableOfLabels.size() + 1;
        String lexeme = "m" + number;
        int value = tableOfLabels.stream().filter(e -> e.getLabel().equals(lexeme)).findFirst().orElse(new LabelData()).getValue();
        if (value == 0) {
            labelData = new LabelData(lexeme, 0);
            tableOfLabels.add(labelData);
        } else {
            throw new ParserException("Label error!");
        }
        return labelData;
    }

    private void setValLabel(LabelData label) {
        tableOfLabels.stream()
                .filter(e -> e.getLabel().equals(label.getLabel()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new)
                .setValue(postfixCode.size());
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

            //задаем переменной следующей за типом заданный тип
            LexerData lexerData1 = getTableOfSymbolsElement();
            IdentifierData identifierData = lexicalAnalyzer.identifiers.stream()
                    .filter(e -> e.getId().equals(lexerData1.getLexeme())).findFirst().orElseThrow(NoSuchElementException::new);
            identifierData.setType(lexerData.getLexeme());

            if (lexerData.getLexeme().equals("bool")) {
                identifierData.setType("boolval");
            }

            printLine(lexerData, indent5);
        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
    }

    // Expression = ArithmExpression | BoolExpression
    private void parseExpression() throws ParserException {
        System.out.println(indent5 + "parseExpression():");
        int returnCounterIfParseArithmIsWrong = counter;
        int indexCounter = postfixCode.size();

        boolean isArithmCorrect = parseArithmExpression();

        LexerData lexerData = getTableOfSymbolsElement();
        if (!lexerData.getLexeme().equals(";") || !isArithmCorrect) {
            //Откат ArithmExpression
            counter = returnCounterIfParseArithmIsWrong;
            for (int i = indexCounter; i < postfixCode.size(); i++) {
                postfixCode.remove(i);
            }
            System.out.println(indent5 + "parseExpression: impossible to parse ArithmExpression. Parsing BoolExpression...");
            parseBoolExpression();
        }
    }

    // IdentList = Ident {’,’ Ident}
    //функция используется только в read() & print()
    private void parseIdentList(String postfixLexeme) throws ParserException {
        System.out.println(indent5 + "parseIdentList():");
        LexerData lexerData = getTableOfSymbolsElement();
        int iteration = 0;
        while (lexerData.getToken().equals("id")) {
            iteration++;
            counter++;
            printLine(lexerData, indent5);

            postfixCode.add(lexerData);
            postfixCode.add(new LexerData(lexerData.getNumLine(), postfixLexeme, postfixLexeme, lexerData.getNumChar()));

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
            //обновляем глобальную переменную для идентификации параметра в других частях кода
            prm = lexerData;
            //добавляем идентификатор в ПОЛИЗ
            postfixCode.add(lexerData);
            parseToken("=", "assign_op", indent5);
            parseArithmExpression(); //трансляція
            //добавляем '=' в ПОЛИЗ
            postfixCode.add(new LexerData("=", "assign_op"));
            //парсинг IndExpr завершен
            //добавляем ПОЛИЗ r1 1 = m1 : r2
            IdentifierData identifier1 = createReservedIdentifier();
            postfixCode.add(new LexerData(identifier1.getId(), "id"));
            postfixCode.add(createValue("1", "int"));
            postfixCode.add(new LexerData("=", "assign_op"));

            LabelData labelData = createLabel();
            setValLabel(labelData);
            postfixCode.add(new LexerData(labelData.getLabel(), "label"));
            postfixCode.add(new LexerData(":", "colon"));

            IdentifierData identifier2 = createReservedIdentifier();
            postfixCode.add(new LexerData(identifier2.getId(), "id"));

        } else {
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }

    }

    // BoolExpr = ArithmExpression RelOp ArithmExpression
    private void parseBoolExpr() throws ParserException {
        System.out.println(indent5 + "parseBoolExpr():");

        //переносим ПОЛИЗ BoolExpr на более позднюю стадию (после ArithmExpr)
        int startId = postfixCode.size();

        LexerData lexerData = getTableOfSymbolsElement();
        if (!parseArithmExpression()) { //трансляция
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }

        LexerData relOp = getTableOfSymbolsElement();
        parseToken(relOp.getLexeme(), "rel_op", indent5);

        if (!parseArithmExpression()){ //трансляция
            lexerData = getTableOfSymbolsElement();
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }
        //добавляем RelOp в ПОЛИЗ
        postfixCode.add(relOp);

        //переносим ПОЛИЗ BoolExpr на более позднюю стадию (после ArithmExpr)
        int num = postfixCode.size() - startId;
        for (int i = 0; i < num; i++) {
            LexerData removed = postfixCode.remove(postfixCode.size() - 1);
            boolExprPostfixCode.add(removed);
        }
        Collections.reverse(boolExprPostfixCode);
    }

    private IdentifierData createReservedIdentifier() throws ParserException {
        IdentifierData identifierData;
        String lexeme = "r" + (reservedIdentifiers.size() + 1);
        IdentifierData data = lexicalAnalyzer.identifiers
                .stream().filter(e -> e.getId().equals(lexeme))
                .findFirst()
                .orElse(null);
        if (data == null) {
            identifierData = new IdentifierData(lexeme, 0);
            identifierData.setType("int");
            identifierData.setValue("0");
            lexicalAnalyzer.identifiers.add(identifierData);
            reservedIdentifiers.add(identifierData);
        } else {
            throw new ParserException("Identifier error!");
        }
        return identifierData;
    }

    // ArithmExpr = ArithmExpression
    private void parseArithmExpr() throws ParserException {
        System.out.println(indent5 + "parseArithmExpr():");

        LexerData lexerData = getTableOfSymbolsElement();
        if (!parseArithmExpression()){//трансляция
            failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
        }

        //парсинг непосредственно ArithmExpr завершен
        //добавляем следующий код в ПОЛИЗ:
        // = r1 0 == m2 JF Prm r2 = m2 : r1 0 = Prm

        IdentifierData identifier1 = getR1();
        IdentifierData identifier2 = getR2();

        postfixCode.add(new LexerData("=", "assign_op"));
        postfixCode.add(new LexerData(identifier1.getId(), "id"));
        postfixCode.add(createValue("0", "int"));
        postfixCode.add(new LexerData("==", "rel_op"));

        LabelData labelData = createLabel();
        postfixCode.add(new LexerData(labelData.getLabel(), "label"));
        postfixCode.add(new LexerData("JF", "jf"));
        postfixCode.add(prm);
        postfixCode.add(new LexerData(identifier2.getId(), "id"));
        postfixCode.add(new LexerData("=", "assign_op"));

        setValLabel(labelData);
        postfixCode.add(new LexerData(labelData.getLabel(), "label"));
        postfixCode.add(new LexerData(":", "colon"));
        postfixCode.add(new LexerData(identifier1.getId(), "id"));
        postfixCode.add(createValue("0", "int"));
        postfixCode.add(new LexerData("=", "assign_op"));
        postfixCode.add(prm);

        //добавляем в ПОЛИЗ BoolExpr
        postfixCode.addAll(boolExprPostfixCode);
        boolExprPostfixCode.clear();

        //добавляем следующий код в ПОЛИЗ:
        //m3 JF
        labelData = createLabel();
        postfixCode.add(new LexerData(labelData.getLabel(), "label"));
        postfixCode.add(new LexerData("JF", "jf"));

    }

    private LexerData createValue(String lexeme, String token) {
        ValueData valueData = new ValueData();
        valueData.setConst(lexeme);
        valueData.setType(token);
        valueData.setValue(lexeme);
        lexicalAnalyzer.values.add(valueData);
        return new LexerData(lexeme, token);
    }

    /**
     * sixth+ level functions
     */

    // ArithmExpression = [Sign] Term { AddOp Term }
    // Sign = '-'
    private boolean parseArithmExpression() {
        try {
            System.out.println(indent6 + "parseArithmExpression():");
            try {
                parseToken("-", "add_op", indent6);
            } catch (ParserException ignored) { }

            parseTerm();

            LexerData lexerData = getTableOfSymbolsElement();
            while (lexerData.getToken().equals("add_op")) {
                counter++;
                printLine(lexerData, indent6);
                parseTerm();

                //добавить AddOp в ПОЛИЗ
                postfixCode.add(lexerData);

                lexerData = getTableOfSymbolsElement();
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
            //добавить BoolConst в ПОЛИЗ
            postfixCode.add(lexerData);
        } else if (parseArithmExpression()){
            LexerData lexerDataRel = getTableOfSymbolsElement();

            if (!lexerDataRel.getToken().equals("rel_op")) {
                failParse(lexerDataRel.getNumLine(), lexerDataRel.getLexeme(), lexerDataRel.getToken());
            }
            counter++;
            printLine(lexerDataRel, indent6);

            if (!parseArithmExpression()) {
                lexerData = getTableOfSymbolsElement();
                failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
            }
            lexerData = getTableOfSymbolsElement();

            //добавить RelOp в ПОЛИЗ
            postfixCode.add(lexerDataRel);

            while (lexerData.getToken().equals("bool_op")) {
                counter++;
                printLine(lexerData, indent6);
                parseBoolExpression();

                //добавить BoolOp в ПОЛИЗ
                postfixCode.add(lexerData);
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
                //Добавить MultOp|ExpOp в ПОЛИЗ
                if (lexerData.getToken().equals("exp_op")) {
                    //переносим знак возведения в степень
                    expOpPostfixCode.add(lexerData);
                } else  {
                    postfixCode.addAll(expOpPostfixCode);
                    expOpPostfixCode.clear();
                    postfixCode.add(lexerData);
                }

            } else {
                break;
            }
        }
        postfixCode.addAll(expOpPostfixCode);
        expOpPostfixCode.clear();
    }

    // Factor = Ident | Literal | ’(’ ArithmExpression ’)’
    private void parseFactor() throws ParserException {
        System.out.println(indent8 + "parseFactor():");
        LexerData lexerData = getTableOfSymbolsElement();

        if ((lexerData.getToken().equals("id"))
                || lexicalAnalyzer.values.contains(new ValueData(lexerData.getLexeme(), lexerData.getToken(), counter))) {
            counter++;
            printLine(lexerData, indent8);
            //Добавить Ident|Value в ПОЛИЗ
            postfixCode.add(lexerData);
        } else if (lexerData.getLexeme().equals("(")) {
            counter++;
            printLine(lexerData, indent8);
            if (!parseArithmExpression()){
                lexerData = getTableOfSymbolsElement();
                failParse(lexerData.getNumLine(), lexerData.getLexeme(), lexerData.getToken());
            }
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

    private IdentifierData getR1() {
        return reservedIdentifiers.get(reservedIdentifiers.size() - 2);
    }

    private IdentifierData getR2() {
        return reservedIdentifiers.get(reservedIdentifiers.size() - 1);
    }

    public LexicalAnalyzer getLexicalAnalyzer() {
        return lexicalAnalyzer;
    }
}
