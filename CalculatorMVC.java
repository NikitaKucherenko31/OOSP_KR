import java.io.*;
import java.util.*;

public class CalculatorMVC {

    public static void main(String[] args) {
        CalculatorController controller = new CalculatorController();
        controller.run();
    }
}
// Модель
class CalculatorModel {
    String historyFile = "calculator_history.txt"; // Имя файла для истории
    private List<Equation> equations = new ArrayList<>();

    public void addEquation(Equation equation) {
        equations.add(equation);
        saveHistory();
    }

    public List<Equation> getEquations() {
        return equations;
    }

    public void loadHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    equations.add(new Equation(parts[0]));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении истории: " + e.getMessage());
        }
    }

    private void saveHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(historyFile))) {
            for (Equation equation : equations) {
                writer.println(equation.getExpression() + "=" + equation.getResult());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении истории: " + e.getMessage());
        }
    }

    public void saveEquations(List<Equation> selectedEquations, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Equation equation : selectedEquations) {
                writer.println(equation.getExpression() + "=" + equation.getResult());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении уравнений: " + e.getMessage());
        }
    }
}
// Представление
class CalculatorView {
    Scanner scanner = new Scanner(System.in);

    public void showMenu() {
        System.out.println("----- Калькулятор -----");
        System.out.println("1. Рассчитать уравнение");
        System.out.println("2. Просмотреть историю");
        System.out.println("3. Сохранить уравнения в файл");
        System.out.println("4. Выход");
        System.out.print("Введите номер действия: ");
    }

    public String getEquation() {
        System.out.print("Введите уравнение: ");
        return scanner.nextLine();
    }

    public void showResult(Equation equation) {
        System.out.println("Результат: " + equation.getResult());
    }

    public void showHistory(List<Equation> equations) {
        if (equations.isEmpty()) {
            System.out.println("История пуста.");
            return;
        }
        System.out.println("----- История -----");
        for (int i = 0; i < equations.size(); i++) {
            System.out.println((i + 1) + ". " + equations.get(i).getExpression() + " = " + equations.get(i).getResult());
        }
    }

    public List<Equation> selectEquations(List<Equation> equations) {
        List<Equation> selectedEquations = new ArrayList<>();
        if (equations.isEmpty()) {
            System.out.println("История пуста.");
            return selectedEquations;
        }
        System.out.println("----- История -----");
        for (int i = 0; i < equations.size(); i++) {
            System.out.println((i + 1) + ". " + equations.get(i).getExpression() + " = " + equations.get(i).getResult());
        }
        System.out.print("Введите номера уравнений для сохранения (через запятую): ");
        String input = scanner.nextLine();
        String[] numbers = input.split(",");
        for (String number : numbers) {
            try {
                int index = Integer.parseInt(number.trim()) - 1;
                if (index >= 0 && index < equations.size()) {
                    selectedEquations.add(equations.get(index));
                } else {
                    System.out.println("Некорректный номер уравнения: " + number);
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод: " + number);
            }
        }
        return selectedEquations;
    }

    public String getFilePath() {
        System.out.print("Введите путь к файлу для сохранения (пусто - текущий файл): ");
        return scanner.nextLine();
    }
}
// Контроллер
class CalculatorController {
    private CalculatorModel model = new CalculatorModel();
    private CalculatorView view = new CalculatorView();

    public void run() {
        model.loadHistory();
        int choice;
        do {
            view.showMenu();
            choice = view.scanner.nextInt();
            view.scanner.nextLine(); // очистка буфера
            switch (choice) {
                case 1:
                    handleCalculate();
                    break;
                case 2:
                    handleShowHistory();
                    break;
                case 3:
                    handleSaveEquations();
                    break;
                case 4:
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Некорректный выбор.");
            }
        } while (choice != 4);
    }

    private void handleCalculate() {
        String equationStr = view.getEquation();
        try {
            Equation equation = new Equation(equationStr);
            model.addEquation(equation);
            view.showResult(equation);
        } catch (Exception e) {
            System.err.println("Ошибка при расчете уравнения: " + e.getMessage());
        }
    }

    private void handleShowHistory() {
        view.showHistory(model.getEquations());
    }

    private void handleSaveEquations() {
        List<Equation> selectedEquations = view.selectEquations(model.getEquations());
        String filePath = view.getFilePath();
        if (filePath.isEmpty()) {
            filePath = model.historyFile;
            System.out.println("Сохранение в файл: " + filePath);
        } else {
            if (filePath.contains(".")) { // Проверка на расширение
                filePath = filePath;
            } else if (filePath.contains(File.separator)) { // Проверка на путь
                filePath = filePath + File.separator + "log.log";
            } else {
                filePath = filePath + ".log";
            }
            System.out.println("Сохранение в файл: " + filePath);
        }
        model.saveEquations(selectedEquations, filePath);
    }
}

class Equation {
    private String expression;
    private double result;

    public Equation(String expression) {
        this.expression = expression;
        this.result = evaluate(expression);
    }

    public String getExpression() {
        return expression;
    }

    public double getResult() {
        return result;
    }

    private double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Неожиданный символ: " + ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm(); // сложение
                    else
                        if (eat('-')) x -= parseTerm(); // вычитание
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor(); // умножение
                    else if (eat('/')) x /= parseFactor(); // деление
                    else if (eat('%')) x %= parseFactor(); // остаток от деления
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // унарный плюс
                if (eat('-')) return -parseFactor(); // унарный минус

                double x;
                int startPos = this.pos;
                if (eat('(')) { // скобки
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // число
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // функция
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expression.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Неизвестная функция: " + func);
                    }
                } else {
                    throw new RuntimeException("Неожиданный символ: " + ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // возведение в степень

                return x;
            }
        }.parse();
    }
}
