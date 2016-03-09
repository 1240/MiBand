package ru.l240.miband.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by user on 03.03.2016.
 */
public class UserMeasurement extends RealmObject {

    private int id;
    private String measurementComment;
    private String measurementName;
    private String measurementUnits;
    private String strValue;
    private long measurementId;
    private Date measurementDate;
    private long measurementFieldId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMeasurementComment() {
        return measurementComment;
    }

    public void setMeasurementComment(String measurementComment) {
        this.measurementComment = measurementComment;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    public String getMeasurementUnits() {
        return measurementUnits;
    }

    public void setMeasurementUnits(String measurementUnits) {
        this.measurementUnits = measurementUnits;
    }

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

    public long getMeasurementFieldId() {
        return measurementFieldId;
    }

    public void setMeasurementFieldId(long measurementFieldId) {
        this.measurementFieldId = measurementFieldId;
    }
}
