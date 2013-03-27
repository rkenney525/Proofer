package proofer;


import java.util.Arrays;

/**
 *
 * @author Ryan Kenney
 */
public class Statement implements Statable {
    // Attributes
    private Operator op;
    private Statable[] operands;

    // Methods
    private Statement() {
        operands = new Statable[2];
    }

    /**
     * Converts a String into a logical statement
     *
     * @param s The String to parse
     * @return The logical statement as a Statable
     * @throws StatementParsingException A parsing error occurs
     */
    public static Statable parseString(String s) throws StatementParsingException {
        // init
        Statement statement = new Statement();
        int index = 0;
        Statable operand;
        s = s.replaceAll(" ", ""); // eliminate whitespace issues

        // parse
        // Case 1: It's a BaseFact
        if ((operand = isBaseFact(s.charAt(index))) != null) {
            statement.operands[0] = operand;
            index++;

            // A statement can be just a BaseFact
            if (index >= s.length()) {
                return operand;
            }
        } // Case 2: Starts with Open Paren
        else if (s.charAt(index) == '(') {
            int start = index + 1;
            index = findCloseParen(s, index);
            if ((operand = Statement.parseString(s.substring(start, index))) != null) {
                statement.operands[0] = operand;
                index++;
                // Only a statement is allowed in parens
            } else {
                throw new StatementParsingException("Error in parenthesis, not a statement");
            }

            // handles superfluous parens
            if (index >= s.length()) {
                return operand;
            }
        } else if (s.charAt(index) == '~') {
            index++;
            statement.op = Operator.NOT;
            if ((operand = Statement.parseString(s.substring(index))) != null) {
                statement.operands[0] = operand;
            }
            index += operand.toString().replaceAll(" ", "").length();
            
            if (index >= s.length()) {
                return statement;
            }
        } else {
            throw new StatementParsingException("Error - Unrecognized token");
        }

        // Next we need an operator
        for (Operator o : Operator.values()) {
            // find more elegant fix later to exclude the unary op
            if (o == Operator.NOT) {
                continue;
            }
            // account for the varying length of operators
            if ((index + o.length() < s.length())
                    && o.equals(s.substring(index, index + o.length()))) {
                index += o.length();
                statement.op = o;
            }
        }
        if (statement.op == null) {
            throw new StatementParsingException("Error - Expected operator");
        }

        // Finally we need a statement
        if ((operand = Statement.parseString(s.substring(index))) != null) {
            statement.operands[1] = operand;
        }

        return statement;
    }
    
    /**
     * Creates the negated form of a Statable
     * @param s The Statable to negate
     * @return The negated Statable
     */
    public static Statable negation(Statable s) {
        Statement neg = new Statement();
        neg.op = Operator.NOT;
        neg.operands[0] = s;
        return neg;
    }

    /**
     * Check if c is a BaseFact
     *
     * @param c The value to check
     * @return The BaseFact representation of c if it exists, null otherwise
     */
    private static BaseFact isBaseFact(char c) {
        for (BaseFact bf : BaseFact.values()) {
            if (bf.equals(c)) {
                return bf;
            }
        }

        // No matches found
        return null;
    }

    /**
     * Search a String for the matching close paren of some specified open
     * paren.
     *
     * @param s The whole String to search
     * @param start index of the paren to match
     * @return Index of the appropriate closing paren
     * @throws StatementParsingException If no matching paren found
     */
    private static int findCloseParen(String s, int start)
            throws StatementParsingException {
        // init
        int offset = 0;
        char pointer;

        for (int i = start + 1; i < s.length(); i++) {
            pointer = s.charAt(i);
            switch (pointer) {
                case '(':
                    offset++;
                    break;
                case ')':
                    if (offset == 0) {
                        return i;
                    } else {
                        offset--;
                    }
                    break;
            }
        }

        throw new StatementParsingException("No matching parenthesis");
    }
    
    @Override
    public String toString() {
        if (op == null) {
            return "(" + ((BaseFact) operands[0]).toString() + ")";
        } else if (operands[1] == null) {
            return op.toString() + operands[0].toString();
        } else {
            return "(" + operands[0].toString() + " " + op.toString() + " "
                    + operands[1].toString() + ")";
        }
    }

    @Override
    public Operator getOperator() {
        return op;
    }

    @Override
    public Statable[] getOperands() {
        return operands;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Statement) {
            return this.toString().equals(((Statement) obj).toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.op != null ? this.op.hashCode() : 0);
        hash = 29 * hash + Arrays.deepHashCode(this.operands);
        return hash;
    }
}
