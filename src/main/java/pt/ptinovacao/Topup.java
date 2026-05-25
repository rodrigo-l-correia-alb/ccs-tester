package pt.ptinovacao;


public class Topup {

    private long topupInterval;

    private boolean topupExecuted;

    private double minimumBalance;

    private long topupExecutionTimestamp;

    public Topup(long topupInterval, boolean topupExecuted, double minimumBalance, long topupExecutionTimestamp) {
        this.topupInterval = topupInterval;
        this.topupExecuted = topupExecuted;
        this.minimumBalance = minimumBalance;
        this.topupExecutionTimestamp = topupExecutionTimestamp;
    }

    public void addTopupInterval() {
        this.topupExecutionTimestamp += this.topupInterval;
    }

    public long getTopupExecutionTimestamp() {
        return topupExecutionTimestamp;
    }

    public void setTopupExecutionTimestamp(long topupExecutionTimestamp) {
        this.topupExecutionTimestamp = topupExecutionTimestamp;
    }

    public long getTopupInterval() {
        return topupInterval;
    }

    public boolean isTopupExecuted() {
        return topupExecuted;
    }

    public void setTopupInterval(long topupInterval) {
        this.topupInterval = topupInterval;
    }

    public void setTopupExecuted(boolean topupExecuted) {
        this.topupExecuted = topupExecuted;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    @Override
    public String toString() {
        return "Topup{" +
                "topupInterval=" + topupInterval +
                ", topupExecuted=" + topupExecuted +
                ", minimumBalance=" + minimumBalance +
                ", topupExecutionTimestamp=" + topupExecutionTimestamp +
                '}';
    }

}
