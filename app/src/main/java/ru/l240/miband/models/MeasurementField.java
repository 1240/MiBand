package ru.l240.miband.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * @author Alexander Popov on 22.05.15.
 */
public class MeasurementField extends RealmObject {

    @SerializedName("name")
    private String name;
    @SerializedName("minValue")
    private Integer min;
    @SerializedName("maxValue")
    private Integer max;
    @SerializedName("initialValue")
    private Double standartValue;
    @SerializedName("lastValue")
    private Double value;
    @SerializedName("measurementId")
    private Long measurementId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Double getStandartValue() {
        return standartValue;
    }

    public void setStandartValue(Double standartValue) {
        this.standartValue = standartValue;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Long measurementId) {
        this.measurementId = measurementId;
    }

}
