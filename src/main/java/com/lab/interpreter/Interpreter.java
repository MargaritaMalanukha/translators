package com.lab.interpreter;

import com.lab.interpreter.exceptions.PostfixException;
import com.lab.interpreter.pojo.PostfixData;
import com.lab.lexicalAnalyzer.pojo.IdentifierData;
import com.lab.lexicalAnalyzer.pojo.LexerData;
import com.lab.lexicalAnalyzer.pojo.ValueData;
import com.lab.syntaxAnalyzer.SyntaxAnalyzer;
import com.lab.syntaxAnalyzer.exceptions.ParserException;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;

public class Interpreter {

    private final SyntaxAnalyzer syntaxAnalyzer;
    private final ArrayList<IdentifierData> identifiers;
    private final ArrayList<ValueData> values;

    public final ArrayList<String> outputList;

    private Stack<LexerData> stack;

    private int instrNum = 0; //текущий номер инструкции

    public Interpreter(SyntaxAnalyzer syntaxAnalyzer) {
        this.syntaxAnalyzer = syntaxAnalyzer;
        this.identifiers = syntaxAnalyzer.getLexicalAnalyzer().identifiers;
        this.values = syntaxAnalyzer.getLexicalAnalyzer().values;
        stack = new Stack<>();
        outputList = new ArrayList<>();
    }

    public boolean postfixProcessing() {
        try {
            int nextInstr, numberOfSteps = 0;
            int size = syntaxAnalyzer.postfixCode.size();
            while (instrNum < size) {
                if (numberOfSteps > 10000) {
                    throw new PostfixException("Cannot interpreter such a large number of steps!");
                }

                LexerData lexerData = syntaxAnalyzer.postfixCode.get(instrNum);
                numberOfSteps++;
                String token = lexerData.getToken();
                if (token.equals("id") || token.equals("real") || token.equals("int") || token.equals("boolval") || token.equals("label")) {
                    stack.push(lexerData);
                    nextInstr = instrNum + 1;
                } else if (token.equals("jump") || token.equals("jf") || token.equals("colon")) {
                    nextInstr = doJumps(lexerData);
                } else if (token.equals("OUT") || token.equals("INP")) {
                    processingInputOutput(lexerData);
                    nextInstr = instrNum + 1;
                } else {
                    doIt(lexerData);
                    nextInstr = instrNum + 1;
                }
                configToPrint(instrNum, lexerData);
                instrNum = nextInstr;
            }
            System.out.println("Total steps operated: " + numberOfSteps);
            System.out.println("Interpreter success status: 1.");
        } catch (RuntimeException e) {
            System.out.println("Interpreter success status: 0.");
            return false;
        } catch (PostfixException e) {
            System.out.println(e.getMessage());
            System.out.println("Interpreter success status: 0.");
            return false;
        }
        return true;
    }

    private void processingInputOutput(LexerData lexerData) throws PostfixException {
        switch (lexerData.getToken()) {
            case "INP": {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                String token = defineType(input);
                stack.push(new LexerData(lexerData.getNumChar(), input, token, lexerData.getNumLine()));
                doIt(new LexerData("=", "assign_op"));
                break;
            }
            case "OUT": {
                LexerData lexIdentifier = stack.pop();
                IdentifierData identifier = identifiers.stream()
                        .filter(e -> e.getId().equals(lexIdentifier.getLexeme()))
                        .findFirst()
                        .orElseThrow(NoSuchElementException::new);
                outputList.add("\t" + identifier.getId() + "=" + identifier.getValue());
            }
        }
    }

    private String defineType(String input) throws PostfixException {
        if (input.equals("true") || input.equals("false")) {
            return "boolval";
        }
        try {
            Double.parseDouble(input);
            if (input.contains(".")) {
                return "real";
            } else return "int";
        } catch (Exception e) {
            LexerData lexerData = new LexerData();
            lexerData.setLexeme(input);
            failRunTime("wrong input", lexerData, new LexerData(), new LexerData());
        }
        return "";
    }

    private int doJumps(LexerData lexerData) throws PostfixException {
        int nextInstr = 0;
        switch (lexerData.getToken()) {
            case "jump":
                nextInstr = processing_JUMP();
                break;
            case "colon":
                nextInstr = processing_colon();
                break;
            case "jf":
                nextInstr = processing_JF();
                break;
        }
        return nextInstr;
    }

    private int processing_JUMP() throws PostfixException {
        //снимаем с вершины стека элемент
        LexerData lexerData = stack.pop();
        //если это метка и она существует, следующая инструкция равна её значению
        int nextInstr = 0;
        if (lexerData.getToken().equals("label")) {
            nextInstr = syntaxAnalyzer.tableOfLabels.stream()
                    .filter(e -> e.getLabel().equals(lexerData.getLexeme()))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new)
                    .getValue();
        } else {
            failRunTime("not a label", lexerData, new LexerData(), new LexerData());
        }
        return nextInstr;
    }

    private int processing_colon() {
        //снимаем с вершины стека метку
        stack.pop();
        //увеличиваем номер текущей инструкции на 1
        return instrNum + 1;
    }

    private int processing_JF() throws PostfixException {
        //метка
        LexerData label = stack.pop();
        //результат вычисления BoolExpression
        LexerData bool = stack.pop();

        if (bool.getToken().equals("boolval") || bool.getToken().equals("id")) {
            String value;
            //если результат BoolExpression - идентификатор, то находим его значение в таблице
            if (bool.getToken().equals("id")) {
                value = identifiers.stream()
                        .filter(e -> e.getId().equals(bool.getLexeme()))
                        .findFirst()
                        .orElseThrow(NoSuchElementException::new)
                        .getValue();
            }
            //если это - булевое значение, то оно и является значением
            else {
                value = bool.getLexeme();
            }

            //если значение = false, то следующая инструкция - значение метки
            if (value.equals("false")) {
                return syntaxAnalyzer.tableOfLabels.stream()
                        .filter(e -> e.getLabel().equals(label.getLexeme()))
                        .findFirst()
                        .orElseThrow(NoSuchElementException::new)
                        .getValue();
            }
            //если значение = true, то следующая инструкция - текущая + 1
            else if (value.equals("true")) {
                return instrNum + 1;
            } else {
                failRunTime("not a boolval", label, new LexerData(), new LexerData());
            }
        } else {
            failRunTime("not a boolval", label, new LexerData(), new LexerData());
        }
        return 0;
    }

    private void doIt(LexerData lexerData) throws PostfixException {
        if (lexerData.getLexeme().equals("=")) {
            //получаем вычисленное значение
            LexerData lexValue = stack.pop();
            //получаем идентификатор
            LexerData identifier = stack.pop();

            boolean isChanged = false;

            for (int i = 0; i < identifiers.size(); i++) {
                if (identifiers.get(i).getId().equals(identifier.getLexeme())) {
                    IdentifierData identifierData = identifiers.get(i);

                    if (lexValue.getToken().equals("id")) {
                        LexerData finalLexValue = lexValue;
                        IdentifierData idToAssign = identifiers.stream()
                                .filter(e -> e.getId().equals(finalLexValue.getLexeme()))
                                .findFirst().orElseThrow(NoSuchElementException::new);
                        lexValue = new LexerData(idToAssign.getValue(), idToAssign.getType());
                    }

                    //если идентификатор и значение имеют разный тип, но при этом это не real и int
                    if (!identifierData.getType().equals(lexValue.getToken()) && !(identifierData.getType().equals("real") && lexValue.getToken().equals("int"))
                    && !lexValue.getToken().equals("id")) {
                        LexerData lexerData1 = new LexerData();
                        lexerData1.setToken(identifierData.getType());
                        failRunTime("wrong type", lexerData1, lexValue, lexerData);
                    }

                    identifierData.setValue(lexValue.getLexeme());
                    isChanged = true;
                }
            }
            if (!isChanged) {
                identifiers.add(new IdentifierData(identifier.getLexeme(), identifier.getNumChar(), lexValue.getToken(), lexValue.getLexeme()));
            }
        } else if (lexerData.getToken().equals("add_op") || lexerData.getToken().equals("mult_op") || lexerData.getToken().equals("exp_op")) {
            //получаем 2 значения
            LexerData valueRight = stack.pop();
            LexerData valueLeft = stack.pop();

            if (valueRight.getToken().equals("boolval") || valueLeft.getToken().equals("boolval")) {
                failRunTime("wrong type", valueLeft, valueRight, lexerData);
            } else {
                processingAddMultExpOp(valueRight, valueLeft, lexerData);
            }
        } else if (lexerData.getToken().equals("rel_op") || lexerData.getToken().equals("bool_op")) {
            //получаем 2 булевых значения
            LexerData boolValueRight = stack.pop();
            LexerData boolValueLeft = stack.pop();

            processingRelBoolOp(boolValueRight, boolValueLeft, lexerData);
        }
    }

    private void processingAddMultExpOp(LexerData rightOp, LexerData leftOp, LexerData operation) throws PostfixException {
        PostfixData rightLexeme, leftLexeme;
        rightLexeme = getCorrectPostfixData(rightOp);
        leftLexeme = getCorrectPostfixData(leftOp);
        getValue(rightLexeme, leftLexeme, operation);
    }

    private void processingRelBoolOp(LexerData rightOp, LexerData leftOp, LexerData operation) throws PostfixException {
        PostfixData rightLexeme, leftLexeme;
        rightLexeme = getCorrectPostfixData(rightOp);
        leftLexeme = getCorrectPostfixData(leftOp);
        getBoolValue(rightLexeme, leftLexeme, operation);
    }

    private void getValue(PostfixData rightLexeme, PostfixData leftLexeme, LexerData operation) throws PostfixException {
        double right, left;

        left = parseRealIntValue(leftLexeme);
        right = parseRealIntValue(rightLexeme);

        double result = 0;

        if (operation.getLexeme().equals("+")) {
            result = left + right;
        } else if (operation.getLexeme().equals("-")) {
            result = left - right;
        } else if (operation.getLexeme().equals("*")) {
            result = left * right;
        } else if (operation.getLexeme().equals("/") && right == 0) {
            LexerData leftL = new LexerData();
            LexerData rightL = new LexerData();

            leftL.setLexeme(leftLexeme.getValue());
            rightL.setLexeme(rightLexeme.getValue());

            failRunTime("zero division", leftL, rightL, operation);
        } else if (operation.getLexeme().equals("/")) {
            result = left / right;
        } else if (operation.getLexeme().equals("^")) {
            result = Math.pow(left, right);
        }

        String token = "";
        if (rightLexeme.getType().equals("int") && leftLexeme.getType().equals("int") && !operation.getLexeme().equals("/")) {
            token = "int";
            int resultI = (int) result;
            stack.push(new LexerData(0, resultI + "", token, 0));
            toTableOfValues(resultI + "", token);
        } else {
            token = "real";
            stack.push(new LexerData(0, result + "", token, 0));
            toTableOfValues(result + "", token);
        }

    }

    private void getBoolValue(PostfixData rightLexeme, PostfixData leftLexeme, LexerData operation) throws PostfixException {
        boolean result = false;
        if (operation.getToken().equals("rel_op")) {
            double right, left;

            right = parseRealIntValue(rightLexeme);
            left = parseRealIntValue(leftLexeme);

            if (operation.getLexeme().equals("<")) {
                result = left < right;
            } else if (operation.getLexeme().equals(">")) {
                result = left > right;
            } else if (operation.getLexeme().equals("==")) {
                result = left == right;
            } else if (operation.getLexeme().equals("!=")) {
                result = left != right;
            } else if (operation.getLexeme().equals("<=")) {
                result = left <= right;
            } else if (operation.getLexeme().equals(">=")) {
                result = left >= right;
            }
        } else if (operation.getToken().equals("bool_op")) {
            boolean right, left;

            right = parseBoolValue(rightLexeme);
            left = parseBoolValue(leftLexeme);

            if (operation.getLexeme().equals("||")) {
                result = left || right;
            } else if (operation.getLexeme().equals("&&")) {
                result = left && right;
            }
        }

        String token = "boolval";
        stack.push(new LexerData(0, result + "", token, 0));
        toTableOfValues(result + "", token);
    }

    private double parseRealIntValue(PostfixData postfixData) throws PostfixException {

        double result = 0;
        if (postfixData.getType().equals("int")) {
            try {
                result = Integer.parseInt(postfixData.getValue());
            } catch (NumberFormatException e) {
                failRunTime("parse error", new LexerData(0, postfixData.getValue(), postfixData.getType(), 0), new LexerData(), new LexerData());
            }
        } else if (postfixData.getType().equals("real")) {
            try {
                result = Double.parseDouble(postfixData.getValue());
            } catch (NumberFormatException e) {
                failRunTime("parse error", new LexerData(0, postfixData.getValue(), postfixData.getType(), 0), new LexerData(), new LexerData());
            }
        } else {
            failRunTime("parse error", new LexerData(0, postfixData.getValue(), postfixData.getType(), 0), new LexerData(), new LexerData());
        }
        return result;
    }

    private boolean parseBoolValue(PostfixData postfixData) throws PostfixException {
        boolean result = false;
        if (postfixData.getType().equals("boolval")) {
            try {
                result = Boolean.parseBoolean(postfixData.getValue());
            } catch (NumberFormatException e) {
                failRunTime("parse error", new LexerData(0, postfixData.getValue(), postfixData.getType(), 0), new LexerData(), new LexerData());
            }
        } else {
            failRunTime("parse error", new LexerData(0, postfixData.getValue(), postfixData.getType(), 0), new LexerData(), new LexerData());
        }
        return result;
    }

    private PostfixData getCorrectPostfixData(LexerData lexerData) throws PostfixException {
        PostfixData postfixData = new PostfixData();
        if (lexerData.getToken().equals("id")) {
            IdentifierData id = identifiers.stream()
                    .filter(e -> e.getId().equals(lexerData.getLexeme()))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
            if (id.getType().equals("undefined")) {
                failRunTime("not initialized variable", new LexerData(), new LexerData(), lexerData);
            } else {
                postfixData = new PostfixData(id.getId(), id.getType(), id.getValue());
            }
        } else {
            ValueData value = values.stream()
                    .filter(e -> e.getConst().equals(lexerData.getLexeme()))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
            postfixData = new PostfixData(value.getConst(), value.getType(), value.getValue());
        }
        return postfixData;
    }

    private void configToPrint(int step, LexerData lexerData) {
        if (step == 0) {
            System.out.println("Interpreter started working");
            System.out.println("Table of identifiers: ");
            identifiers.forEach(System.out::println);
            System.out.println("Table of values: ");
            values.forEach(System.out::println);
        }

        System.out.println("Step #" + step + " of interpretation");
        if (lexerData.getToken().equals("int") || lexerData.getToken().equals("float") || lexerData.getToken().equals("boolval")) {
            System.out.println("\tLexeme " + lexerData.getLexeme() + " added to values table: (" +
                    values.stream().filter(e -> e.getConst().equals(lexerData.getLexeme())).findFirst().orElse(new ValueData()).toString() + ").");
        } else if (lexerData.getToken().equals("id")) {
            System.out.println("\tLexeme " + lexerData.getLexeme() + " added to identifiers table: (" +
                    identifiers.stream().filter(e -> e.getId().equals(lexerData.getLexeme())).findFirst().orElse(new IdentifierData()).toString() + ").");
        } else {
            System.out.println("\tLexeme " + lexerData.getLexeme() + " was processed.");
        }

        if (step == syntaxAnalyzer.postfixCode.size()-1) {
            System.out.println("Interpreter finished working");
            System.out.println("Table of identifiers: ");
            identifiers.forEach(System.out::println);
            System.out.println("Table of values: ");
            values.forEach(System.out::println);
        }

    }

    private void failRunTime(String error, LexerData leftExpression, LexerData rightExpression, LexerData lexemeToApply) throws PostfixException {
        String message = "Undefined exception occured!";
        switch (error) {
            case "wrong type": {
                message = "Cannot apply operand '" + lexemeToApply.getLexeme() + "' to types " + leftExpression.getToken() + " and "
                        + rightExpression.getToken() + ".";
                break;
            }
            case "not initialized variable": {
                message = "Variable '" + lexemeToApply.getLexeme() + "' might not have been initialized.";
                break;
            }
            case "zero division": {
                message = "Zero division occured: " + leftExpression.getLexeme() + lexemeToApply.getLexeme() + rightExpression.getLexeme() + ".";
                break;
            }
            case "parse error": {
                message = "Cannot parse value '" + leftExpression.getLexeme() + "' with token '" + leftExpression.getToken() + "'.";
                break;
            }
            case "not a label": {
                message = "Cannot interpreter as a label lexeme '" + leftExpression.getLexeme() + "' with token '" + leftExpression.getToken() + "'.";
                break;
            }
            case "not a boolval": {
                message = "Cannot interpreter as a bool value lexeme '" + leftExpression.getLexeme() + "' with token '" + leftExpression.getToken() + "'.";
                break;
            } case "wrong input": {
                message = "Cannot interpreter your input '" + leftExpression.getLexeme() + "'.";
                break;
            }
        }
        throw new PostfixException(message);
    }

    private void toTableOfValues(String value, String type) {
        long count = values.stream().filter(e -> e.getConst().equals(value)).count();
        if (count == 0){
            values.add(new ValueData(value, 0, type, value));
        }
    }
}
