package proofer;

/**
 * Common interface between Statement and BaseFact
 * @author Ryan Kenney
 */
public interface Statable {
    public Operator getOperator();
    public Statable[] getOperands();
}
