package example.storm.phoenix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logic center for handling configurable values.
 */
public class Configuration {

    /** Logger */
    private static Logger log = Logger.getLogger(Configuration.class.getName());

    /* For versatility, there are two concepts that are both used in conjunction.
     * 1) A local configuration file on the webserver that drives configuration of the platform,
     * managed by the server administrator. For deployment on tomcat, this would creating/updating
     * the setenv.(sh|bat) file, adding to CATALINA_OPTS
     * 2) configuration files within the codebase that are managed by the developers, triggered by
     * a concept of 'runmode'
     * 3) environment variables (as these are primarily what is within Azure
     * 1 & 2 are available for testing purposes, though #3 is what is used in "production".
     * */

    /**
     * System property provided in the java environment to indicate
     * the configuration file that should be read from for values, if specified.
     * This is mostly useful for storing passwords for systems that developers
     * should not be concerned with. */
    static final String SYSPROP_LOCAL_CONFIG_FILE = "config.dev.properties";

    /**
     * Property name for the runmode that the server is running under.
     * This can be specified as either a System Property, or within the server-local
     * configuration file.
     */
    static final String PROPNAME_RUN_MODE = "storm-phoenix-example.runmode";

    /** Default run mode */
    static final String DEFAULT_RUN_MODE = "dev";

    /** Run mode */
    private static String RUN_MODE;

    /** The configured values */
    private static Properties CONFIG_VALUES;

    @SuppressWarnings("unchecked")
    public static Collection<String> getConfigKeys() {
        return Collections.list((Enumeration<String>) CONFIG_VALUES.propertyNames());
    }

    /**
     * Retrieve a possibly configured value
     * @param key the name of a possibly configured value.
     * @return the value matching the key, or {@code null} if one is not defined.
     */
    public static String getConfigValue(String key) {
        init();
        // convert aaa.bbb.ccc to AAA_BBB_CCC for environment variable
        String value = System.getenv(key.toUpperCase().replaceAll("\\.", "_"));
        if (value == null) {
            // if there is no value in environment variable, try by the config file
            value = CONFIG_VALUES.getProperty(key);
        }
        return value;
    }

    /**
     * Retrieve a possibly configured value
     * @param key the name of a possibly configured value.
     * @param defaultValue the value to utilize if the key is not configured.
     * @return the value matching the key, or {@code defaultValue} if one is not defined.
     */
    public static String getConfigValue(String key, String defaultValue) {
        init();
        String value = getConfigValue(key);
        value = Utils.defaultValue(value, defaultValue);
        return value;
    }

    /**
     * Retrieve the run mode of the server
     * @return run mode of the server
     */
    public static String getRunMode() {
        if (RUN_MODE != null) {
            return RUN_MODE;
        }

        // first get by config file.
        String runModesStr = getConfigValue(PROPNAME_RUN_MODE);
        if (runModesStr == null) {
            // if not in config file, try by system property, default to dev if not available
            runModesStr = System.getProperty(PROPNAME_RUN_MODE);

            // default to the dev run mode if one is not supplied
            if (runModesStr == null || runModesStr.isEmpty()) {
                runModesStr = DEFAULT_RUN_MODE;
            }
        }

        RUN_MODE = runModesStr;
        return RUN_MODE;
    }

    /**
     * Perform delayed initialization processes.
     */
    private static synchronized void init() {
        if (CONFIG_VALUES != null) {
            return;
        }

        CONFIG_VALUES = new Properties();
        processLocalConfig();
        processIncludedConfig();
    }

    /**
     * Process the system local configuration file, if applicable
     */
    private static void processLocalConfig() {
        String localConfigFile = getConfigValue(SYSPROP_LOCAL_CONFIG_FILE);
        if (localConfigFile == null || localConfigFile.isEmpty()) {
            log.log(Level.FINE, "No local configuration defined, skipping associated processing");
            return;
        }

        log.log(Level.FINE, "Processing configuration file {0}", localConfigFile);
        Path p = Paths.get(localConfigFile);
        if (!Files.exists(p)) {
            log.log(Level.WARNING, "Path {0} does not exist", p.toString());
            return;
        } else if (!Files.isRegularFile(p)) {
            log.log(Level.WARNING, "Path {0} is not a file", p.toString());
            return;
        } else if (!Files.isReadable(p)) {
            log.log(Level.WARNING, "File {0} is not readable", p.toString());
            return;
        }

        Properties prop = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            prop.load(reader);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error occurred while reading " + p.toString(), e);
        }
        CONFIG_VALUES.putAll(prop);
    }

    /**
     * Process the included configuration file, if applicable
     */
    private static void processIncludedConfig() {
        String configName = "config." + getRunMode() + ".properties";

        //relative path cannot be recognized and not figure out the reason, so here use absolute path instead
        InputStream configStream = Configuration.class.getResourceAsStream("/"+configName);
        if (configStream == null) {
            log.log(Level.WARNING, "configuration resource {0} is missing", configName);
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(configStream, StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.load(reader);
            CONFIG_VALUES.putAll(props);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to process configuration {0}", configName);
        }

    }

    /**
     * Clear the configuration cache, allowing it to be recalculated with any updated data.
     */
    static void clearCache() {
        CONFIG_VALUES = null;
        RUN_MODE = null;
    }
}
