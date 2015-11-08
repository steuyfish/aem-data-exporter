package aem.dataexporter;

import aem.dataexporter.operations.PackageBuilder;
import aem.dataexporter.operations.PackageCreator;
import aem.dataexporter.operations.PackageDownloader;
import aem.dataexporter.operations.PackageRemover;
import aem.dataexporter.operations.PackageUploader;
import aem.dataexporter.utilities.ProgramArgument;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Data exporter for the {@code JCR}.
 * <p>
 * The {@code JCR} is read for the specified host and path, and then a package definition is created for the {@code DAM}
 * content that is contained within that specified path. The code will recursively look through all referenced content
 * in order to ensure that the full data set is exported.
 * <p>
 * The exporter expects the following program arguments: <ul> <li>host</li> <li>path</li> <li>username</li>
 * <li>password</li> <li>packageName</li> <li>maxPageDepth - i.e. how deep to parse the page structure</li>
 * <li>maxDAMDepth - i.e. how deep to parse the dam structure</li></ul>
 */
public class DataExporter {

    /**
     * Working directory.
     */
    public static final String WORKING_DIRECTORY = "src/main/resources/";

    /**
     * Run the data exporter.
     *
     * @param args Array of program arguments.
     */
    public static void main(final String[] args) {
        PackageCreator.main(args);
        String[] updatedArgs = ArrayUtils.add(args, "-" + ProgramArgument.NUMBER_OF_PACKAGES.getKey());
        updatedArgs = ArrayUtils.add(updatedArgs, String.valueOf(PackageCreator.numberOfPackages));
        PackageUploader.main(updatedArgs);
        PackageBuilder.main(updatedArgs);
        PackageDownloader.main(updatedArgs);
        PackageRemover.main(updatedArgs);
        System.out.print(PackageCreator.numberOfPackages);
    }

}
