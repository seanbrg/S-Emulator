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
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> loadXml();
                case "2" -> engine.printProgram();
                case "3" -> {
                    System.out.println("Exiting S-Emulator");
                    return;
                }
                case "4" -> {
                    int input = 0;
                    try {
                        input = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice, please try again.");
                        break;
                    }
                    if (0 <= input && input <= engine.maxDegree())
                        engine.runProgram(input);
                    else {
                        System.out.println("Invalid choice, please try again.");
                    }
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
