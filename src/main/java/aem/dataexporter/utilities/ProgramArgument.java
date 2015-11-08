package aem.dataexporter.utilities;

/**
 * The program arguments used.
 */
public enum ProgramArgument {
    /**
     * Host argument key.
     */
    HOST("host"),
    /**
     * Hostname argument key.
     */
    HOSTNAME("hostname"),
    /**
     * Max DAM depth argument key.
     */
    MAX_DAM_DEPTH("maxDAMDepth"),
    /**
     * Max page depth argument key.
     */
    MAX_PAGE_DEPTH("maxPageDepth"),
    /**
     * Number of packages argument key.
     */
    NUMBER_OF_PACKAGES("numberOfPackages"),
    /**
     * Package name argument key.
     */
    PACKAGE_NAME("packageName"),
    /**
     * Password argument key.
     */
    PASSWORD("password"),
    /**
     * Path argument key.
     */
    PATH("path"),
    /**
     * Port argument key.
     */
    PORT("port"),
    /**
     * Username argument key.
     */
    USERNAME("username"),
    /**
     * Working directory argument key.
     */
    WORKING_DIRECTORY("workingDirectory");
    /**
     * Name of the program argument.
     */
    private String key;

    /**
     * Constructs a new {@code ProgramArgument}.
     *
     * @param newKey Name of the program argument.
     */
    ProgramArgument(final String newKey) {
        this.key = newKey;
    }

    /**
     * Returns the name of the program argument.
     *
     * @return Name of the program argument.
     */
    public String getKey() {
        return key;
    }
}
