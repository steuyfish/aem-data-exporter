package aem.dataexporter.utilities;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for dealing with program arguments.
 */
public class ProgramArguments {

    /**
     * Hostname/Port regular expression.
     */
    private static final Pattern HOSTNAME_PORT = Pattern.compile("^.*://([A-Za-z0-9\\-\\.]+)(:[0-9]+)?.*$");
    /**
     * System err output.
     */
    private final PrintStream ERR = System.err;
    /**
     * Required {@code ProgramArgument}s.
     */
    private ProgramArgument[] requiredArguments;
    /**
     * Contains the {@code ProgramArgument}s and their associated values.
     */
    private final Map<ProgramArgument, String> argumentMap = new HashMap<ProgramArgument, String>();

    /**
     * Constructs a new {@code ProgramArguments}.
     *
     * @param arguments Array of program arguments.
     * @param newRequiredArguments List of required {@code ProgramArgument}s.
     */
    public ProgramArguments(final String[] arguments, final ProgramArgument... newRequiredArguments) {
        this.requiredArguments = newRequiredArguments;
        setArgumentValues(arguments);
        setHostnameAndPort();
    }

    /**
     * Returns the value of the specified {@code ProgramArgument}.
     *
     * @param key {@code ProgramArgument} to retrieve value of.
     * @return Value of the specified {@code ProgramArgument}.
     */
    public final String get(final ProgramArgument key) {
        return argumentMap.get(key);
    }

    /**
     * Returns a boolean indicating whether or not a value has been provided for the specified {@code ProgramArgument}.
     *
     * @param key {@code ProgramArgument} to check for a provided value.
     * @return True if a value has been provided for the specified {@code ProgramArgument}; otherwise false.
     */
    public final boolean has(final ProgramArgument key) {
        return argumentMap.containsKey(key);
    }

    /**
     * Returns a boolean indicating whether or not all required {@code ProgramArgument}s have a value provided.
     *
     * @return True if a value has been provided for all of the required {@code ProgramArgument}s; otherwise false.
     */
    public final boolean hasRequiredArguments() {
        boolean hasAllRequiredArguments = true;
        for (ProgramArgument argument : requiredArguments) {
            if (!argumentMap.containsKey(argument)) {
                hasAllRequiredArguments = false;
                break;
            }
        }
        return hasAllRequiredArguments;
    }

    /**
     * Lists all the missing {@code ProgramArgument}s to {@code System.ERR}.
     */
    public final void listMissingArguments() {
        for (ProgramArgument argument : requiredArguments) {
            if (!argumentMap.containsKey(argument)) {
                listMissingArgument(argument);
            }
        }
    }

    /**
     * List a missing {@code ProgramArgument} to {@code System.ERR}.
     *
     * @param programArgument {@code ProgramArgument} to list as missing.
     */
    public final void listMissingArgument(final ProgramArgument programArgument) {
        ERR.println("Missing argument: " + programArgument.getKey());
    }

    /**
     * Prints the usage message for the data exporter.
     */
    public final void printUsageMessage() {
        ERR.println("Usage:");
        ERR.println("params are: -host <host> -path <path> -username <username> -password <password> " +
                "[-maxPageDepth <maxPageDepth>] [-maxDAMDepth <maxDAMDepth>]");
        ERR.println("-host: The host to connect to in order to export data from");
        ERR.println("-path: The content path in the JCR to start data export from");
        ERR.println("-username: The username to authenticate with");
        ERR.println("-password: The password to authenticate with");
        ERR.println("-packageName: The name of the package to create");
        ERR.println("-maxPageDepth: The maximum page depth");
        ERR.println("-maxDAMDepth: The maximum DAM depth");
        ERR.println("-overwritePackages: boolean value to determine wheather to overrite packages (optional).");
        ERR.println(
                "-uploadFullContent: boolean value to determine wheather to upload content downloaded from another server.");
        ERR.println("Note that at least one of maxPageDepth and maxDAMDepth must be specified");
        listMissingArguments();
    }

    /**
     * Prints the usage message for the package upload/downloaders.
     */
    public final void printPackageUsageMessage() {
        ERR.println("Usage:");
        ERR.println("params are: -host <host> -username <username> -password <password>");
        ERR.println("-host: The host to connect to in order to export data from");
        ERR.println("-username: The username to authenticate with");
        ERR.println("-password: The password to authenticate with");
        ERR.println(
                "-packageName: The (base) name of the package that was created (i.e. without the '_<packageNumber>' in the name");
        ERR.println("-numberOfPackages: The number of packages that have been created");
        listMissingArguments();
    }

    /**
     * Sets the program argument values.
     *
     * @param args Array of program arguments.
     */
    private void setArgumentValues(final String[] args) {
        int index = 0;
        while (index < args.length) {
            boolean foundParameterValue = setParameter(args, index);
            if (foundParameterValue) {
                index = index + 2;
            } else {
                index = index + 1;
            }
        }
    }

    /**
     * Sets the hostname and port values.
     */
    private void setHostnameAndPort() {
        Matcher matcher = HOSTNAME_PORT.matcher(get(ProgramArgument.HOST));
        if (matcher.matches()) {
            argumentMap.put(ProgramArgument.HOSTNAME, matcher.group(1));
            argumentMap.put(ProgramArgument.PORT, matcher.group(2).replaceAll(":", ""));
        }
    }

    /**
     * Sets the {@code ProgramArgument} from the specified array of program arguments.
     *
     * @param arguments Array of program arguments.
     * @param index Index to retrieve program arguments from.
     * @return True if the {@code ProgramArgument} was set correctly.
     */
    private boolean setParameter(final String[] arguments, final int index) {
        if (arguments.length < index + 1) {
            ERR.println("Argument [" + arguments[index] + "] must have a value... exiting.");
            return false;
        }
        for (ProgramArgument programArgument : ProgramArgument.values()) {
            if (arguments[index].equalsIgnoreCase("-" + programArgument.getKey())) {
                String argumentValue = arguments[index + 1];
                if (StringUtils.isNotBlank(argumentValue)) {
                    argumentMap.put(programArgument, argumentValue);
                    return true;
                } else {
                    ERR.println("Argument for [" + programArgument.getKey() + "] is blank, not using.");
                }
            }
        }
        ERR.println("Unrecognized argument [" + arguments[index] + "]");
        return false;
    }

}
