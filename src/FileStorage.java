import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads and writes expense data and the budget in a CSV file. */
public class FileStorage {
    private final Path filePath;

    /** Creates storage that uses the supplied file name. */
    public FileStorage(String fileName) {
        this.filePath = Path.of(fileName);
    }

    /** Saves the budget and all expense rows, returning false if writing fails. */
    public boolean save(ExpenseManager manager) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("BUDGET," + manager.getMonthlyBudget());
            writer.newLine();
            for (Expense expense : manager.getExpenses()) {
                writer.write(expense.toCsvRow());
                writer.newLine();
            }
            return true;
        } catch (IOException exception) {
            System.out.println("Could not save expense data: " + exception.getMessage());
            return false;
        }
    }

    /** Loads saved data and returns the number of valid expenses recovered. */
    public int load(ExpenseManager manager) {
        if (!Files.exists(filePath)) {
            return 0;
        }
        int loadedCount = 0;
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String row;
            while ((row = reader.readLine()) != null) {
                if (row.isBlank()) {
                    continue;
                }
                try {
                    if (row.startsWith("BUDGET,")) {
                        manager.setMonthlyBudget(Double.parseDouble(row.substring(7)));
                    } else {
                        manager.addExpense(Expense.fromCsvRow(row));
                        loadedCount++;
                    }
                } catch (RuntimeException exception) {
                    System.out.println("Skipped one invalid data row: " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            System.out.println("Could not read expense data: " + exception.getMessage());
        }
        return loadedCount;
    }
}