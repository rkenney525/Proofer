package proofer;

/**
 *
 * @author Ryan Kenney
 */
public enum Operator {
    COND("->"), BICOND("<->"), OR("V"), AND("^"), NOT("~");
    private String srep;
    
    private Operator(String srep) {
        this.srep = srep;
    }
    
    /**
     * Check if srep is the String representation of the Operator
     * @param srep The String to check
     * @return True if the Operator matches srep, false otherwise.
     */
    public boolean equals(String srep) {
        return this.srep.equals(srep);
    }
    
    /**
     * Returns the number of characters in the operator.
     * ie "<->" has 3 characters while "^" has 1 character.
     * @return The number of characters
     */
    protected int length() {
        return srep.length();
    }
    
    @Override
    public String toString() {
        return srep;
    }
}
