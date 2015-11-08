package aem.dataexporter.file.xml;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;

import java.util.Set;

/**
 * Interface for the creation of {@code Document}s that represents the {@code XML} content for the appropriate file
 * types.
 */
public interface DocumentCreator {

    /**
     * Returns an {@code Document} that represents the {@code XML} content for the appropriate file type.
     *
     * @param documentBuilder {@code DocumentBuilder} to use to create the {@code Document}.
     * @param contentPaths {@code Set} that contains the {@code JCR DAM} content paths.
     * @param username Username to authenticate with
     * @param packageName Name of the package to create.
     * @return {@code Document} that represents the {@code XML} content for the appropriate file type.
     */
    Document getDocument(final DocumentBuilder documentBuilder, final Set<String> contentPaths, final String username,
            final String packageName);

}
