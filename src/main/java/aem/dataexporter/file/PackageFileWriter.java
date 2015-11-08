package aem.dataexporter.file;

import aem.dataexporter.file.xml.DefinitionContentXmlDocumentCreator;
import aem.dataexporter.file.xml.DocumentCreator;
import aem.dataexporter.file.xml.FilterXmlDocumentCreator;
import aem.dataexporter.file.xml.PropertiesXmlDocumentCreator;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes the files necessary for the {@code CRX} package definition.
 */
public class PackageFileWriter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PackageFileWriter.class.getName());
    /**
     * {@code DocumentCreator} for creating the {@code definition/.content.xml}.
     */
    private DocumentCreator definitionContentXmlDocumentCreator;
    /**
     * {@code DocumentCreator} for creating the {@code filter.xml}.
     */
    private DocumentCreator filterXmlDocumentCreator;
    /**
     * {@code DocumentCreator} for creating the {@code properties.xml}.
     */
    private DocumentCreator propertiesXmlDocumentCreator;

    /**
     * Constructs a new {@code PackageFileWriter}.
     */
    public PackageFileWriter() {
        definitionContentXmlDocumentCreator = new DefinitionContentXmlDocumentCreator();
        filterXmlDocumentCreator = new FilterXmlDocumentCreator();
        propertiesXmlDocumentCreator = new PropertiesXmlDocumentCreator();
    }

    /**
     * Writes the files necessary for the {@code CRX} package definition.
     *
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with.
     * @param packageName Name of the package to create.
     */
    public final void writePackageFiles(final Set<String> contentPaths, final String username,
            final String packageName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            writeFile(definitionContentXmlDocumentCreator, documentBuilder, transformer, contentPaths, username,
                    packageName, "src/main/resources/META-INF/vault/definition/.content.xml");
            writeFile(filterXmlDocumentCreator, documentBuilder, transformer, contentPaths, username, packageName,
                    "src/main/resources/META-INF/vault/filter.xml");
            writePropertiesXmlFile(documentBuilder, transformer, contentPaths, username, packageName);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Unable to write package files: {0}", e.getMessage());
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, "Unable to write package files: {0}", e.getMessage());
        }
    }

    /**
     * Writes a file.
     *
     * @param documentCreator {@code DocumentCreator} to use to populate the {@code Document}.
     * @param documentBuilder {@code DocumentBuilder} to use to create the {@code Document}.
     * @param transformer {@code Transformer} to use to transform the {@code Document} into {@code XML}.
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with.
     * @param packageName Name of the package to create.
     * @param filename Name of the file.
     * @throws TransformerException If an error occurs transforming the {@code Document} into {@code XML}.
     */
    private void writeFile(final DocumentCreator documentCreator, final DocumentBuilder documentBuilder,
            final Transformer transformer, final Set<String> contentPaths, final String username,
            final String packageName, final String filename) throws TransformerException {
        DOMSource domSource =
                new DOMSource(documentCreator.getDocument(documentBuilder, contentPaths, username, packageName));
        String directoryPath = filename.substring(0, filename.lastIndexOf("/"));
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            LOGGER.log(Level.INFO, "Directory not found, creating directory: " + directoryPath);
            directory.mkdir();
        }
        StreamResult streamResult = new StreamResult(new File(filename));
        transformer.transform(domSource, streamResult);
    }

    /**
     * Writes the {@code properties.xml} file.
     *
     * @param documentBuilder {@code DocumentBuilder} to use to create the {@code Document}.
     * @param transformer {@code Transformer} to use to transform the {@code Document} into {@code XML}.
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with.
     * @param packageName Name of the package to create.
     * @throws TransformerException If an error occurs transforming the {@code Document} into {@code XML}.
     */
    private void writePropertiesXmlFile(final DocumentBuilder documentBuilder, final Transformer transformer,
            final Set<String> contentPaths, final String username, final String packageName)
            throws TransformerException {
        Document propertiesXmlDocument =
                propertiesXmlDocumentCreator.getDocument(documentBuilder, contentPaths, username, packageName);
        DOMImplementation domImplementation = propertiesXmlDocument.getImplementation();
        DocumentType documentType =
                domImplementation.createDocumentType("doctype", "", "http://java.sun.com/dtd/properties.dtd");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());
        DOMSource domSource = new DOMSource(propertiesXmlDocument);
        StreamResult streamResult = new StreamResult(new File("src/main/resources/META-INF/vault/properties.xml"));
        transformer.transform(domSource, streamResult);
    }

}
