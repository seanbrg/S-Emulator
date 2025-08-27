package logic.labels;

public class NumericLabel implements Label {
    private int num;

    public NumericLabel(int num) {
        this.num = num;
    }

    @Override
    public String getLabel() { return "L" + num; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumericLabel other)) return false;
        return num == other.num;
    }
    @Override
    public int hashCode() { return Integer.hashCode(num); }

}