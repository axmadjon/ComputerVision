package uz.greenwhite.facetrack.bean;


import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.collection.MyMapper;
import uz.greenwhite.lib.uzum.UzumAdapter;
import uz.greenwhite.lib.uzum.UzumReader;
import uz.greenwhite.lib.uzum.UzumWriter;

public class UserFace {

    public final String name;
    public final MyArray<MyArray<String>> faceEncodes;

    public UserFace(String name, MyArray<MyArray<String>> faceEncodes) {
        this.name = name;
        this.faceEncodes = faceEncodes;
    }

    public static final UserFace EMPTY = new UserFace("", MyArray.<MyArray<String>>emptyArray());

    public static final MyMapper<UserFace, String> KEY_ADAPTER = new MyMapper<UserFace, String>() {
        @Override
        public String apply(UserFace userFace) {
            return userFace.name;
        }
    };

    public static final UzumAdapter<UserFace> UZUM_ADAPTER = new UzumAdapter<UserFace>() {
        @Override
        public UserFace read(UzumReader in) {
            return new UserFace(in.readString(), in.readArray(STRING_ARRAY));
        }

        @Override
        public void write(UzumWriter out, UserFace val) {
            out.write(val.name);
            out.write(val.faceEncodes, STRING_ARRAY);
        }
    };
}
