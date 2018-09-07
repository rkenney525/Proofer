package proofer;

/**
 * Thrown when a parsing error occurs
 * @author Ryan Kenney
 */
public class StatementParsingException extends Exception {
    public StatementParsingException(String msg) {
        super(msg);
    }
}
