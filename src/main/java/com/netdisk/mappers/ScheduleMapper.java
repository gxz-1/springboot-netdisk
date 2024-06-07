package com.netdisk.mappers;

public interface ScheduleMapper {

    void updateByRecoveryTime();

    void deleteEmailCode();

    void deleteFile();

    void deleteShare();

    void deleteErrorFile();
}
