package execute.managers;

import logic.instructions.Instruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.labels.NumericLabel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelManager {
    Label maxLabel = null;
    Map<String, Label> labels;

    public LabelManager() {
        labels = new HashMap<>();
    }

    public void addLabel(Label label) {
        if (label instanceof NumericLabel) {
            labels.put(label.getLabel(), label);
            if (maxLabel == null || maxLabel.getNum() < label.getNum()) {
                maxLabel = label;
            }
        }
    }

    public void loadInstructionLabels(List<Instruction> instructions) {
        instructions
                .stream()
                .filter(instr -> instr.getSelfLabel() instanceof NumericLabel)
                .forEach(instr -> { this.addLabel(instr.getSelfLabel()); } );
    }

    public void clear() {
        labels.clear();
        maxLabel = null;
    }

    public void removeLabel(Label label) {
        labels.remove(label.getLabel());
        if (maxLabel == label) {
            maxLabel = labels.values().stream()
                    .max(Comparator.comparing(Label::getLabel))
                    .orElse(null);
        }
    }

    public Label getFreshLabel() {
        int max = maxLabel == null ? 0 : maxLabel.getNum();
        Label newLabel = new NumericLabel(max + 1);
        labels.put(newLabel.getLabel(), newLabel);
        maxLabel = newLabel;
        return newLabel;
    }
}
