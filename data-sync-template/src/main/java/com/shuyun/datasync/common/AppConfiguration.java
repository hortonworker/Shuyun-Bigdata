package com.shuyun.datasync.common;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class AppConfiguration {

    private static Logger logger = Logger.getLogger(AppConfiguration.class);

    private final static Properties properties = new Properties();

    public static void loadConfiguration(String runMode) {
        InputStream is = AppConfiguration.class.getResourceAsStream("/config." + runMode + ".properties");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            properties.load(br);
        } catch (Exception e) {
            logger.error("load config error!", e);
        }

    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
