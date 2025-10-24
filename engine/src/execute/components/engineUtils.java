package execute.components;

import execute.dto.InstructionDTO;

import java.util.ArrayList;
import java.util.List;

public class engineUtils {

    public static List<InstructionDTO> getInstrParents(InstructionDTO selectedInstr) {
        List<InstructionDTO> result = new ArrayList<>();

        for (InstructionDTO current = selectedInstr; current != null; current = current.getParent()) {
            result.add(current);
        }

        return result;
    }
}
