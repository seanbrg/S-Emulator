package emulator.utils;

public class ProgramRunStats {
    private int runCount = 0;
    private double totalCost = 0.0;

    public void recordRun(double cost) {
        runCount++;
        totalCost += cost;
    }

    public int getRunCount() {
        return runCount;
    }

    public double getAverageCost() {
        return (runCount == 0) ? 0 : totalCost / runCount;
    }
}
