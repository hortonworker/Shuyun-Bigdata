package com.shuyun.datasync.domain;

import com.shuyun.datasync.common.SyncStrategyType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class TaskConfig implements Serializable{
    private long id;
    private String taskName;
    private String database;
    private String tableName;
    private int parallelSize = 1;
    private List<ColumnMapping> columnMapping;
    private String bucketColumn;
    private int bucketSize;
    private String fileType;
    private SyncStrategyType syncStrategy = SyncStrategyType.SERIAL_COVER_ALWAYS;
    private long strategySplitCount = 1000;
    private Map<String, String> tblproperties;
    private Map<String, String> sparkConfigProperties;

    public TaskConfig() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnMapping> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(List<ColumnMapping> columnMapping) {
        this.columnMapping = columnMapping;
    }

    public SyncStrategyType getSyncStrategy() {
        return syncStrategy;
    }

    public void setSyncStrategy(SyncStrategyType syncStrategy) {
        this.syncStrategy = syncStrategy;
    }

    public long getStrategySplitCount() {
        return strategySplitCount;
    }

    public void setStrategySplitCount(long strategySplitCount) {
        this.strategySplitCount = strategySplitCount;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public void setBucketSize(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    public String getBucketColumn() {
        return bucketColumn;
    }

    public void setBucketColumn(String bucketColumn) {
        this.bucketColumn = bucketColumn;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Map<String, String> getTblproperties() {
        return tblproperties;
    }

    public void setTblproperties(Map<String, String> tblproperties) {
        this.tblproperties = tblproperties;
    }

    public Map<String, String> getSparkConfigProperties() {
        return sparkConfigProperties;
    }

    public int getParallelSize() {
        return parallelSize;
    }

    public void setParallelSize(int parallelSize) {
        this.parallelSize = parallelSize;
    }

    public void setSparkConfigProperties(Map<String, String> sparkConfigProperties) {
        this.sparkConfigProperties = sparkConfigProperties;
    }
}
