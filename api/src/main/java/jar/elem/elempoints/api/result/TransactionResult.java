package jar.elem.elempoints.api.result;

/**
 * Represents the result of any economy transaction.
 *
 * @since 2.0.0
 */
public final class TransactionResult {

    public enum Status {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        CURRENCY_NOT_FOUND,
        MAX_BALANCE_EXCEEDED,
        TRANSFER_DISABLED,
        BELOW_MINIMUM,
        CANCELLED_BY_EVENT,
        ERROR
    }

    private final Status status;
    private final double oldBalance;
    private final double newBalance;
    private final double amount;
    private final String message;

    private TransactionResult(Status status, double oldBalance, double newBalance, double amount, String message) {
        this.status = status;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.amount = amount;
        this.message = message;
    }

    public static TransactionResult success(double oldBalance, double newBalance, double amount) {
        return new TransactionResult(Status.SUCCESS, oldBalance, newBalance, amount, null);
    }

    public static TransactionResult failure(Status status, double currentBalance, String message) {
        return new TransactionResult(status, currentBalance, currentBalance, 0, message);
    }

    public static TransactionResult failure(Status status, String message) {
        return new TransactionResult(status, 0, 0, 0, message);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public Status getStatus() {
        return status;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "TransactionResult{status=" + status +
                ", old=" + oldBalance +
                ", new=" + newBalance +
                ", amount=" + amount +
                (message != null ? ", msg=" + message : "") + "}";
    }
}
