package proofer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Provides interface with the user.
 *
 * @author Ryan Kenney
 */
public class ProoferInterface {

    private static final String BASE_COORDINATE_INDICATOR = "0";
    private static final String RULE_FORMAT =
	    "[a-zA-Z]{1,4}\\([0-9\\*]+(,[0-9\\*]+){0,1}(,[1-2\\.]+[1-2])*\\)";

    private enum Mode {

	PREMISE, CONCLUSION, RULE;
    };

    private enum Command {

	EXIT("exit"), DONE("done"), SHOW_FACTS("facts"),
	THEREFORE("therefore"), RESET("reset"), HELP("help"),
	NONE("none");
	private String srep;

	private Command(String srep) {
	    this.srep = srep;
	}

	public boolean equals(String srep) {
	    return this.srep.equals(srep.toLowerCase());
	}
    }

    /**
     * Proofer - The Logic Engine
     *
     * @param args
     */
    public static void main(String args[]) {
	// Init
	Scanner scan = new Scanner(System.in);
	Mode inputMode = Mode.PREMISE;
	FactBase fb = new FactBase();
	Statable statement;
	String input;
	Map<Mode, List<Command>> commands = buildCommandMap();

	// Main program
	System.out.println("Welcome to Proofer - The Logic Engine!");
	System.out.println("Enter some premises");
	// instructions
	program:
	while (true) {
	    System.out.print("proofer> ");
	    input = scan.nextLine();

	    // Ignore blank lines
	    if (input.equals("")) {
		continue;
	    }

	    // Check for commands first
	    Command cmd = Command.NONE;
	    for (Command c : commands.get(inputMode)) {
		if (c.equals(input)) {
		    cmd = c;
		    break;
		}
	    }

	    // Execute the command
	    switch (cmd) {
		case EXIT:
		    break program;
		case THEREFORE:
		    inputMode = Mode.CONCLUSION;
		    System.out.println("Enter the conclusion");
		    continue;
		case DONE:
		    for (Statable s : fb.values()) {
			if (s.equals(fb.getConclusion())) {
			    System.out.println("You've shown the "
				    + "conclusion to be true!");
			    continue program;
			}
		    }
		    System.out.println("It appears you still haven't "
			    + "shown the conclusion to be true.  Are "
			    + "you sure the argument is valid?");
		    continue;

		case SHOW_FACTS:
		    System.out.println("\nFacts:");
		    for (Integer i : fb.keySet()) {
			System.out.println(i + "\t" + fb.get(i));
		    }
		    if (inputMode == Mode.RULE) {
			System.out.println("\nConclusion: " + fb.getConclusion());
		    }
		    System.out.println();
		    continue;
		case HELP:
		    break;
		case RESET:
		    System.out.println("\nClearing facts, entering premise mode.\n");
		    fb.clear();
		    inputMode = Mode.PREMISE;
		    continue;
	    }

	    // Handle the input in the appropriate way
	    switch (inputMode) {
		case PREMISE:
		    try {
			statement = Statement.parseString(input);
			fb.add(statement);
		    } catch (StatementParsingException ex) {
			System.err.println(ex.getMessage());
		    }
		    break;
		case CONCLUSION:
		    try {
			statement = Statement.parseString(input);
			fb.setConclusion(statement);
			System.out.println("Entering rule mode");
			inputMode = Mode.RULE;
		    } catch (StatementParsingException ex) {
			System.err.println(ex.getMessage());
		    }
		    break;
		case RULE:
		    // Make sure the pattern fits
		    // Avoids potential problems like NumberFormatException
		    try {
			input = input.replaceAll(" ", "");
			if (!input.matches(RULE_FORMAT)) {
			    throw new RuleFormatException("rule",
				    "Rule format must be name(arg0[,arg1])");
			}
			// Get the information we need
			Rule rule = Rule.getRule(input.substring(0, input.indexOf('(')).toUpperCase());
			String[] arguments = input.substring(input.indexOf('(') + 1,
				input.length() - 1).split(",");

			List<Statable> arg0 = fb.getValues(arguments[0]);
			List<Statable> result;

			if (arguments.length >= 2) {
			    // Handle Rules of Replacement
			    if (rule == Rule.DM
				    || rule == Rule.COM
				    || rule == Rule.ASSOC
				    || rule == Rule.DIST
				    || rule == Rule.DN
				    || rule == Rule.TRANS
				    || rule == Rule.IMPL
				    || rule == Rule.EQUIV
				    || rule == Rule.EXP
				    || rule == Rule.TAUT) {
				int[] coordinates = getCoordinates(rule.toString(), arguments[1]);
				result = rule.doRule(arg0, coordinates,
					usesWildcard(arguments[0]));
			    } else {
				List<Statable> arg1 = fb.getValues(arguments[1]);
				result = rule.doRule(arg0, arg1,
					usesWildcard(arguments[0], arguments[1]));
			    }

			} else {
			    // Handle the special "grab out of thin air" rule
			    if (rule == Rule.ADD) {
				List<Statable> arg1 = new ArrayList<>();
				Statable add = null;

				// Get the Statable to add
				System.out.println("enter the statement to add:");
				while (add == null) {
				    System.out.print("proofer> ");
				    try {
					add = Statement.parseString(scan.nextLine());
				    } catch (StatementParsingException ex) {
					System.err.println(ex.getMessage());
				    }
				}
				arg1.add(add);


				// Only do wildcard check on arg0
				result = rule.doRule(arg0, arg1, usesWildcard(arguments[0]));
			    } else {
				result = rule.doRule(arg0, usesWildcard(arguments[0]));
			    }
			}
			fb.add(result);

			break;
		    } catch (RuleFormatException ex) {
			System.err.println(ex.errMessage());
		    } catch (NullPointerException ex) {
			System.err.println("Error - Incorrect argument type");
		    }
	    }
	}

	// End program
	System.out.println("Good bye!");
    }

    /**
     * Returns the integer coordinates of a sub-statement
     *
     * @param rule The calling rule
     * @param scoord String coordinates
     * @return The coordinates represented as an integer, or null if the Base
     * Coordinate Indicator is sent.
     * @throws RuleFormatException An invalid coordinate String was received
     */
    private static int[] getCoordinates(String rule, String data)
	    throws RuleFormatException {
	if (data.equals(BASE_COORDINATE_INDICATOR)) {
	    return null;
	}
	String[] scoord = data.split("\\.");
	int[] coordinates = new int[scoord.length];
	for (int i = 0; i < coordinates.length; i++) {
	    try {
		coordinates[i] = Integer.parseInt(scoord[i]);
	    } catch (NumberFormatException ex) {
		throw new RuleFormatException(rule, "Invalid statement coordinates");
	    }
	}
	return coordinates;
    }

    /**
     * Checks if either arg0 or arg1 use the wildcard
     *
     * @param arg0 The first argument
     * @param arg1 The second argument
     * @return True if either argument or both use the wildcard, false otherwise
     */
    private static boolean usesWildcard(String arg0, String arg1) {
	return (usesWildcard(arg0) | usesWildcard(arg1));
    }

    /**
     * Checks if arg0 is a wildcard
     *
     * @param arg0 The argument to check
     * @return True if the argument uses the wildcard, false otherwise
     */
    private static boolean usesWildcard(String arg0) {
	return arg0.equals(FactBase.WILDCARD);
    }

    /**
     * Builds a map associating a mode of execution with valid commands in that
     * mode.
     *
     * @return The command map
     */
    private static Map<Mode, List<Command>> buildCommandMap() {
	// init
	Map<Mode, List<Command>> map = new HashMap<>();

	// Premise
	List<Command> premiseCommands = new ArrayList();
	premiseCommands.add(Command.EXIT);
	premiseCommands.add(Command.THEREFORE);
	premiseCommands.add(Command.SHOW_FACTS);
	map.put(Mode.PREMISE, premiseCommands);

	// Conclusion
	List<Command> conclusionCommands = new ArrayList();
	conclusionCommands.add(Command.EXIT);
	conclusionCommands.add(Command.SHOW_FACTS);
	map.put(Mode.CONCLUSION, conclusionCommands);

	// Rule
	List<Command> ruleCommands = new ArrayList();
	ruleCommands.add(Command.EXIT);
	ruleCommands.add(Command.DONE);
	ruleCommands.add(Command.SHOW_FACTS);
	ruleCommands.add(Command.RESET);
	map.put(Mode.RULE, ruleCommands);

	return map;
    }
}
