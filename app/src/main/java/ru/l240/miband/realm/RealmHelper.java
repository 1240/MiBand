package ru.l240.miband.realm;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import ru.l240.miband.models.Measurement;
import ru.l240.miband.models.MeasurementField;
import ru.l240.miband.models.Profile;
import ru.l240.miband.models.UserMeasurement;

/**
 * @author Alexander Popov on 03.03.2016.
 */
public class RealmHelper {

    public static <T extends RealmObject> List<T> getAll(Realm realm, Class<T> clazz) {
        return realm.allObjects(clazz);
    }

    public static <T extends RealmObject> void save(Realm realm, List<T> data) {
        realm.beginTransaction();
        realm.copyToRealm(data);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> void save(Realm realm, T data) {
        realm.beginTransaction();
        realm.copyToRealm(data);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> void clear(Realm realm, Class<T> clazz) {
        realm.beginTransaction();
        realm.clear(clazz);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> void clearAll(Realm realm) {
        realm.beginTransaction();
        realm.clear(Measurement.class);
        realm.clear(MeasurementField.class);
        realm.clear(UserMeasurement.class);
        realm.clear(Profile.class);
        realm.commitTransaction();
    }


}
