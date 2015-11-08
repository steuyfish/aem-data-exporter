package aem.dataexporter.file.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;

import java.util.Set;

/**
 * Implementation of an {@code DocumentCreator} for creating the {@code filter.xml}.
 */
public class FilterXmlDocumentCreator implements DocumentCreator {

    /**
     * {@inheritDoc}.
     */
    @Override
    public final Document getDocument(final DocumentBuilder documentBuilder, final Set<String> contentPaths,
            final String username, final String packageName) {
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("workspaceFilter");
        root.setAttribute("version", "1.0");
        document.appendChild(root);
        for (String path : contentPaths) {
            Element filter = document.createElement("filter");
            filter.setAttribute("root", path);
            root.appendChild(filter);
        }
        return document;
    }

}
