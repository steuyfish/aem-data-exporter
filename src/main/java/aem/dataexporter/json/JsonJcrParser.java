package aem.dataexporter.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for the {@code JSON} representation of {@code JCR} data.
 */
public class JsonJcrParser {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonJcrParser.class.getName());

    /**
     * Returns the content paths extracted from the specified {@code JCR} data.
     *
     * @param path Path {@code JSON} data was retrieved from.
     * @param data {@code JSON} representation of {@code JCR} data.
     * @return Content paths extracted from the specified {@code JCR} data.
     */
    public final Set<String> getContentPaths(final String path, final byte[] data) {
        Set<String> contentPaths = new HashSet<String>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            if (jsonNode.isArray()) {
                // The first entry will be the most specific.
                // The CQ json will return numerous versions if the ".inifinity" version is going to be too big. Since
                // we always want the most specific one, it will always be the first entry (i.e. <path>.3.json).
                contentPaths.add(jsonNode.get(0).textValue());
            } else if (jsonNode.isObject()) {
                contentPaths.addAll(getJcrContentPaths(jsonNode));
            }
        } catch (IOException e) {
            // This only needs fine logging since it is caused by missing asset information, and the JCR returning 404.
            LOGGER.log(Level.FINE, "Unable to parse JSON data at: [{0}], [{1}]. {2}",
                    new String[]{path, new String(data), e.getMessage()});
        }
        return contentPaths;
    }

    /**
     * Returns the page content paths extracted from the specified {@code JCR} data.
     *
     * @param path Path {@code JSON} data was retrieved from.
     * @param data {@code JSON} representation of {@code JCR} data.
     * @return Page content paths extracted from the specified {@code JCR} data.
     */
    public final Set<String> getPageContentPaths(final String path, final byte[] data) {
        Set<String> pageContentPaths = new HashSet<String>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            if (jsonNode.isArray()) {
                // The first entry will be the most specific.
                // The CQ json will return numerous versions if the ".inifinity" version is going to be too big. Since
                // we always want the most specific one, it will always be the first entry (i.e. <path>.3.json).
                pageContentPaths.add(jsonNode.get(0).textValue());
            } else if (jsonNode.isObject()) {
                pageContentPaths.addAll(getPageContentPaths(path, jsonNode));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to parse JSON data at: [{0}], [{1}]. {2}",
                    new String[]{path, new String(data), e.getMessage()});
        }
        return pageContentPaths;
    }

    /**
     * Returns an {@code Set} that contains the content paths for {@code DAM} based content which is referenced within
     * the {@code jcr:content} of the specified {@code JsonNode}.
     *
     * @param jsonNode {@code JsonNode} to determine content paths for {@code DAM} based content for.
     * @return {@code Set} that contains the content paths for {@code DAM} based content which is referenced within the
     * {@code jcr:content} of the specified {@code JsonNode}.
     */
    private Set<String> getJcrContentPaths(final JsonNode jsonNode) {
        Set<String> jcrContentPaths = new HashSet<String>();
        for (Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields(); fields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isObject()) {
                jcrContentPaths.addAll(getJcrContentPaths(field.getValue()));
            } else if ((field.getValue().isTextual()) && (field.getValue().textValue().startsWith("/content/dam"))) {
                jcrContentPaths.add(field.getValue().textValue());
            }
        }
        return jcrContentPaths;
    }

    /**
     * Returns an {@code Set} that contains the content paths for the appropriate pages extracted from the specified
     * {@code JsonNode}.
     *
     * @param path Path {@code JSON} data was retrieved from.
     * @param jsonNode {@code JsonNode} to determine page content paths for.
     * @return {@code Set} that contains the content paths for the appropriate pages extracted from the specified {@code
     * JsonNode}.
     */
    private Set<String> getPageContentPaths(final String path, final JsonNode jsonNode) {
        Set<String> pageContentPaths = new HashSet<String>();
        for (Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields(); fields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = fields.next();
            // If the field represents an actual page, and not either jcr or sling properties (or contains a file extension).
            if (StringUtils.isNotBlank(field.getKey()) &&
                    ((!field.getKey().matches(".*(cq:|jcr:|sling:).*")) || (field.getKey().contains("."))) &&
                    (isPageNode(field.getValue()))) {
                pageContentPaths.add(path + "/" + field.getKey());
            }
        }
        return pageContentPaths;
    }

    /**
     * Returns a boolean indicating whether or not the specified {@code JsonNode} represents an {@code cq:Page}.
     *
     * @param jsonNode {@code JsonNode} to test whether or not it represents an {@code cq:Page}.
     * @return True if the {@code JsonNode} represents an {@code cq:Page}; otherwise false.
     */
    private boolean isPageNode(final JsonNode jsonNode) {
        return ((jsonNode.has("jcr:primaryType")) &&
                ("cq:Page").equalsIgnoreCase(jsonNode.get("jcr:primaryType").textValue()));
    }
}
