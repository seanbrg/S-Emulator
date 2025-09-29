package logic.arguments;

import execute.components.ProgramManager;
import logic.instructions.api.synthetic.Quote;
import logic.variables.Variable;

import java.util.List;

public class QuoteArgument implements Argument {
    private final Quote quoteInstruction;

    public QuoteArgument(Quote quoteInstruction) {
        this.quoteInstruction = quoteInstruction;
    }

    @Override
    public String toString() {
        String printed = quoteInstruction.print();
        return printed.substring(printed.indexOf("-") + 2).trim();
    }

    public String customString() {
        List<Argument> args = quoteInstruction.getArgs();
        String argsStr = String.join(",", args.stream().map(Argument::toString).toList());
        String funcStr = quoteInstruction.getFunction().getUserStr();
        return funcStr + "(" + argsStr + ")";
    }

    @Override
    public Variable get() {
        quoteInstruction.execute();
        return quoteInstruction.getPrimaryVar();
    }

    public Quote getQuoteInstruction() {
        return quoteInstruction;
    }
}
