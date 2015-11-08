package aem.dataexporter.operations;

import aem.dataexporter.http.HttpReader;
import aem.dataexporter.utilities.ProgramArgument;
import aem.dataexporter.utilities.ProgramArguments;

/**
 * Package remover for the {@code DataExporter}.
 * <p>
 * This class is designed to supplement the {@code DataExporter} by allowing the ability to individually run specific
 * operations should the entire data export process fail.
 * <p>
 * The package remover expects the following program arguments: <ul> <li>host</li> <li>username</li> <li>password</li>
 * <li>packageName</li> <li>numberOfPackages</li> </ul>
 */
public class PackageRemover {

    /**
     * Required program arguments.
     */
    private static final ProgramArgument[] REQUIRED_ARGUMENTS =
            {ProgramArgument.HOST, ProgramArgument.USERNAME, ProgramArgument.PASSWORD, ProgramArgument.PACKAGE_NAME};

    /**
     * Run the package remover.
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
        for (int index = 1; index <= numberOfPackages; index++) {
            boolean success = httpReader
                    .post(programArguments.get(ProgramArgument.HOST) + "/crx/packmgr/service.jsp?cmd=rm&name=" +
                            programArguments.get(ProgramArgument.PACKAGE_NAME) + "_" + index +
                            "&group=data_exporter_packages");
            if (!success) {
                System.exit(-1);
            }
        }
    }

}
