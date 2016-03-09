package ru.l240.miband.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * @author Alexander Popov on 22.05.15.
 */
public class Measurement extends RealmObject {

    private int id;
    private String name;
    private String sysName;
    private String units;
    private int displayOrder;
    private int decimals;
    private double stepping;
    private RealmList<MeasurementField> fields;

    public RealmList<MeasurementField> getFields() {
        return fields;
    }

    public void setFields(RealmList<MeasurementField> fields) {
        this.fields = fields;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public double getStepping() {
        return stepping;
    }

    public void setStepping(double stepping) {
        this.stepping = stepping;
    }

}

