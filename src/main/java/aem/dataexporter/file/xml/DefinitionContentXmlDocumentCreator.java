package aem.dataexporter.file.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * Implementation of an {@code DocumentCreator} for creating the {@code definition/.content.xml}.
 */
public class DefinitionContentXmlDocumentCreator implements DocumentCreator {

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
        Element root = document.createElement("jcr:root");
        root.setAttribute("xmlns:vlt", "http://www.day.com/jcr/vault/1.0");
        root.setAttribute("xmlns:jcr", "http://www.jcp.org/jcr/1.0");
        root.setAttribute("xmlns:nt", "http://www.jcp.org/jcr/nt/1.0");
        root.setAttribute("jcr:created", "{Date}" + simpleDateFormat.format(currentDate));
        root.setAttribute("jcr:createdBy", username);
        root.setAttribute("jcr:lastModified", "{Date}" + simpleDateFormat.format(currentDate));
        root.setAttribute("jcr:lastModifiedBy", username);
        root.setAttribute("jcr:primaryType", "vlt:PackageDefinition");
        root.setAttribute("buildCount", "1");
        root.setAttribute("group", "data_exporter_packages");
        root.setAttribute("lastUnwrapped", "{Date}" + simpleDateFormat.format(currentDate));
        root.setAttribute("lastUnwrappedBy", username);
        root.setAttribute("lastWrapped", "{Date}" + simpleDateFormat.format(currentDate));
        root.setAttribute("lastWrappedBy", username);
        root.setAttribute("name", packageName);
        root.setAttribute("version", "");
        document.appendChild(root);
        return document;
    }

}
