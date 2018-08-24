package com.shuyun.datasync.utils;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

/**
 * Created by jiawei.guo on 2018/8/24.
 */
public class DataTypeConvert {

    public static String sparkTypeToHiveType(DataType sparkType) {
        if(sparkType.equals(DataTypes.LongType)) {
            return "bigint";
        }
        return sparkType.typeName();
    }
}
