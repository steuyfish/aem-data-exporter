package aem.dataexporter.file.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * Implementation of an {@code DocumentCreator} for creating the {@code properties.xml}.
 */
public class PropertiesXmlDocumentCreator implements DocumentCreator {

    /**
     * {@inheritDoc}.
     */
    @Override
    public final Document getDocument(final DocumentBuilder documentBuilder, final Set<String> contentPaths,
            final String username, final String packageName) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date currentDate = new Date();
        Document document = documentBuilder.newDocument();
        Element properties = document.createElement("properties");
        document.appendChild(properties);
        Element comment = document.createElement("comment");
        comment.setTextContent("FileVault Package Properties");
        properties.appendChild(comment);
        appendEntry(document, properties, "createdBy", username);
        appendEntry(document, properties, "name", packageName);
        appendEntry(document, properties, "lastModified", simpleDateFormat.format(currentDate));
        appendEntry(document, properties, "lastModifiedBy", username);
        appendEntry(document, properties, "created", simpleDateFormat.format(currentDate));
        appendEntry(document, properties, "buildCount", "1");
        appendEntry(document, properties, "packageFormatVersion", "2");
        appendEntry(document, properties, "group", "data_exporter_packages");
        appendEntry(document, properties, "lastWrapped", simpleDateFormat.format(currentDate));
        appendEntry(document, properties, "lastWrappedBy", username);
        Element entry = document.createElement("entry");
        entry.setAttribute("key", "version");
        properties.appendChild(entry);
        entry = document.createElement("entry");
        entry.setAttribute("key", "dependencies");
        properties.appendChild(entry);
        return document;
    }

    /**
     * Appends an {@code Entry} element into the specified {@code Element}.
     *
     * @param document {@code Document} to use to create the {@code Entry} element.
     * @param element {@code Element} to append the {@code Entry} element to.
     * @param key Value of the {@code key} attribute on the {@code Entry}.
     * @param value Value of the {@code Entry} element.
     */
    private void appendEntry(final Document document, final Element element, final String key, final String value) {
        Element entry = document.createElement("entry");
        entry.setAttribute("key", key);
        entry.setTextContent(value);
        element.appendChild(entry);
    }

}
