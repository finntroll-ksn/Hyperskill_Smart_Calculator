package calculator;

import java.math.BigInteger;
import java.util.*;

public class Main {
    static String calculateNumbers(String input, List<String> vars, List<String> values) {
        char[] braces = input.toCharArray();
        int cnt = 0;

        for (char brace : braces) {
            if (brace == '(' || brace == ')') cnt++;
        }

        for (int i = 0; i < braces.length; i++) {
            if (braces[i] == '*' && braces[i + 1] == '*') {
                return "Invalid expression";
            } else if (braces[i] == '/' && braces[i + 1] == '/') {
                return "Invalid expression";
            }
        }

        if (cnt % 2 != 0) {
            return "Invalid expression";
        }


        String[] commands = input
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\+{2,}", "+")
                .split(" ");

        boolean varOrNot = false;

        if (vars.size() > 0) {
            for (String x : commands) {
                if (x.matches("[a-zA-Z]+")) {
                    varOrNot = true;
                    break;
                }
            }
        }

        if (varOrNot) {
            for (int i = 0; i < commands.length; i++) {
                if (commands[i].matches("[a-zA-Z]+")) {
                    for (int j = 0; j < vars.size(); j++) {
                        if (vars.get(j).equals(commands[i])) {
                            commands[i] = values.get(j);
                            break;
                        }
                    }
                }
            }
        }

        if (commands[0].matches("[a-zA-Z]+") || vars.size() == 1 && input.toLowerCase().equals(vars.get(0))) {
            return "Unknown variable";
        }

        for (int i = 0; i < commands.length; i++) {
            if (commands[i].matches("[a-zA-Z]+") ||
                    commands[i].matches("\\d+\\+") ||
                    i % 2 != 0 && commands[i].matches("\\d+") ||
                    commands[i].matches("\\d+-")) {
                return "Invalid expression";
            }
        }

        if (input.trim().length() == 1 && varOrNot) {
            return String.valueOf(values.get(vars.indexOf(input.trim())));
        } else if (input.trim().length() == 1) {
            return input;
        } else {
            for (int i = 0; i < commands.length; i++) {
                if (commands[i].matches("-{2,}")) {
                    if (commands[i].length() % 2 == 0) {
                        commands[i] = "+";
                    } else {
                        commands[i] = "-";
                    }
                }

                if (commands[i].matches("\\+\\d+")) {
                    commands[i] = commands[i].substring(1);
                }
            }
        }

        String temp = "";

        for (int i = 0; i < commands.length; i++) {
            if (commands[i].equals("+") && commands[i + 1].contains("-")) {
                continue;
            } else {
                temp += commands[i];
            }
        }

        PostFixConverter postFixConverter = new PostFixConverter(temp);
        PostFixCalculator postFixCalculator = new PostFixCalculator(postFixConverter.getPostfixAsList());

        return String.valueOf(postFixCalculator.result());
    }

    static void addVariables(String input, List<String> vars, List<String> values) {
        String[] variables = input.replaceAll("\\s+", "").split("=");

        for (int i = 0; i < variables.length - 1; i += 2) {
            if (!(variables[i].matches("[a-zA-Z]+"))) {
                System.out.println("Invalid identifier");
                break;
            } else if (variables.length > 2 || variables[i + 1].matches("[a-zA-Z]+\\d+[a-zA-Z]+") ||
                    variables[i + 1].matches("\\d+[a-zA-Z]+")) {
                System.out.println("Invalid assignment");
                break;
            } else {
                if (variables[i + 1].matches("[a-zA-Z]+")) {
                    if (!(vars.contains(variables[i + 1]))) {
                        System.out.println("Unknown variable");
                        break;
                    } else {
                        if (vars.contains(variables[i])) {
                            values.set(vars.indexOf(variables[i]), values.get(vars.indexOf(variables[i + 1])));
                        } else {
                            vars.add(variables[i]);
                            values.add(values.get(vars.indexOf(variables[i + 1])));
                        }
                    }
                } else {
                    if (vars.contains(variables[i])) {
                        values.set(vars.indexOf(variables[i]), variables[i + 1]);
                    } else {
                        vars.add(variables[i]);
                        values.add(variables[i + 1]);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> vars = new ArrayList<>();
        List<String> values = new ArrayList<>();

        while (true) {
            String input = scanner.nextLine();

            if (input.isEmpty()) continue;

            if (input.equals("/exit")) {
                System.out.println("Bye!");
                break;
            } else if (input.equals("/help")) {
                System.out.println("The program calculates the " +
                        "addition/subtraction/division/multiplication of numbers");
            } else if (input.matches("/.*")) {
                System.out.println("Unknown command");
            } else {
                if (input.replaceAll("\\s+", "").contains("=")) {
                    addVariables(input, vars, values);
                } else {
                    System.out.println(calculateNumbers(input, vars, values));
                }
            }
        }
    }
}

class PostFixConverter extends Main {
    private final String infix;
    private final Deque<Character> stack = new ArrayDeque<>();
    private final List<String> postfix = new ArrayList<>();

    public PostFixConverter(String expression) {
        infix = expression;
        convertExpression();
    }

    private void convertExpression() {
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i != infix.length(); ++i) {
            if (Character.isDigit(infix.charAt(i))) {
                temp.append(infix.charAt(i));

                while ((i + 1) != infix.length() && (Character.isDigit(infix.charAt(i + 1))
                        || infix.charAt(i + 1) == '.')) {
                    temp.append(infix.charAt(++i));
                }

                postfix.add(temp.toString());
                temp.delete(0, temp.length());
            } else {
                inputToStack(infix.charAt(i));
            }
        }

        clearStack();
    }


    private void inputToStack(char input) {
        if (stack.isEmpty() || input == '(') {
            stack.addLast(input);
        } else {
            if (input == ')') {
                while (!stack.getLast().equals('(')) {
                    postfix.add(stack.removeLast().toString());
                }

                stack.removeLast();
            } else {
                if (stack.getLast().equals('(')) {
                    stack.addLast(input);
                } else {
                    while (!stack.isEmpty() && !stack.getLast().equals('(') &&
                            getPrecedence(input) <= getPrecedence(stack.getLast())) {
                        postfix.add(stack.removeLast().toString());
                    }

                    stack.addLast(input);
                }
            }
        }
    }


    private int getPrecedence(char op) {
        if (op == '+' || op == '-') {
            return 1;
        } else if (op == '*' || op == '/') {
            return 2;
        } else if (op == '^') {
            return 3;
        } else {
            return 0;
        }
    }


    private void clearStack() {
        while (!stack.isEmpty()) {
            postfix.add(stack.removeLast().toString());
        }
    }


    List<String> getPostfixAsList() {
        return postfix;
    }
}

class PostFixCalculator extends Main {
    private final List<String> expression;
    private final Deque<BigInteger> stack = new ArrayDeque<>();

    PostFixCalculator(List<String> postfix) {
        expression = postfix;
    }


    public BigInteger result() {
        for (int i = 0; i != expression.size(); ++i) {
            if (Character.isDigit(expression.get(i).charAt(0))) {
                stack.addLast(new BigInteger(expression.get(i)));
            } else {
                BigInteger tempResult;
                BigInteger temp;

                switch (expression.get(i)) {
                    case "+":
                        temp = stack.removeLast();
                        tempResult = stack.removeLast().add(temp);
                        break;

                    case "-":
                        temp = stack.removeLast();
                        tempResult = stack.removeLast().subtract(temp);
                        break;

                    case "*":
                        temp = stack.removeLast();
                        tempResult = stack.removeLast().multiply(temp);
                        break;

                    case "/":
                        temp = stack.removeLast();
                        tempResult = stack.removeLast().divide(temp);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + expression.get(i));
                }

                stack.addLast(tempResult);
            }
        }

        return stack.removeLast();
    }
}