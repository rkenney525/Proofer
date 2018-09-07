package proofer;

/**
 *
 * @author Ryan Kenney
 */
public class RuleFormatException extends Exception {
    private String rule;
    
    public RuleFormatException(String rule, String msg) {
        super(msg);
        this.rule = rule;
    }
    
    public String errMessage() {
        return "Error executing " + rule + " - " + getMessage();
    }
}
