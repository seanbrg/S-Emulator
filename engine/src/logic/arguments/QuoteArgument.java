package logic.arguments;

import logic.instructions.api.synthetic.Quote;
import logic.variables.Variable;

public class QuoteArgument implements Argument {
    private final Quote quoteInstruction;

    public QuoteArgument(Quote quoteInstruction) {
        this.quoteInstruction = quoteInstruction;
    }

    @Override
    public String toString() {
        return quoteInstruction.print();
    }

    @Override
    public Variable get() {
        quoteInstruction.execute();
        return quoteInstruction.getVars().getFirst();
    }
}
