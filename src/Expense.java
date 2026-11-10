import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Represents one expense recorded by the tracker. */
public class Expense {
    private final String description;
    private final double amount;
    private final String category;
    private final LocalDate date;

    /** Creates an expense after checking that its required values are valid. */
    public Expense(String description, double amount, String category, LocalDate date) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        this.description = description.trim();
        this.amount = amount;
        this.category = category.trim();
        this.date = date;
    }

    /** Returns the expense description. */
    public String getDescription() {
        return description;
    }

    /** Returns the amount spent. */
    public double getAmount() {
        return amount;
    }

    /** Returns the expense category. */
    public String getCategory() {
        return category;
    }

    /** Returns the date the expense occurred. */
    public LocalDate getDate() {
        return date;
    }

    /** Formats the expense as a readable row for the console. */
    @Override
    public String toString() {
        return String.format("%s | %-12s | %-24s | $%.2f", date, category, description, amount);
    }

    /** Converts the expense to a properly escaped CSV row. */
    public String toCsvRow() {
        return String.join(",", escapeCsv(date.toString()), escapeCsv(category),
                escapeCsv(description), Double.toString(amount));
    }

    /** Creates an expense from a CSV row written by this class. */
    public static Expense fromCsvRow(String row) {
        List<String> fields = parseCsvRow(row);
        if (fields.size() != 4) {
            throw new IllegalArgumentException("Expected four CSV fields.");
        }
        return new Expense(fields.get(2), Double.parseDouble(fields.get(3)),
                fields.get(1), LocalDate.parse(fields.get(0)));
    }

    /** Escapes commas, quotes, and line breaks using standard CSV quoting. */
    private static String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    /** Splits a CSV row while preserving commas inside quoted fields. */
    private static List<String> parseCsvRow(String row) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;
        for (int index = 0; index < row.length(); index++) {
            char current = row.charAt(index);
            if (current == '"') {
                if (insideQuotes && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                fields.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        if (insideQuotes) {
            throw new IllegalArgumentException("Unclosed quote in CSV row.");
        }
        fields.add(value.toString());
        return fields;
    }
}