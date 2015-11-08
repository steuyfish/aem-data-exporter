package aem.dataexporter.operations;

import aem.dataexporter.DataExporter;
import aem.dataexporter.http.HttpReader;
import aem.dataexporter.utilities.ProgramArgument;
import aem.dataexporter.utilities.ProgramArguments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Package downloader for the {@code DataExporter}.
 * <p>
 * This class is designed to supplement the {@code DataExporter} by allowing the ability to individually run specific
 * operations should the entire data export process fail.
 * <p>
 * The package downloader expects the following program arguments: <ul> <li>host</li> <li>path</li> <li>username</li>
 * <li>password</li> <li>packageName</li> <li>numberOfPackages</li> <li>maxPageDepth - i.e. how deep to parse the page
 * structure</li> <li>maxDAMDepth - i.e. how deep to parse the dam structure</li> <li>workingDirectory</li></ul>
 */
public class PackageDownloader {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PackageDownloader.class.getName());
    /**
     * Required program arguments.
     */
    private static final ProgramArgument[] REQUIRED_ARGUMENTS =
            {ProgramArgument.HOST, ProgramArgument.USERNAME, ProgramArgument.PASSWORD, ProgramArgument.PACKAGE_NAME};

    /**
     * Run the package downloader.
     *
     * @param args Array of program arguments.
     */
    public static void main(final String[] args) {
        ProgramArguments programArguments = new ProgramArguments(args, REQUIRED_ARGUMENTS);
        if (!programArguments.hasRequiredArguments()) {
            programArguments.printPackageUsageMessage();
            System.exit(-1);
        }
        HttpReader httpReader = new HttpReader(programArguments.get(ProgramArgument.USERNAME),
                programArguments.get(ProgramArgument.PASSWORD), programArguments.get(ProgramArgument.HOSTNAME),
                programArguments.get(ProgramArgument.PORT));
        int numberOfPackages = Integer.parseInt(programArguments.get(ProgramArgument.NUMBER_OF_PACKAGES));
        String workingDirectory = DataExporter.WORKING_DIRECTORY;
        if (programArguments.has(ProgramArgument.WORKING_DIRECTORY)) {
            workingDirectory = programArguments.get(ProgramArgument.WORKING_DIRECTORY);
        }
        for (int index = 1; index <= numberOfPackages; index++) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(
                        workingDirectory + programArguments.get(ProgramArgument.PACKAGE_NAME) + "_" + index + ".zip"));
                httpReader.getDataAndWriteToOutputSteam(
                        programArguments.get(ProgramArgument.HOST) + "/crx/packmgr/service.jsp?name=" +
                                programArguments.get(ProgramArgument.PACKAGE_NAME) + "_" + index +
                                "&group=data_exporter_packages", fileOutputStream);
                fileOutputStream.close();
                LOGGER.log(Level.INFO, "Finished downloading package [src/main/resources/{0}.zip",
                        programArguments.get(ProgramArgument.PACKAGE_NAME) + "_" + index);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to download package. {0}", e.getMessage());
                System.exit(-1);
            }
        }
    }

}
