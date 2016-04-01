package com.devoxx.model;

/**
 * Created by eloudsa on 01/11/15.
 */
public class ScheduleModel {

    private String dayName;
    private Long dayMillis;

    public ScheduleModel() {
    }

    public ScheduleModel(String dayName, Long dayMillis) {
        this.dayName = dayName;
        this.dayMillis = dayMillis;
    }

    public Long getDayMillis() {
        return dayMillis;
    }

    public void setDayMillis(Long dayMillis) {
        this.dayMillis = dayMillis;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }
}
