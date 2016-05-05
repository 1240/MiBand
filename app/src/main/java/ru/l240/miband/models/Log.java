package ru.l240.miband.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by l24o on 04.05.16.
 */
public class Log extends RealmObject {

    private Date date;
    private String text;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
