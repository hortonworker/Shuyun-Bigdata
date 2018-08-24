package com.shuyun.datasync.common;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by jiawei.guo on 2018/8/24.
 */

public class TableSelectTypeSerializer implements JsonSerializer<TableSelectType>,JsonDeserializer<TableSelectType> {
    // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
    @Override
    public JsonElement serialize(TableSelectType tableSelectType, Type arg1,
                                 JsonSerializationContext arg2) {

        return new JsonPrimitive(tableSelectType.name());
    }

    @Override
    public TableSelectType deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context) throws JsonParseException {

        return TableSelectType.valueOf(json.getAsString());
    }
}
