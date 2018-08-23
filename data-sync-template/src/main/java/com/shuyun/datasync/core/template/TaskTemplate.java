package com.shuyun.datasync.core.template;

import com.shuyun.datasync.common.SyncStrategyType;
import com.shuyun.datasync.core.HbaseMetaManager;
import com.shuyun.datasync.core.TaskConfigManager;
import com.shuyun.datasync.core.template.strategy.CoverOrUpdateSyncStrategySerial;
import com.shuyun.datasync.core.template.strategy.CoverSyncStrategySerial;
import com.shuyun.datasync.core.template.strategy.CoverSyncStrategyParallel;
import com.shuyun.datasync.domain.TaskConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class TaskTemplate {

    private static Logger logger = Logger.getLogger(TaskTemplate.class);

    public static void execute(String configId) throws Exception {

        TaskConfig taskConfig = TaskConfigManager.loadConfig(configId);
        if(taskConfig == null) {
            logger.error("task config is null!");
            throw new Exception("task config is null!");
        }

        List<String> tables = HbaseMetaManager.getSubTables(taskConfig);
        if(CollectionUtils.isEmpty(tables)) {
            logger.error("hbase table is null!");
            throw new Exception("hbase table is null!");
        }
        switch (taskConfig.getSyncStrategy()) {
            case SERIAL_COVER_ALWAYS:
                CoverSyncStrategySerial.handle(tables, taskConfig);
                break;
            case PARALLEL_COVER_ALWAYS:
                CoverSyncStrategyParallel.handle(tables, taskConfig);
                break;
            case SERIAL_COVER_OR_UPDATE_BY_COUNT:
                CoverOrUpdateSyncStrategySerial.handle();
                break;
            default:
                logger.error("strategy error!");
                throw new Exception("strategy error!");
        }
    }
}
