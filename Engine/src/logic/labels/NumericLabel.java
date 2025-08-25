package logic.labels;

public class NumericLabel {
    private int num;

    NumericLabel(int num) {
        this.num = num;
    }

    public String getName() { return "L" + num; }
}