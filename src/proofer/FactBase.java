package proofer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Ryan Kenney
 */
public class FactBase extends HashMap<Integer, Statable> {

    private Statable conclusion;
    private int index = 1;
    
    public static final String WILDCARD = "*";

    /**
     * Adds e to the FactBase
     *
     * @param e The Statable to add
     * @return True if e was added, false otherwise
     */
    public boolean add(Statable e) {
        for (Statable s : this.values()) {
            if (s.equals(e)) {
                return false;
            }
        }

        super.put(index++, e);
        return true;
    }

    /**
     * Attempts to add all members of li to the list.
     *
     * @param li The list of Statables to add
     * @return True if ALL members of li were entered, false otherwise
     */
    public boolean add(List<Statable> li) {
        boolean allEntered = true;

        for (Statable s : li) {
            allEntered &= add(s);
        }

        return allEntered;
    }

    /**
     * Resets the FactBase to empty
     */
    @Override
    public void clear() {
        super.clear();
        index = 1;
    }

    /**
     * Retrieves a list of all facts with op as their main operator
     *
     * @param op The operator to check for
     * @return The list of all facts with op as their main operator
     */
    public List<Statable> getByOperator(Operator op) {
        List<Statable> ret = new ArrayList<>();

        for (Statable s : values()) {
            if (s.getOperator() == op) {
                ret.add(s);
            }
        }

        return ret;
    }

    /**
     * @return the conclusion
     */
    public Statable getConclusion() {
        return conclusion;
    }

    /**
     * More flexible than get(key) because it returns 0..* values
     *
     * @param key The key to use
     * @return The list of values
     */
    public List<Statable> getValues(String key) {
        List<Statable> ret = new ArrayList<>();

        // Handle the wildcard case
        if (key.equals("*")) {
            for (Statable s : values()) {
                ret.add(s);
            }
        } else {
            Statable value = get(Integer.parseInt(key));
            if (value != null) {
                ret.add(value);
            }
        }

        return ret;
    }

    /**
     * @param conclusion the conclusion to set
     */
    public void setConclusion(Statable conclusion) {
        this.conclusion = conclusion;
    }
}
