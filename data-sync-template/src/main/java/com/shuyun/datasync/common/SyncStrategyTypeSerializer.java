package com.shuyun.datasync.common;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by jiawei.guo on 2018/8/24.
 */

public class SyncStrategyTypeSerializer implements JsonSerializer<SyncStrategyType>,JsonDeserializer<SyncStrategyType> {
    // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
    @Override
    public JsonElement serialize(SyncStrategyType strategyType, Type arg1,
                                 JsonSerializationContext arg2) {

        return new JsonPrimitive(strategyType.name());
    }

    @Override
    public SyncStrategyType deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context) throws JsonParseException {

        return SyncStrategyType.valueOf(json.getAsString());
    }
}
