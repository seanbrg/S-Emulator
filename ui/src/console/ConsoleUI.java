package console;
import execute.EngineImpl;
import java.util.Scanner;

public class ConsoleUI {
    private final EngineImpl engine = new EngineImpl();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new ConsoleUI().mainMenu();
    }

    private void mainMenu() {
        while (true) {
            System.out.println("\n==== S-Emulator ====");
            System.out.println("1. Load Program from XML");
            System.out.println("2. Show Program");
            System.out.println("3. Expand Program");
            System.out.println("4. Run Program");
            System.out.println("5. Show History/Statistics");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> loadXml();
                case "2" -> engine.printProgram();
                case "3" -> {}
                case "4" -> {
                    int input = 0;
                    long result;
                    int degree = engine.maxDegree();
                    System.out.printf("Current Program maximum degree is: %d.%n", degree);
                    System.out.printf("Please choose Program degree from 0 to %d:%n", degree);

                    try {
                        input = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice, please try again.");
                        break;
                    }
                    if (0 <= input && input <= degree) {
                        result = engine.runProgram(input);
                        System.out.println(String.format("Program exited with result y = %d", result));
                    }
                    else {
                        System.out.println("Invalid choice, please try again.");
                    }
                }
                case "5" -> {}
                case "6" -> {
                    System.out.println("Exiting S-Emulator");
                    return;
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private void loadXml() {
        System.out.print("Enter full XML file path: ");
        String path = scanner.nextLine();
        engine.loadFromXML(path);
    }
}
