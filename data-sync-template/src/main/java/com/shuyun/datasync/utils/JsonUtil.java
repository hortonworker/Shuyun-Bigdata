package com.shuyun.datasync.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shuyun.datasync.common.*;
import org.apache.spark.sql.types.DataType;

/**
 * Created by jiawei.guo on 2018/8/24.
 */
public class JsonUtil {

    private static Gson gson = null;
    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FileType.class,new FileTypeSerializer());
        gsonBuilder.registerTypeAdapter(SyncStrategyType.class, new SyncStrategyTypeSerializer());
        gsonBuilder.registerTypeAdapter(DataType.class, new DataTypeSerializer());
        gson =gsonBuilder.create();
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T  fromJson(String json, Class<T> clz) {
        return gson.fromJson(json, clz);
    }
}
