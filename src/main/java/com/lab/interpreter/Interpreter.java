package com.lab.interpreter;

import com.lab.interpreter.exceptions.PostfixException;
import com.lab.interpreter.pojo.PostfixData;
import com.lab.lexicalAnalyzer.pojo.IdentifierData;
import com.lab.lexicalAnalyzer.pojo.LexerData;
import com.lab.lexicalAnalyzer.pojo.ValueData;
import com.lab.syntaxAnalyzer.SyntaxAnalyzer;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Stack;

public class Interpreter {

    private final SyntaxAnalyzer syntaxAnalyzer;
    private final ArrayList<IdentifierData> identifiers;
    private final ArrayList<ValueData> values;

    private Stack<LexerData> stack;

    public Interpreter(SyntaxAnalyzer syntaxAnalyzer) {
        this.syntaxAnalyzer = syntaxAnalyzer;
        this.identifiers = syntaxAnalyzer.getLexicalAnalyzer().identifiers;
        this.values = syntaxAnalyzer.getLexicalAnalyzer().values;
        stack = new Stack<>();
    }

    public boolean postfixProcessing() {
        try {
            int i = 0;
            for (LexerData lexerData : syntaxAnalyzer.postfixCode) {
                String token = lexerData.getToken();
                if (token.equals("id") || token.equals("real") || token.equals("int") || token.equals("boolval")) {
                    stack.push(lexerData);
                } else doIt(lexerData);
                configToPrint(i++, lexerData);
            }
            System.out.println("Interpreter success status: 1.");
            stack.forEach(System.out::println);
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
                    //если идентификатор и значение имеют разный тип, но при этом это не real и int
                    if (!identifierData.getType().equals(lexValue.getToken()) && !(identifierData.getType().equals("real") && lexValue.getToken().equals("int"))) {
                        LexerData lexerData1 = new LexerData();
                        lexerData1.setToken(identifierData.getType());
                        failRunTime("wrong type", lexerData1, lexValue, lexerData);
                    }
                    identifierData.setType(lexValue.getToken());
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
        switch (error) {
            case "wrong type": {
                String message = "Cannot apply operand '" + lexemeToApply.getLexeme() + "' to types " + leftExpression.getToken() + " and "
                        + rightExpression.getToken() + ".";
                throw new PostfixException(message);
            }
            case "not initialized variable": {
                String message = "Variable '" + lexemeToApply.getLexeme() + "' might not have been initialized.";
                throw new PostfixException(message);
            }
            case "zero division": {
                String message = "Zero division occured: " + leftExpression.getLexeme() + lexemeToApply.getLexeme() + rightExpression.getLexeme() + ".";
                throw new PostfixException(message);
            }
            case "parse error": {
                String message = "Cannot parse value '" + leftExpression.getLexeme() + "' with token '" + leftExpression.getToken() + "'.";
                throw new PostfixException(message);
            }
        }
    }

    private void toTableOfValues(String value, String type) {
        long count = values.stream().filter(e -> e.getConst().equals(value)).count();
        if (count == 0){
            values.add(new ValueData(value, 0, type, value));
        }
    }



}
