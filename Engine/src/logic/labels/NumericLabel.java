package logic.labels;

public class NumericLabel implements Label {
    private int num;

    NumericLabel(int num) {
        this.num = num;
    }

    @Override
    public String getLabel() { return "L" + num; }
}