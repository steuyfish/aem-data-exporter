package aem.dataexporter.file;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the {@code CRX} package definition.
 */
public class PackageFileZipper {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PackageFileZipper.class.getName());

    /**
     * Creates the {@code CRX} package definition.
     *
     * @param packageName Name of the package to create.
     */
    public final void createPackageZip(final String packageName) {
        try {
            ArchiveOutputStream archiveOutputStream =
                    new ZipArchiveOutputStream(new File("src/main/resources/" + packageName + ".zip"));
            addZipEntry(archiveOutputStream, "META-INF/vault/definition/.content.xml");
            addZipEntry(archiveOutputStream, "META-INF/vault/config.xml");
            addZipEntry(archiveOutputStream, "META-INF/vault/filter.xml");
            addZipEntry(archiveOutputStream, "META-INF/vault/nodetypes.cnd");
            addZipEntry(archiveOutputStream, "META-INF/vault/properties.xml");
            addZipEntry(archiveOutputStream, "jcr_root/.content.xml");
            archiveOutputStream.finish();
            archiveOutputStream.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to create package zip: {0}", e.getMessage());
        }
    }

    /**
     * Adds an entry to the zip.
     *
     * @param archiveOutputStream {@code ArchiveOutputStream} to output zip entry to.
     * @param filename Name of the file to add to the zip.
     * @throws IOException If an error occurs adding the file to the zip.
     */
    private void addZipEntry(final ArchiveOutputStream archiveOutputStream, final String filename) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            File file = new File("src/main/resources/" + filename);
            ArchiveEntry archiveEntry = archiveOutputStream.createArchiveEntry(file, filename);
            archiveOutputStream.putArchiveEntry(archiveEntry);
            fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, archiveOutputStream);
            archiveOutputStream.closeArchiveEntry();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to create zip entry for file: [{0}]. {1}",
                    new String[]{filename, e.getMessage()});
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

}
