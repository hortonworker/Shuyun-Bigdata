package com.shuyun.datasync.domain;

import org.apache.spark.sql.types.DataType;

import java.io.Serializable;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class ColumnMapping implements Serializable {
    private String family;
    private String hbaseColumn;
    private String hiveColumn;
    private DataType type;

    public ColumnMapping() {
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getHbaseColumn() {
        return hbaseColumn;
    }

    public void setHbaseColumn(String hbaseColumn) {
        this.hbaseColumn = hbaseColumn;
    }

    public String getHiveColumn() {
        return hiveColumn;
    }

    public void setHiveColumn(String hiveColumn) {
        this.hiveColumn = hiveColumn;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}
