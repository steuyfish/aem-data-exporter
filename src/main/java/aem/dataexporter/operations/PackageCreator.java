package aem.dataexporter.operations;

import aem.dataexporter.file.PackageFileWriter;
import aem.dataexporter.file.PackageFileZipper;
import aem.dataexporter.http.HttpReader;
import aem.dataexporter.json.JsonJcrParser;
import aem.dataexporter.utilities.ProgramArgument;
import aem.dataexporter.utilities.ProgramArguments;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Package creator for the {@code DataExporter}.
 * <p>
 * This class is designed to supplement the {@code DataExporter} by allowing the ability to individually run specific
 * operations should the entire data export process fail.
 * <p>
 * The package creator expects the following program arguments: <ul> <li>host</li> <li>path</li> <li>username</li>
 * <li>password</li> <li>packageName</li> <li>maxPageDepth - i.e. how deep to parse the page structure</li>
 * <li>maxDAMDepth - i.e. how deep to parse the dam structure</li> </ul>
 */
public class PackageCreator {

    /**
     * Number of packages that have been created.
     */
    public static int numberOfPackages = 0;
    /**
     * Maximum number of content paths that can be defined in an {@code CRX} package definition.
     */
    private static final int MAXIMUM_PACKAGE_CONTENT_PATHS = 100;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PackageCreator.class.getName());
    /**
     * Required program arguments.
     */
    private static final ProgramArgument[] REQUIRED_ARGUMENTS =
            {ProgramArgument.HOST, ProgramArgument.PATH, ProgramArgument.USERNAME, ProgramArgument.PASSWORD,
                    ProgramArgument.PACKAGE_NAME};
    /**
     * Depth of the {@code JCR DAM} content path crawling.
     */
    private static int damDepth = 0;
    /**
     * Depth of the page content path crawling.
     */
    private static int pageDepth = 0;

    /**
     * Run the package creator.
     *
     * @param args Array of program arguments.
     */
    public static void main(final String[] args) {
        ProgramArguments programArguments = new ProgramArguments(args, REQUIRED_ARGUMENTS);
        if (!programArguments.hasRequiredArguments()) {
            programArguments.printUsageMessage();
            System.exit(-1);
        }
        int maxPageDepth = 0;
        int maxDAMDepth = 0;
        if (programArguments.has(ProgramArgument.MAX_PAGE_DEPTH) ||
                programArguments.has(ProgramArgument.MAX_DAM_DEPTH)) {
            maxPageDepth = getDepth(programArguments.get(ProgramArgument.MAX_PAGE_DEPTH));
            maxDAMDepth = getDepth(programArguments.get(ProgramArgument.MAX_DAM_DEPTH));
        } else {
            programArguments.printUsageMessage();
            programArguments.listMissingArgument(ProgramArgument.MAX_PAGE_DEPTH);
            programArguments.listMissingArgument(ProgramArgument.MAX_DAM_DEPTH);
            System.exit(-1);
        }
        HttpReader httpReader = new HttpReader(programArguments.get(ProgramArgument.USERNAME),
                programArguments.get(ProgramArgument.PASSWORD), programArguments.get(ProgramArgument.HOSTNAME),
                programArguments.get(ProgramArgument.PORT));
        Set<String> contentPaths = new LinkedHashSet<String>();
        populateContentPaths(contentPaths, httpReader, programArguments.get(ProgramArgument.HOST),
                programArguments.get(ProgramArgument.PATH), maxPageDepth, maxDAMDepth);
        writePackages(contentPaths, programArguments.get(ProgramArgument.USERNAME),
                programArguments.get(ProgramArgument.PACKAGE_NAME));
    }

    /**
     * Returns the depth for the specified program argument value.
     *
     * @param argumentDepth Depth specified as a program argument.
     * @return Depth for the specified program argument value.
     */
    private static int getDepth(final String argumentDepth) {
        if (StringUtils.isNotBlank(argumentDepth)) {
            try {
                return Integer.parseInt(argumentDepth);
            } catch (NumberFormatException e) {
                // Do nothing as returning the maximum value for an integer will be sufficient.
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Populates the {@code Set} that contains the {@code JCR DAM} content paths.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param httpReader {@code HttpReader} to use to retrieve content.
     * @param host Name of the host to retrieve content from.
     * @param path Path to retrieve content from.
     * @param maxPageDepth Maximum depth to retrieve page content paths for.
     * @param maxDAMDepth Maximum depth to retrieve {@code JCR DAM} content paths for.
     */
    private static void populateContentPaths(final Set<String> contentPaths, final HttpReader httpReader,
            final String host, final String path, final int maxPageDepth, final int maxDAMDepth) {
        JsonJcrParser jsonJcrParser = new JsonJcrParser();
        Set<String> pageContentPaths = new LinkedHashSet<String>();
        LOGGER.log(Level.INFO, "Adding page content path: {0}", path);
        pageContentPaths.add(path);
        contentPaths.add(path);
        if (pageDepth < maxPageDepth) {
            populatePageContentPaths(pageContentPaths, httpReader, jsonJcrParser, host, path, maxPageDepth);
        }
        if (maxDAMDepth > 0) {
            populateContentPaths(contentPaths, pageContentPaths, httpReader, jsonJcrParser, host, maxDAMDepth);
        }
    }

    /**
     * Populates the {@code Set} that contains the {@code JCR DAM} content paths.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param pageContentPaths {@code Set} that contains the page content paths.
     * @param httpReader {@code HttpReader} to use to retrieve content.
     * @param jsonJcrParser {@code JsonJcrParser} to use to parse the {@code JSON} representation of {@code JCR} data.
     * @param host Name of the host to retrieve content from.
     * @param maxDAMDepth Maximum depth to retrieve {@code JCR DAM} content paths for.
     */
    private static void populateContentPaths(final Set<String> contentPaths, final Set<String> pageContentPaths,
            final HttpReader httpReader, final JsonJcrParser jsonJcrParser, final String host, final int maxDAMDepth) {
        Set<String> processedContentPaths = new HashSet<String>();
        for (String pageContentPath : pageContentPaths) {
            LOGGER.log(Level.INFO, "Processing page content path: {0}", pageContentPath);
            Set<String> pageContents = jsonJcrParser.getContentPaths(pageContentPath,
                    httpReader.getData(host + pageContentPath + "/jcr:content.infinity.json"));
            for (String pageContent : pageContents) {
                LOGGER.log(Level.FINE, "Adding content path: {0}", pageContent);
                contentPaths.add(pageContent);
                if (!processedContentPaths.contains(pageContent)) {
                    damDepth = 0;
                    populateContentPaths(contentPaths, processedContentPaths, pageContent, httpReader, jsonJcrParser,
                            host, maxDAMDepth);
                }
            }
        }
    }

    /**
     * Populates the {@code Set} that contains the {@code JCR DAM} content paths.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param processedContentPaths {@code Set} that contains the already processed {@code JCR DAM} content paths.
     * @param contentPath Path to retrieve content from.
     * @param httpReader {@code HttpReader} to use to retrieve content.
     * @param jsonJcrParser {@code JsonJcrParser} to use to parse the {@code JSON} representation of {@code JCR} data.
     * @param host Name of the host to retrieve content from.
     * @param maxDAMDepth Maximum depth to retrieve {@code JCR DAM} content paths for.
     */
    private static void populateContentPaths(final Set<String> contentPaths, final Set<String> processedContentPaths,
            final String contentPath, final HttpReader httpReader, final JsonJcrParser jsonJcrParser, final String host,
            final int maxDAMDepth) {
        processedContentPaths.add(contentPath);
        LOGGER.log(Level.FINE, "Processing content path: {0}", contentPath);
        Set<String> paths = jsonJcrParser
                .getContentPaths(contentPath, httpReader.getData(host + contentPath + "/jcr:content.infinity.json"));
        for (String path : paths) {
            LOGGER.log(Level.FINE, "Adding content path: {0}", contentPath);
            contentPaths.add(path);
            if ((!processedContentPaths.contains(path)) && (damDepth < maxDAMDepth)) {
                damDepth++;
                populateContentPaths(contentPaths, processedContentPaths, path, httpReader, jsonJcrParser, host,
                        maxDAMDepth);
            }
        }
        damDepth--;
    }

    /**
     * Populates the {@code Set} that contains the page content paths which represent all of the pages that content can
     * exist for starting at the specified path.
     *
     * @param pageContentPaths {@code Set} that contains the page content paths.
     * @param httpReader {@code HttpReader} to use to retrieve content.
     * @param jsonJcrParser {@code JsonJcrParser} to use to parse the {@code JSON} representation of {@code JCR} data.
     * @param host Name of the host to retrieve content from.
     * @param path Path to retrieve content from.
     * @param maxPageDepth Maximum depth to retrieve page content paths for.
     */
    private static void populatePageContentPaths(final Set<String> pageContentPaths, final HttpReader httpReader,
            final JsonJcrParser jsonJcrParser, final String host, final String path, int maxPageDepth) {
        Set<String> deeperPageContentPaths;
        if (path.matches(".*(\\.\\d*)?\\.json")) {
            deeperPageContentPaths = jsonJcrParser
                    .getPageContentPaths(path.replaceFirst("(\\.\\d*)?\\.json", ""), httpReader.getData(host + path));
        } else {
            deeperPageContentPaths =
                    jsonJcrParser.getPageContentPaths(path, httpReader.getData(host + path + ".infinity.json"));
        }
        for (String deeperPageContentPath : deeperPageContentPaths) {
            if (!deeperPageContentPath.matches(".*(\\.\\d*)?\\.json")) {
                LOGGER.log(Level.INFO, "Adding page content path: {0}", deeperPageContentPath);
                pageContentPaths.add(deeperPageContentPath);
            }
            if (pageDepth < maxPageDepth) {
                pageDepth++;
                populatePageContentPaths(pageContentPaths, httpReader, jsonJcrParser, host, deeperPageContentPath,
                        maxPageDepth);
            }
        }
    }

    /**
     * Writes the {@code CRX} package definitions.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with.
     * @param packageName Name of the package to create.
     */
    private static void writePackages(final Set<String> contentPaths, final String username, final String packageName) {
        int count = 0;
        Set<String> subContentPaths = new HashSet<String>();
        for (Iterator<String> contentPathsIterator = contentPaths.iterator(); contentPathsIterator.hasNext(); ) {
            if (count >= MAXIMUM_PACKAGE_CONTENT_PATHS) {
                numberOfPackages++;
                writePackage(subContentPaths, username, packageName + "_" + numberOfPackages);
                subContentPaths = new HashSet<String>();
                count = 0;
            }
            subContentPaths.add(contentPathsIterator.next());
            count++;
        }
        if (!subContentPaths.isEmpty()) {
            numberOfPackages++;
            writePackage(subContentPaths, username, packageName + "_" + numberOfPackages);
        }
    }

    /**
     * Writes the files necessary for the {@code CRX} package definition.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with.
     * @param packageName Name of the package to create.
     */
    private static void writePackage(final Set<String> contentPaths, final String username, final String packageName) {
        PackageFileWriter packageFileWriter = new PackageFileWriter();
        packageFileWriter.writePackageFiles(contentPaths, username, packageName);
        PackageFileZipper packageFileZipper = new PackageFileZipper();
        packageFileZipper.createPackageZip(packageName);
    }

}
