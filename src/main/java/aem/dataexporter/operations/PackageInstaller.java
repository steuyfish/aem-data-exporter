package aem.dataexporter.operations;

import aem.dataexporter.http.HttpReader;
import aem.dataexporter.json.JsonSimplePackageManagerResponse;
import aem.dataexporter.utilities.ProgramArgument;
import aem.dataexporter.utilities.ProgramArguments;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Package uploader for the {@code DataExporter}.
 * <p>
 * This class is designed to supplement the {@code DataExporter} by allowing the ability to individually run specific
 * operations should the entire data export process fail.
 * <p>
 * The package creator expects the following program arguments: <ul> <li>host</li> <li>username</li> <li>password</li>
 * <li>packageName</li> <li>numberOfPackages</li> </ul>
 */
public class PackageInstaller {

    private static final Logger LOGGER = Logger.getLogger(PackageInstaller.class.getName());
    private final static ProgramArgument[] REQUIRED_ARGUMENTS =
            {ProgramArgument.HOST, ProgramArgument.USERNAME, ProgramArgument.PASSWORD, ProgramArgument.PACKAGE_NAME};

    /**
     * Run the package uploader.
     *
     * @param args Array of program arguments.
     */
    public static void main(String[] args) {
        ProgramArguments programArguments = new ProgramArguments(args, REQUIRED_ARGUMENTS);
        if (!programArguments.hasRequiredArguments()) {
            programArguments.printPackageUsageMessage();
            System.exit(-1);
        }
        HttpReader httpReader = new HttpReader(programArguments.get(ProgramArgument.USERNAME),
                programArguments.get(ProgramArgument.PASSWORD), programArguments.get(ProgramArgument.HOSTNAME),
                programArguments.get(ProgramArgument.PORT));
        int numberOfPackages = Integer.parseInt(programArguments.get(ProgramArgument.NUMBER_OF_PACKAGES));
        for (int index = 1; index <= numberOfPackages; index++) {
            String responseString = httpReader.postAndGetResponseString(programArguments.get(ProgramArgument.HOST) +
                    "/crx/packmgr/service/.json/etc/packages/data_exporter_packages/" +
                    programArguments.get(ProgramArgument.PACKAGE_NAME) + "_" + index + ".zip?cmd=install");
            if (responseString == null ||
                    !JsonSimplePackageManagerResponse.validateAndLogResponse(responseString, "Installation")) {
                LOGGER.log(Level.SEVERE, "Unable to install package.");
                System.exit(-1);
            }
        }
    }
}
