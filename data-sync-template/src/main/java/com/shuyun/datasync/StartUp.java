package com.shuyun.datasync;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.core.template.TaskTemplate;
import org.apache.log4j.Logger;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class StartUp {

    private static Logger logger = Logger.getLogger(StartUp.class);

    public static void main(String[] args) {
        String runMode = null;
        String configId = null;
        try {
            runMode = args[0];
            configId = args[1];
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            logger.error("arguments is blank!", e);
            System.exit(1);
        }

        AppConfiguration.loadConfiguration(runMode);

        try {
            TaskTemplate.execute(configId, runMode);
        } catch (Exception e) {
            logger.error("execute task error!", e);
            System.exit(1);
        }

    }
}
