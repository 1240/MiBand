package ru.l240.miband.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.fors.remsmed.core.dto.timezones.TimeZoneItem;

public class SettingsDataContainer {
    //static private List<LanguageItem> languageList = new ArrayList<>();
    static private List<TimeZoneItem> timeZoneList = new ArrayList<>();

    //public static List<LanguageItem> getLanguageList() {
    //    return languageList;
    //}

    public static List<TimeZoneItem> getTimeZoneList(String response) throws JSONException {
        JSONObject mainObject = new JSONObject(response);
        JSONArray jsonLanguage = mainObject.getJSONArray("classifiers");

        List<TimeZoneItem> timeZoneList = new ArrayList<>();

        for (int i = 0; i < jsonLanguage.length(); ++i) {
            JSONObject object = (JSONObject) jsonLanguage.get(i);
            Integer cls = (Integer) object.get("cls_id");
            if (cls == -3) {
                JSONArray jsonTimeZoneItems = object.getJSONArray("items");
                for (int j = 0; j < jsonTimeZoneItems.length(); ++j) {
                    JSONObject langObject = (JSONObject) jsonTimeZoneItems.get(j);
                    timeZoneList.add(new TimeZoneItem(langObject.getString("item_id"),
                            langObject.getString("item_name")));
                }
                SettingsDataContainer.setTimeZoneList(timeZoneList);
            }
        }
        return timeZoneList;
    }

//    public static void setLanguageList(List<LanguageItem> languageList) {
//        SettingsDataContainer.languageList = languageList;
//    }

    public static void setTimeZoneList(List<TimeZoneItem> timeZoneList) {
        SettingsDataContainer.timeZoneList = timeZoneList;
    }

    public static void setLanguageAndTimeZone(String response) throws JSONException {
        JSONObject mainObject = new JSONObject(response);
        JSONArray jsonLanguage = mainObject.getJSONArray("classifiers");

        //List<SettingsDataContainer.LanguageItem> languageList = new ArrayList<>();
        List<TimeZoneItem> timeZoneList = new ArrayList<>();

        for (int i = 0; i < jsonLanguage.length(); ++i) {
            JSONObject object = (JSONObject) jsonLanguage.get(i);
            Integer cls = (Integer) object.get("cls_id");
//            if(cls == -2) {
//                JSONArray jsonLanguageItems = object.getJSONArray("items");
//                for(int j = 0; j < jsonLanguageItems.length(); ++j) {
//                    JSONObject langObject = (JSONObject) jsonLanguageItems.get(j);
//                    languageList.add(new SettingsDataContainer.LanguageItem(langObject.getString("item_id"),
//                            langObject.getString("item_name")));
//                }
//                SettingsDataContainer.setLanguageList(languageList);
//            }
            if (cls == -3) {
                JSONArray jsonTimeZoneItems = object.getJSONArray("items");
                for (int j = 0; j < jsonTimeZoneItems.length(); ++j) {
                    JSONObject langObject = (JSONObject) jsonTimeZoneItems.get(j);
                    timeZoneList.add(new TimeZoneItem(langObject.getString("item_id"),
                            langObject.getString("item_name")));
                }
                SettingsDataContainer.setTimeZoneList(timeZoneList);
            }
        }
    }

//    public static class LanguageItem {
//        String lId;
//        String lName;
//
//        public LanguageItem(String lId, String lName) {
//            this.lId = lId;
//            this.lName = lName;
//        }
//
//        public String getId() {
//            return lId;
//        }
//
//        public String getName() {
//            return lName;
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(lName);
//            stringBuilder.append(" (");
//            stringBuilder.append(lId);
//            stringBuilder.append(")");
//            return stringBuilder.toString();
//        }
//    }

}
