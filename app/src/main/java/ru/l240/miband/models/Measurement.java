package ru.l240.miband.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * @author Alexander Popov on 22.05.15.
 */
public class Measurement extends RealmObject {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SerializedName("name")
    private String name;
    @SerializedName("sysName")
    private String sysName;
    @SerializedName("units")
    private String unit;
    @SerializedName("displayOrder")
    private Integer sortBy;
    @SerializedName("decimals")
    private Integer decimals;
    @SerializedName("stepping")
    private Double stepping;
    @SerializedName("fieldsDelimiter")
    private String delimiter;
    @SerializedName("fields")
    private RealmList<MeasurementField> fields;

    public RealmList<MeasurementField> getFields() {
        return fields;
    }

    public void setFields(RealmList<MeasurementField> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public Integer getSortBy() {
        return sortBy;
    }

    public void setSortBy(Integer sortBy) {
        this.sortBy = sortBy;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public Double getStepping() {
        return stepping;
    }

    public void setStepping(Double stepping) {
        this.stepping = stepping;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Float getMaxValue(String name) {
        Float maxValue = 300f;

        exitPoint1:
        for (MeasurementField mf : fields) {
            String entryName = mf.getName();

            if (name.equals(entryName)) {
                maxValue = Float.valueOf(mf.getMax());
                break exitPoint1;
            }
        }
        return maxValue;
    }

    public Float getMinValue(String name) {
        Float minValue = 0f;

        exitPoint1:
        for (MeasurementField mf : fields) {
            String entryName = mf.getName();

            if (name.equals(entryName)) {
                minValue = Float.valueOf(mf.getMin());
                break exitPoint1;
            }
        }
        return minValue;
    }

}

