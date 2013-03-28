package proofer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ryan Kenney
 */
public enum Rule {

    MP("MP", "Modus Ponens"), MT("MT", "Modus Tolens"), DS("DS", "Disjunctive Syllogism"),
    HS("HS", "Hypothetical Syllogism"), SIMP("Simp", "Simplification"),
    CONJ("Conj", "Conjunction"), CD("CD", "Constructive Dilemma"),
    ABS("Abs", "Absorption"), ADD("Add", "Addition"), DM("DM", "DeMorgans"),
    COM("Com", "Commutation"), ASSOC("Assoc", "Association"), DIST("Dist", "Distribution"),
    DN("DN", "Double Negation"), TRANS("Trans", "Transposition"),
    IMPL("Impl", "Material Implication"), EQUIV("Equiv", "Material Equivalence"),
    EXP("Exp", "Exportation"), TAUT("Taut", "Tautology");
    private String terse, verbose;

    private Rule(String terse, String verbose) {
	this.terse = terse;
	this.verbose = verbose;
    }

    /**
     * Get the rule whose terse String (or toString()) is val
     *
     * @param val The String value matching the terse representation of a rule
     * @return The Rule, if there is a match, false otherwise
     */
    public static Rule getRule(String val) {
	for (Rule r : values()) {
	    if (val.equalsIgnoreCase(r.terse)) {
		return r;
	    }
	}

	return null;
    }

    /**
     * Gives the String representation of the rule.
     *
     * @param verb True for verbose String, false for terse
     * @return The String representation of the rule
     */
    public String toString(boolean verb) {
	return (verb) ? verbose : terse;
    }

    /**
     * Returns the terse representation of the rule
     *
     * @return The terse representation of the rule
     */
    @Override
    public String toString() {
	return toString(false);
    }

    /**
     * Call the Rule's appropriate function.  The implementation is clunky, but
     * it works.
     * 
     * @param arg0 The first argument(s)
     * @param arg1 The second argument(s)
     * @param silent True if no error should be reported, false otherwise
     * @return The result of the rule
     * @throws RuleFormatException The rule cannot be applied
     */
    public List<Statable> doRule(List<Statable> arg0, List<Statable> arg1,
	    boolean silent) throws RuleFormatException {
	switch (this) {
	    case MP:
		return ModusPonens(arg0, arg1, silent);
	    case MT:
		return ModusTollens(arg0, arg1, silent);
	    case DS:
		return DisjunctiveSyllogism(arg0, arg1, silent);
	    case HS:
		return HypotheticalSyllogism(arg0, arg1, silent);
	    case SIMP:
		return Simplification(arg0, silent);
	    case CONJ:
		return Conjunction(arg0, arg1, silent);
	    case CD:
		return ConstructiveDilemma(arg0, arg1, silent);
	    case ABS:
		return Absorption(arg0, silent);
	    case ADD:
		return Addition(arg0, arg1.get(0), silent);
	    default:
		return null;
	}
    }

    /**
     * Single argument version of doRule(). This function simply calls the two
     * argument version with null passed as the second parameter, but is
     * included for convenience.
     *
     * @param arg0 The argument for the Rule
     * @return A List of resulting Statables
     * @throws RuleFormatException The rule cannot be applied
     */
    public List<Statable> doRule(List<Statable> arg0, boolean silent)
	    throws RuleFormatException {
	return doRule(arg0, null, silent);
    }

    /**
     * Attempts to do Modus Ponens with imp and ant.
     *
     * @param imp The implication
     * @param ant The antecedent of imp (otherwise the rule cannot be applied)
     * @return The resulting statement if the rule can be executed
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable ModusPonens(Statable imp, Statable ant)
	    throws RuleFormatException {
	// Make sure imp is an implication and
	// make sure ant really is the antecedent of imp
	if (imp.getOperands()[0].equals(ant)
		&& (imp.getOperator() == Operator.COND)) {
	    return imp.getOperands()[1];
	} else {
	    throw new RuleFormatException("MP", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Modus Ponens
     *
     * @param imp The list of implications
     * @param ant The list of antecedents
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> ModusPonens(List<Statable> imp, List<Statable> ant,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : imp) {
	    for (Statable arg1 : ant) {
		try {
		    ret.add(ModusPonens(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do Modus Tollens with imp and negant
     *
     * @param imp The implication
     * @param negcon The negation of the consequent of imp (otherwise the rule
     * cannot be applied.
     * @return The resulting statement if the rule can be executed
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable ModusTollens(Statable imp, Statable negcon)
	    throws RuleFormatException {
	// Make sure imp is an implication and
	// make sure negant really is the negation of the consequent of imp
	// Note:  Due to strict nature of rule, (~p -> q) and p will not
	//        work.  Additional rule must be applied beforehand.
	if ((imp.getOperator() == Operator.COND)
		&& imp.getOperands()[1].equals(negcon.getOperands()[0])
		&& negcon.getOperator() == Operator.NOT) {
	    return Statement.negation(imp.getOperands()[0]);
	} else {
	    throw new RuleFormatException("MT", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Modus Tollens
     *
     * @param imp The list of implications
     * @param negcon The list of negative consequents
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> ModusTollens(List<Statable> imp, List<Statable> negcon,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : imp) {
	    for (Statable arg1 : negcon) {
		try {
		    ret.add(ModusTollens(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do a disjunctive syllogism with disj and negd
     *
     * @param disj The disjunction
     * @param negd The negation of the first disjunct (otherwise the rule cannot
     * be applied)
     * @return The second disjunct
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable DisjunctiveSyllogism(Statable disj, Statable negd)
	    throws RuleFormatException {
	// Make sure disj is a disjunction and
	// Make sure negd is a negation of the first disjunct
	if ((disj.getOperator() == Operator.OR)
		&& negd.equals(Statement.negation(disj.getOperands()[0]))) {
	    return disj.getOperands()[1];
	} else {
	    throw new RuleFormatException("DS", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Disjunctive Syllogism
     *
     * @param disj The list of disjunctions
     * @param negd The list of negative disjuncts
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> DisjunctiveSyllogism(List<Statable> disj, List<Statable> negd,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : disj) {
	    for (Statable arg1 : negd) {
		try {
		    ret.add(DisjunctiveSyllogism(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do a hypothetical syllogism on imp0 and imp1
     *
     * @param imp0 The first implication
     * @param imp1 The second implication
     * @return The resulting implication
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable HypotheticalSyllogism(Statable imp0, Statable imp1)
	    throws RuleFormatException {
	// Make sure imp0 is an implication and
	// make sure imp1 is an implication and
	// make sure the consequent of imp0 is the antecedent of imp1
	if ((imp0.getOperator() == Operator.COND)
		&& (imp1.getOperator() == Operator.COND)
		&& (imp0.getOperands()[1].equals(imp1.getOperands()[0]))) {
	    try {
		return Statement.parseString("(" + imp0.getOperands()[0].toString() + ")"
			+ Operator.COND.toString()
			+ "(" + imp1.getOperands()[1].toString() + ")");
	    } catch (StatementParsingException ex) {
		throw new RuleFormatException("HS", "Rule could not be applied");
	    }
	} else {
	    throw new RuleFormatException("HS", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Disjunctive Syllogism
     *
     * @param imp0 The first list of implications
     * @param imp1 The second list of implications
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> HypotheticalSyllogism(List<Statable> imp0, List<Statable> imp1,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : imp0) {
	    for (Statable arg1 : imp1) {
		try {
		    ret.add(HypotheticalSyllogism(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to simplify the first conjunct of conj
     *
     * @param conj The conjunction
     * @return The first conjunct of conjuction
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable Simplification(Statable conj)
	    throws RuleFormatException {
	// Make sure conj is a conjunction
	if (conj.getOperator() == Operator.AND) {
	    return conj.getOperands()[0];
	} else {
	    throw new RuleFormatException("SIMP", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Conjunction
     *
     * @param conj The list of conjunctions
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> Simplification(List<Statable> conj, boolean silent)
	    throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : conj) {
	    try {
		ret.add(Simplification(arg0));
	    } catch (RuleFormatException ex) {
		if (!silent) {
		    throw ex;
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do a conjunction on conj0 and conj1
     *
     * @param conj0 The first conjunct
     * @param conj1 The second conjunct
     * @return The conjunction of conj0 and conj1
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable Conjunction(Statable conj0, Statable conj1)
	    throws RuleFormatException {
	try {
	    return Statement.parseString("(" + conj0 + ")"
		    + Operator.AND
		    + "(" + conj1 + ")");
	} catch (StatementParsingException ex) {
	    throw new RuleFormatException("CONJ", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Conjunction
     *
     * @param conj0 The first list of conjunctions
     * @param conj1 The second list of conjunctions
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> Conjunction(List<Statable> conj0, List<Statable> conj1,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : conj0) {
	    for (Statable arg1 : conj1) {
		try {
		    ret.add(Conjunction(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do a constructive dilemma on conjOfImps and disj
     *
     * @param conjOfImps The conjunction of two implications
     * @param disj The disjunction of the two consequents
     * @return The disjunction of the two consequents (otherwise the rule cannot
     * be applied)
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable ConstructiveDilemma(Statable conjOfImps, Statable disj)
	    throws RuleFormatException {
	// Make sure conjOfImps is a conjunction and
	// make sure the first conjunct is an implication and
	// make sure the second conjunct is an implication and
	// make sure disj is a disjunct and
	// make sure the first disjunct is the antecedent of the first
	// implication and
	// make sure the second disjunct is the antecedent of the second
	// implication
	if ((conjOfImps.getOperator() == Operator.AND)
		&& (conjOfImps.getOperands()[0].getOperator() == Operator.COND)
		&& (conjOfImps.getOperands()[1].getOperator() == Operator.COND)
		&& (disj.getOperator() == Operator.OR)
		&& (disj.getOperands()[0].equals(conjOfImps.getOperands()[0].getOperands()[0]))
		&& (disj.getOperands()[1].equals(conjOfImps.getOperands()[1].getOperands()[0]))) {
	    try {
		return Statement.parseString("(" + conjOfImps.getOperands()[0]
			.getOperands()[1] + ")" + Operator.OR
			+ "(" + conjOfImps.getOperands()[1].getOperands()[1] + ")");
	    } catch (StatementParsingException ex) {
		throw new RuleFormatException("CD", "Rule could not be applied");
	    }
	} else {
	    throw new RuleFormatException("CD", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Constructive Dilemma
     *
     * @param conjOfImps The list of conjunction of implications
     * @param negd The list of disjunctions
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> ConstructiveDilemma(List<Statable> conjOfImps, List<Statable> disj,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : conjOfImps) {
	    for (Statable arg1 : disj) {
		try {
		    ret.add(ConstructiveDilemma(arg0, arg1));
		} catch (RuleFormatException ex) {
		    if (!silent) {
			throw ex;
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do an Absorption on imp.
     *
     * @param imp The implication
     * @return The resulting statement
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable Absorption(Statable imp) throws RuleFormatException {
	// Make sure imp is an implication
	if (imp.getOperator() == Operator.COND) {
	    try {
		return Statement.parseString(imp.getOperands()[0].toString()
			+ Operator.COND
			+ "("
			+ imp.getOperands()[0].toString()
			+ Operator.AND
			+ imp.getOperands()[1].toString()
			+ ")");
	    } catch (StatementParsingException ex) {
		throw new RuleFormatException("ABS", "Rule could not be applied");
	    }
	} else {
	    throw new RuleFormatException("ABS", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Absorption
     *
     * @param imp The list of implications
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> Absorption(List<Statable> imp, boolean silent)
	    throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : imp) {
	    try {
		ret.add(Absorption(arg0));
	    } catch (RuleFormatException ex) {
		if (!silent) {
		    throw ex;
		}
	    }
	}

	return ret;
    }

    /**
     * Attempts to do Addition on arg0 with arg1
     *
     * @param arg0 The first statement to add
     * @param arg1 The second statement to add
     * @return The disjunction of arg0 and arg1
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable Addition(Statable arg0, Statable arg1)
	    throws RuleFormatException {
	try {
	    return Statement.parseString("(" + arg0 + ")"
		    + Operator.OR
		    + "(" + arg1 + ")");
	} catch (StatementParsingException ex) {
	    throw new RuleFormatException("ADD", "Rule could not be applied");
	}
    }

    /**
     * The rule-logic behind Addition
     *
     * @param stat The list of Statables
     * @param add The Statable to add
     * @param silent True if you don't want error messages, flase otherwise.
     * @return The result of every successful rule application
     */
    public static List<Statable> Addition(List<Statable> stat, Statable add, boolean silent)
	    throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : stat) {
	    try {
		ret.add(Addition(arg0, add));
	    } catch (RuleFormatException ex) {
		if (!silent) {
		    throw ex;
		}
	    }
	}

	return ret;
    }

    /**
     * Applies the Double Negative rule of Replacement
     *
     * @param stat The statement to
     * @return The double negation of stat
     * @throws RuleFormatException The rule cannot be applied
     */
    private static Statable DoubleNegative(Statable stat)
	    throws RuleFormatException {
	try {
	    if (stat.getOperator() == Operator.NOT
		    && stat.getOperands()[0].getOperator() == Operator.NOT) {
		// case of ~~p
		return stat.getOperands()[0].getOperands()[0];
	    } else {
		// case of p
		return Statement.parseString(Operator.NOT.toString()
			+ Operator.NOT.toString()
			+ stat);
	    }
	} catch (StatementParsingException ex) {
	    throw new RuleFormatException("DN", "Rule could not be applied");
	}
    }

    /**
     * Applies the Double Negative rule of replacement
     *
     * @param stat The List of statements to negate
     * @param coordinates The coordinates of the statement to operate on
     * @param silent True if you don't want error messages, false otherwise.
     * @return The list of double negated statements
     * @throws RuleFormatException The rule cannot be applied
     */
    public static List<Statable> DoubleNegative(List<Statable> stat, int[] coordinates,
	    boolean silent) throws RuleFormatException {
	List<Statable> ret = new ArrayList<>();

	for (Statable arg0 : stat) {
	    try {
		ret.add(DoubleNegative(arg0));


		/*Statable copy = null;
		 try {
		 copy = Statement.parseString(arg0.toString());
		 } catch (StatementParsingException ex) {
		 System.err.println("This should never print - problem with toString()");
		 continue;
		 }
		 Statable operate = copy;
		 Statable parent;
		 if (coordinates == null) {
		 ret.add(DoubleNegative(arg0));
		 continue;
		 }
		 try {
		 for (int i = 0; i < coordinates.length; i++) {
		 parent = operate;
		 try {
		 operate = parent.getOperands()[coordinates[i] - 1];
		 if (i == coordinates.length - 1) {
		 parent.getOperands()[coordinates[i] - 1] = 
		 DoubleNegative(operate);
		 }
		 } catch (ArrayIndexOutOfBoundsException ex) {
		 throw new RuleFormatException("DN", "Location too large or small");
		 }
		 }
		 ret.add(copy);
		 } catch (NullPointerException ex) {
		 throw new RuleFormatException("DN", "Invalid location");
		 }*/
	    } catch (RuleFormatException ex) {
		if (!silent) {
		    throw ex;
		}
	    }
	}

	return ret;
    }
}
