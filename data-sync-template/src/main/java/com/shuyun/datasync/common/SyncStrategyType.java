package com.shuyun.datasync.common;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public enum SyncStrategyType {
    SERIAL_COVER_ALWAYS,
    PARALLEL_COVER_ALWAYS,
    SERIAL_COVER_OR_UPDATE_BY_COUNT,
    PARALLEL_COVER_OR_UPDATE_BY_COUNT;
}
