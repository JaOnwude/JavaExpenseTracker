import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores expenses and calculates totals and budget information. */
public class ExpenseManager {
    private final ArrayList<Expense> expenses = new ArrayList<>();
    private double monthlyBudget;

    /** Adds an expense to the in-memory collection. */
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    /** Removes an expense by its zero-based position and reports success. */
    public boolean removeExpense(int index) {
        if (index < 0 || index >= expenses.size()) {
            return false;
        }
        expenses.remove(index);
        return true;
    }

    /** Returns a read-only view of the recorded expenses. */
    public List<Expense> getExpenses() {
        return List.copyOf(expenses);
    }

    /** Returns the number of recorded expenses. */
    public int getExpenseCount() {
        return expenses.size();
    }

    /** Calculates spending for the current calendar month. */
    public double getCurrentMonthTotal() {
        YearMonth thisMonth = YearMonth.now();
        double total = 0;
        for (Expense expense : expenses) {
            if (YearMonth.from(expense.getDate()).equals(thisMonth)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    /** Groups current-month spending by category with a HashMap. */
    public Map<String, Double> getCurrentMonthCategoryTotals() {
        YearMonth thisMonth = YearMonth.now();
        Map<String, Double> totals = new HashMap<>();
        for (Expense expense : expenses) {
            if (YearMonth.from(expense.getDate()).equals(thisMonth)) {
                totals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    /** Sets the monthly spending limit; zero means no budget is set. */
    public void setMonthlyBudget(double monthlyBudget) {
        if (!Double.isFinite(monthlyBudget) || monthlyBudget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative.");
        }
        this.monthlyBudget = monthlyBudget;
    }

    /** Returns the current monthly budget. */
    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    /** Returns the percentage of this month's budget already used. */
    public double getBudgetUsedPercent() {
        if (monthlyBudget == 0) {
            return 0;
        }
        return getCurrentMonthTotal() / monthlyBudget * 100;
    }

    /** Returns the budget amount left; a negative number means overspending. */
    public double getBudgetRemaining() {
        return monthlyBudget - getCurrentMonthTotal();
    }

    /** Reports whether current-month spending exceeds a set budget. */
    public boolean isOverBudget() {
        return monthlyBudget > 0 && getCurrentMonthTotal() > monthlyBudget;
    }
}