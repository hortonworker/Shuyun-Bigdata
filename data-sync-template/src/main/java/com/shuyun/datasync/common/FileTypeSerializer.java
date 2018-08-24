package com.shuyun.datasync.common;

import com.google.gson.*;
import com.shuyun.datasync.common.FileType;

import java.lang.reflect.Type;

/**
 * Created by jiawei.guo on 2018/8/24.
 */

public class FileTypeSerializer implements JsonSerializer<FileType>,JsonDeserializer<FileType> {
    // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
    @Override
    public JsonElement serialize(FileType fileType, Type arg1,
                                 JsonSerializationContext arg2) {

        return new JsonPrimitive(fileType.name());
    }

    @Override
    public FileType deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context) throws JsonParseException {

        return FileType.valueOf(json.getAsString());
    }
}
