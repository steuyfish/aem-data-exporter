package aem.dataexporter.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonSimplePackageManagerResponse {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonSimplePackageManagerResponse.class.getName());
    /**
     * Whether or not the package was successfully installed.
     */
    private boolean success;
    /**
     * Message accompanying the package installation.
     */
    private String message;
    /**
     * Path the package was installed at.
     */
    private String path;

    /**
     * Returns a boolean indicating whether or not the package was installed successfully.
     *
     * @return True if the package was installed successfully; otherwise false.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Sets a boolean indicating whether or not the package was installed successfully.
     *
     * @param value True if the package was installed successfully; otherwise false.
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Returns the message accompanying the package installation.
     *
     * @return Message accompanying the package installation.
     */
    @JsonProperty("msg")
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message accompanying the package installation.
     *
     * @param value Message accompanying the package installation.
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Returns the path the packages was installed at.
     *
     * @return Path the packages was installed at.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path the packages was installed at.
     *
     * @param value Path the packages was installed at.
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Returns an {@code JsonSimplePackageManagerResponse} that was mapped from the specified {@code JSON} response from
     * the {@code CRX} package manager.
     *
     * @param jsonPackMgrResponse {@code JSON} response from the {@code CRX} package manager.
     * @return {@code JsonSimplePackageManagerResponse} that was mapped from the specified {@code JSON} response from
     * the {@code CRX} package manager.
     */
    public static JsonSimplePackageManagerResponse mapJsonToObject(final String jsonPackMgrResponse) {
        ObjectMapper mapper = new ObjectMapper();
        JsonSimplePackageManagerResponse responseObject = new JsonSimplePackageManagerResponse();
        try {
            responseObject = mapper.readValue(jsonPackMgrResponse, JsonSimplePackageManagerResponse.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read Json from package manager response: . ",
                    new String[]{jsonPackMgrResponse, e.getMessage()});
        }
        return responseObject;
    }

    /**
     * Validates the {@code JSON} response from the {@code CRX} package manager.
     *
     * @param responseString {@code JSON} response from the {@code CRX} package manager.
     * @param actionName Name of the action that was performed.
     * @return Whether or not the {@code JSON} response from the {@code CRX} package manager was valid.
     */
    public static boolean validateAndLogResponse(final String responseString, final String actionName) {
        JsonSimplePackageManagerResponse responsePojo = null;
        if (responseString != null) {
            responsePojo = JsonSimplePackageManagerResponse.mapJsonToObject(responseString);
        }
        if (responsePojo == null) {
            LOGGER.log(Level.SEVERE, "Error: The {0} was unsuccessfull. unable to read response");
            return false;
        } else if (!responsePojo.getSuccess()) {
            LOGGER.log(Level.SEVERE, "Error: The {0} was unsuccessfull. Response message: {1}",
                    new String[]{actionName, responsePojo.getMessage()});
            return false;
        } else {
            LOGGER.log(Level.INFO, "Info: The {0} was successfull. Response message: {1}",
                    new String[]{actionName, responsePojo.getMessage()});
            return true;
        }
    }
}
