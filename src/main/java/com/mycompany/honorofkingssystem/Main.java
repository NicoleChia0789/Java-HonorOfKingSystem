package com.mycompany.honorofkingssystem;

import java.util.*;

public class Main {

    // Why use static? 
    // Because the main() method itself is static, static methods can only use static variables directly.
    static Scanner scanner = new Scanner(System.in);
    static RelationshipGraph graph = new RelationshipGraph();

    public static void main(String[] args) {
        graph.loadInitialData();        // load the 15 default character and their 17 relationship
        printWelcome();
        mainMenu();
    }

    // ==================== Welcome ====================
    static void printWelcome() {

        ConsoleUI.printBanner("HONOR OF KINGS - CHARACTER RELATIONSHIP SYSTEM");
        System.out.println("  Graph Type      : Undirected Graph");
        System.out.println("  Representation  : Adjacency List");
        System.out.println("  Traversal       : Breadth-First Search (BFS)");
        ConsoleUI.printLightLine();
        System.out.printf("  %-20s: %d%n", "Total Characters", graph.getCharacterCount());
        System.out.printf("  %-20s: %d%n", "Total Relationships", graph.getRelationshipCount());
        ConsoleUI.printHeavyLine();
        System.out.println();
    }

    // ==================== Main Menu ====================
    static void mainMenu() {
        while (true) {
            ConsoleUI.printBanner("MAIN MENU");
            System.out.println("  1. Manage Characters & Relationships");
            System.out.println("  2. Search Character");
            System.out.println("  3. BFS Traversal");
            System.out.println("  4. Display All Relationships");
            System.out.println("  5. View Full Graph Visualization");
            System.out.println("  0. Exit");
            ConsoleUI.printLightLine();

            int choice = getInteger("  Enter your choice: ", 0, 5);
            System.out.println("");

            switch (choice) {
                case 1:
                    manageCharacterAndRelationshipMenu();
                    break;
                case 2:
                    searchMenu();
                    break;
                case 3:
                    bfsMenu();
                    break;
                case 4:
                    graph.displayGraph();
                    pressZeroToContinue();
                    break;
                case 5:
                    viewFullGraph();
                    break;
                case 0:
                    exitProgram();
                    return;
            }
        }
    }

    // ==================== View Full Graph ====================
    static void viewFullGraph() {
        System.out.println();
        ConsoleUI.printHeader("FULL GRAPH VISUALIZATION");
        System.out.println("  Displaying all characters and relationships...");
        ConsoleUI.printFooter();
        System.out.println();

        Set<String> allCharacters = graph.getAllCharacters();
        List<String[]> allEdges = graph.getAllEdges();

        // Create a level map (all at level 0 for the visualizer)
        Map<String, Integer> levelMap = new LinkedHashMap<>();
        for (String character : allCharacters) {
            levelMap.put(character, 0);
        }

        graph.displayFullGraph();
    }

    // ==================== Manage Menu ====================
    static void manageCharacterAndRelationshipMenu() {
        while (true) {

            ConsoleUI.printBanner("MANAGE CHARACTERS & RELATIONSHIPS");
            System.out.println("  1. Add a Character");
            System.out.println("  2. Remove a Character");
            System.out.println("  3. Add a Relationship");
            System.out.println("  4. Remove a Relationship");
            System.out.println("  0. Back to Main Menu");
            ConsoleUI.printLightLine();

            int choice = getInteger("  Enter your choice: ", 0, 4);
            System.out.println("");

            switch (choice) {
                case 1:
                    addCharacter();

                    break;
                case 2:
                    removeCharacter();

                    break;
                case 3:
                    addRelationship();

                    break;
                case 4:
                    removeRelationship();

                    break;
                case 0:
                    return;
            }
        }
    }

    // ==================== Add Character ====================
    static void addCharacter() {

        ConsoleUI.printBanner("ADD CHARACTER");
        System.out.println("  Enter 0 to cancel.");
        String name = getNonEmptyString("  Enter character name: ");
        if (name == null) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        graph.addCharacter(name);
        pressZeroToContinue();
    }

    // ==================== Remove Character ====================
    static void removeCharacter() {

        ConsoleUI.printBanner("REMOVE CHARACTER");

        printCharacterList();

        System.out.println("  Enter 0 to cancel.");
        int number = getInteger(
                "  Enter character number to remove: ", 0, graph.getCharacterCount()
        );

        if (number == 0) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        String name = graph.getCharacterByNumber(number);

        System.out.println();
        System.out.println("  Selected character: " + name);

        String confirmation = getYesNo("  Confirm remove '" + name + "' and all relationships? (Y/N): ");
        
        if (confirmation.equals("Y")) {
            graph.removeCharacter(name);
            pressZeroToContinue();
        } else {
            System.out.println("  [!] Operation cancelled.");
        }
    }

    // ==================== Add Relationship ====================
    static void addRelationship() {

        ConsoleUI.printBanner("ADD RELATIONSHIP");

        if (graph.getCharacterCount() < 2) {
            System.out.println("  [!] At least two characters are required.");
            return;
        }

        printCharacterList();

        System.out.println("  Enter 0 to cancel.");

        int firstNumber = getInteger("  Enter first character number: ", 0, graph.getCharacterCount());

        if (firstNumber == 0) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        int secondNumber = getInteger("  Enter second character number: ", 0, graph.getCharacterCount());

        if (secondNumber == 0) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        String c1 = graph.getCharacterByNumber(firstNumber);
        String c2 = graph.getCharacterByNumber(secondNumber);

        if (graph.relationshipExists(c1, c2)) {
            System.out.println("  [!] A relationship already exists between " + c1 + " and " + c2 + ".");
            System.out.println("  [!] Multiple relationships between the same two characters are not allowed.");
            pressZeroToContinue();
            return;
        }

        System.out.println();
        ConsoleUI.printLightLine();
        System.out.println("  Relationship Types");
        ConsoleUI.printLightLine();
        System.out.println("  1. Teammates");
        System.out.println("  2. Lovers");
        System.out.println("  3. Married Couple");
        System.out.println("  4. Siblings");
        System.out.println("  5. Sisters");
        System.out.println("  6. Sworn Brothers");
        System.out.println("  7. Lord & Strategist");
        System.out.println("  8. Rivals");
        System.out.println("  9. Other (custom input)");
        System.out.println("  0. Cancel");
        ConsoleUI.printLightLine();

        int typeNumber = getInteger("  Enter relationship type: ", 0, 9);

        if (typeNumber == 0) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        String type;

        if (typeNumber == 9) {
            // let user type a custom relationship type
            type = getNonEmptyString("  Enter custom relationship type: ");
            if (type == null) {
                System.out.println("  [!] Operation cancelled.");
                return;
            }
        } else {
            type = getRelationshipType(typeNumber);
        }

        graph.addRelationship(c1, c2, type);
        pressZeroToContinue();
    }

    // ==================== Remove Relationship  ====================
    static void removeRelationship() {

        ConsoleUI.printBanner("REMOVE RELATIONSHIP");

        // Get all relationships
        List<String[]> allEdges = graph.getAllEdges();

        if (allEdges.isEmpty()) {
            System.out.println("  [!] No relationships exist to remove.");
            pressZeroToContinue();
            return;
        }

        System.out.println("  Existing Relationships (" + allEdges.size() + ")");
        ConsoleUI.printLightLine();

        int number = 1;
        for (String[] edge : allEdges) {
            String c1 = edge[0];
            String c2 = edge[1];
            String type = edge[2];
            System.out.printf("  %2d. %-16s <--[ %-16s ]--> %s%n", number, c1, type, c2);
            number++;
        }

        ConsoleUI.printLightLine();
        System.out.println("  Enter 0 to cancel.");

        int choice = getInteger("  Enter relationship number to remove: ", 0, allEdges.size());

        if (choice == 0) {
            System.out.println("  [!] Operation cancelled.");
            return;
        }

        // Get the selected relationship
        String[] selectedEdge = allEdges.get(choice - 1);
        String c1 = selectedEdge[0];
        String c2 = selectedEdge[1];
        String type = selectedEdge[2];

        System.out.println();
        System.out.println("  Selected relationship: " + c1 + " <--[ " + type + " ]--> " + c2);
        
        String confirmation = getYesNo("  Confirm remove this relationship? (Y/N): ");
        
        if (confirmation.equals("Y")) {
            graph.removeRelationship(c1, c2);
            pressZeroToContinue();
        } else {
            System.out.println("  [!] Operation cancelled.");
        }
    }

    // ==================== Search ====================
    static void searchMenu() {

        ConsoleUI.printBanner("SEARCH CHARACTER");

        printCharacterList();

        System.out.println("  Enter 0 to cancel.");

        int number = getInteger("  Enter character number to search: ", 0, graph.getCharacterCount());

        if (number == 0) {
            return;
        }

        String name = graph.getCharacterByNumber(number);

        graph.searchCharacter(name);

        pressZeroToContinue();
    }

    // ==================== BFS ====================
    static void bfsMenu() {

        ConsoleUI.printBanner("BFS TRAVERSAL");
        System.out.println("  BFS explores connections level by level.");
        System.out.println();

        printCharacterList();

        System.out.println("  Enter 0 to cancel.");

        int number = getInteger("  Enter starting character number: ", 0, graph.getCharacterCount());

        if (number == 0) {
            return;
        }

        String name = graph.getCharacterByNumber(number);

        System.out.println();
        System.out.println("  Starting character: " + name);

        int maxLevel = getInteger("  Enter maximum level (1 - 99): ", 1, 99);

        graph.bfsTraversal(name, maxLevel);

        System.out.println();
        String choice = getYesNo("  View traversal as graph? (Y/N): ");
        
        if (choice.equals("Y")) {
            GraphVisualizer.showBfsGraph(name, graph.getLastBfsLevels(), graph.getLastBfsEdges());
        }

        pressZeroToContinue();
    }

    // ==================== Character List ====================
    static void printCharacterList() {
        System.out.println();
        System.out.println("  Characters (" + graph.getCharacterCount() + ")");
        ConsoleUI.printLightLine();

        int number = 1;

        for (String name : graph.getAllCharacters()) {
            System.out.printf("  %2d. %s%n", number, name);
            number++;
        }

        ConsoleUI.printLightLine();
    }

    // ==================== Relationship Type ====================
    static String getRelationshipType(int number) {
    switch (number) {
        case 1: 
            return "Teammates";
        case 2: 
            return "Lovers";
        case 3: 
            return "Married Couple";
        case 4: 
            return "Siblings";
        case 5: 
            return "Sisters";
        case 6: 
            return "Sworn Brothers";
        case 7: 
            return "Lord & Strategist";
        case 8: 
            return "Rivals";
        default: 
            return "";
    }
}

    // ==================== Input Validation ====================
    static int getInteger(String message, int min, int max) {
        while (true) {
            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println(
                            "  [!] Please enter a number from "
                            + min + " to " + max + "."
                    );
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a number.");
            }
        }
    }

    public static String getNonEmptyString(String prompt) {
        String input;

        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  [!] Input cannot be empty.");
            } else if (input.equals("0")) {
                return null;
            } else {
                return input;
            }
        }
    }

    // ==================== Press 0 ====================
    static void pressZeroToContinue() {
    while (true) {
        System.out.print("\n  Press 0 to return to menu: ");
        String input = scanner.nextLine().trim();
        if (input.equals("0")) {
            return;
        }
        System.out.println("  [!] Please enter 0.");
    }
}

    // ==================== Exit ====================
    static void exitProgram() {

        ConsoleUI.printHeader("THANK YOU FOR USING THIS SYSTEM!");
        System.out.println("  Goodbye!");
        ConsoleUI.printFooter();

        scanner.close();
        System.exit(0);
    }
    
    // ==================== Y/N Input Validation ====================
static String getYesNo(String prompt) {
    while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim().toUpperCase();
        
        if (input.equals("Y") || input.equals("N")) {
            return input;
        }
        System.out.println("  [!] Invalid input. Please enter Y or N only.");
    }
}
}