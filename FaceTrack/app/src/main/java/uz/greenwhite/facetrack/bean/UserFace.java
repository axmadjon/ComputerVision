package uz.greenwhite.facetrack.bean;


import com.my.jni.dlib.DLibLandmarks68Detector;

import uz.greenwhite.facetrack.common.PersonFace;
import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.collection.MyMapper;
import uz.greenwhite.lib.uzum.UzumAdapter;
import uz.greenwhite.lib.uzum.UzumReader;
import uz.greenwhite.lib.uzum.UzumWriter;

public class UserFace {

    public final String name;
    public final MyArray<PersonFace> faces;

    public UserFace(String name, MyArray<PersonFace> faces) {
        this.name = name;
        this.faces = faces;
    }

    public void prepareFaceEncodeToString(DLibLandmarks68Detector detector) {
        for (int i = 0; i < faces.size(); i++) {
            detector.prepareUserFaces(name, faces.get(i).getFaceEncodeToString());
        }
    }

    public static final UserFace EMPTY = new UserFace("", MyArray.<PersonFace>emptyArray());

    public static final MyMapper<UserFace, String> KEY_ADAPTER = new MyMapper<UserFace, String>() {
        @Override
        public String apply(UserFace userFace) {
            return userFace.name;
        }
    };

    public static final UzumAdapter<UserFace> UZUM_ADAPTER = new UzumAdapter<UserFace>() {
        @Override
        public UserFace read(UzumReader in) {
            return new UserFace(in.readString(), in.readArray(PersonFace.UZUM_ADAPTER));
        }

        @Override
        public void write(UzumWriter out, UserFace val) {
            out.write(val.name);
            out.write(val.faces, PersonFace.UZUM_ADAPTER);
        }
    };
}
