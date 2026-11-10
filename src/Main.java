import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

/** Runs the menu and connects user input to the expense tracker classes. */
public class Main {
    private static final String DATA_FILE = "expenses.csv";
    private static final Scanner INPUT = new Scanner(System.in);

    /** Loads existing records, starts the menu, and saves before exit. */
    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        FileStorage storage = new FileStorage(DATA_FILE);
        int loaded = storage.load(manager);
        System.out.printf("Welcome. Loaded %d expense(s).%n", loaded);

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addExpense(manager);
                case 2 -> listExpenses(manager);
                case 3 -> removeExpense(manager);
                case 4 -> showSummary(manager);
                case 5 -> setBudget(manager);
                case 6 -> {
                    storage.save(manager);
                    running = false;
                    System.out.println("Your data has been saved. Goodbye.");
                }
                default -> System.out.println("Choose a number from 1 through 6.");
            }
        }
    }

    /** Displays the available actions. */
    private static void printMenu() {
        System.out.println("\n=== Monthly Expense Tracker ===");
        System.out.println("1. Add an expense");
        System.out.println("2. View all expenses");
        System.out.println("3. Remove an expense");
        System.out.println("4. View this month's summary");
        System.out.println("5. Set this month's budget");
        System.out.println("6. Save and quit");
    }

    /** Collects validated details and records a new expense. */
    private static void addExpense(ExpenseManager manager) {
        String description = readRequiredText("What was the expense? ");
        double amount = readPositiveAmount("Amount: $");
        String category = readRequiredText("Category: ");
        LocalDate date = readDate("Date (YYYY-MM-DD, Enter for today): ");
        manager.addExpense(new Expense(description, amount, category, date));
        System.out.println("Expense added.");
        if (manager.isOverBudget()) {
            System.out.println("Budget alert: this month's spending is over the limit.");
        }
    }

    /** Prints all saved expenses with numbers suitable for the remove action. */
    private static void listExpenses(ExpenseManager manager) {
        if (manager.getExpenseCount() == 0) {
            System.out.println("No expenses have been recorded yet.");
            return;
        }
        for (int index = 0; index < manager.getExpenses().size(); index++) {
            System.out.printf("%d. %s%n", index + 1, manager.getExpenses().get(index));
        }
    }

    /** Lets the user remove one expense by its displayed number. */
    private static void removeExpense(ExpenseManager manager) {
        if (manager.getExpenseCount() == 0) {
            System.out.println("There are no expenses to remove.");
            return;
        }
        listExpenses(manager);
        int displayedNumber = readInt("Number to remove: ");
        boolean removed = manager.removeExpense(displayedNumber - 1);
        System.out.println(removed ? "Expense removed." : "That number is not in the list.");
    }

    /** Shows current-month total, category totals, and budget status. */
    private static void showSummary(ExpenseManager manager) {
        System.out.printf("This month's total: $%.2f%n", manager.getCurrentMonthTotal());
        if (manager.getCurrentMonthCategoryTotals().isEmpty()) {
            System.out.println("No expenses are dated in the current month.");
        } else {
            manager.getCurrentMonthCategoryTotals().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                    .forEach(entry -> System.out.printf("  %-16s $%.2f%n",
                            entry.getKey(), entry.getValue()));
        }
        if (manager.getMonthlyBudget() == 0) {
            System.out.println("No budget is set. Choose option 5 to add one.");
            return;
        }
        System.out.printf("Budget: $%.2f | %.1f%% used | $%.2f remaining%n",
                manager.getMonthlyBudget(), manager.getBudgetUsedPercent(), manager.getBudgetRemaining());
        if (manager.isOverBudget()) {
            System.out.println("You are over this month's budget.");
        }
    }

    /** Reads a nonnegative budget, where zero clears the budget. */
    private static void setBudget(ExpenseManager manager) {
        double budget = readNonnegativeAmount("Monthly budget (0 clears it): $");
        manager.setMonthlyBudget(budget);
        System.out.println(budget == 0 ? "Budget cleared." : "Monthly budget updated.");
    }

    /** Keeps asking until the user enters a whole number. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(INPUT.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number, such as 2.");
            }
        }
    }

    /** Reads a nonempty text value. */
    private static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    /** Reads an amount strictly greater than zero. */
    private static double readPositiveAmount(String prompt) {
        while (true) {
            double amount = readNonnegativeAmount(prompt);
            if (amount > 0) {
                return amount;
            }
            System.out.println("Enter an amount greater than zero.");
        }
    }

    /** Reads an amount that is zero or greater. */
    private static double readNonnegativeAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double amount = Double.parseDouble(INPUT.nextLine().trim());
                if (Double.isFinite(amount) && amount >= 0) {
                    return amount;
                }
                System.out.println("Enter a finite amount that is zero or greater.");
            } catch (NumberFormatException exception) {
                System.out.println("Enter a number such as 45.50.");
            }
        }
    }

    /** Reads an ISO date and uses today's date when the field is left blank. */
    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (value.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException exception) {
                System.out.println("Use YYYY-MM-DD, for example 2026-11-02.");
            }
        }
    }
}