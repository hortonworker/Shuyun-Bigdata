package com.shuyun.datasync.common;

import com.google.gson.*;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import java.lang.reflect.Type;

/**
 * Created by jiawei.guo on 2018/8/24.
 */

public class DataTypeSerializer implements JsonSerializer<DataType>,JsonDeserializer<DataType> {
    // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
    @Override
    public JsonElement serialize(DataType dataType, Type arg1,
                                 JsonSerializationContext arg2) {

        return new JsonPrimitive(dataType.typeName());
    }

    @Override
    public DataType deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context) throws JsonParseException {

        return DataType.fromJson("\"" + json.getAsString() + "\"");
    }
}
