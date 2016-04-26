package ru.l240.miband.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * @author Alexander Popov on 08.05.15.
 */
public class Profile extends RealmObject {


    private int id;
    private String firstName;
    private String lastName;
    private String sex;
    private int externalUserId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(int externalUserId) {
        this.externalUserId = externalUserId;
    }
}
