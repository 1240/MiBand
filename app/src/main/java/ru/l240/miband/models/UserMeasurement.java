package ru.l240.miband.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by user on 03.03.2016.
 */
public class UserMeasurement extends RealmObject {

    private String strValue;
    private long measurementId;
    private Date measurementDate;


    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    public Date getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(Date measurementDate) {
        this.measurementDate = measurementDate;
    }

}
