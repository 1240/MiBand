package ru.l240.miband.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

/**
 * @author Alexander Popov on 08.05.15.
 */
public class JournalItem extends RealmObject {
    @SerializedName("id")
    private long id;
    @SerializedName("actualDate")
    private Date date;
    @SerializedName("text")
    private String message;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
