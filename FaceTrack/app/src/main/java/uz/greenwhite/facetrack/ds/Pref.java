package uz.greenwhite.facetrack.ds;

import android.content.Context;
import android.content.SharedPreferences;

import uz.greenwhite.lib.uzum.Uzum;
import uz.greenwhite.lib.uzum.UzumAdapter;

public class Pref {

    private static final String PREF_NAME = "uz.greenwhite.face_time.FACE_ENCODE_v2";

    private final SharedPreferences sp;

    public Pref(Context context) {
        this.sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String load(String key) {
        return sp.getString(key, null);
    }

    public <E> E load(String key, UzumAdapter<E> adapter) {
        String val = load(key);
        if (val == null) {
            return null;
        }
        return Uzum.toValue(val, adapter);
    }

    public void save(String key, String value) {
        SharedPreferences.Editor edit = sp.edit();
        if (value != null) {
            edit.putString(key, value);
        } else {
            edit.remove(key);
        }
        edit.commit();
    }

    public <E> void save(String key, E val, UzumAdapter<E> adapter) {
        String txt = null;
        if (val != null) {
            txt = Uzum.toJson(val, adapter);
        }
        save(key, txt);
    }
}
