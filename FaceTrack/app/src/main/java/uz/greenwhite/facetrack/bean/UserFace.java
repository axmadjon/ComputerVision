package uz.greenwhite.facetrack.bean;


import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.collection.MyMapper;
import uz.greenwhite.lib.uzum.UzumAdapter;
import uz.greenwhite.lib.uzum.UzumReader;
import uz.greenwhite.lib.uzum.UzumWriter;

public class UserFace {

    public final String name;
    public final MyArray<String> faceEncodes;

    public UserFace(String name, MyArray<String> faceEncodes) {
        this.name = name;
        this.faceEncodes = faceEncodes;
    }

    public String[][] getFaceEncodeToLong() {
        String[][] result = new String[faceEncodes.size()][3];
        for (int i = 0; i < faceEncodes.size(); i++) {
            String val = faceEncodes.get(i);
            String[] split = val.split(",");
            result[i] = new String[]{split[0], split[1], split.length == 3 ? split[2] : ""};

        }
        return result;
    }

    public static final UserFace EMPTY = new UserFace("", MyArray.<String>emptyArray());

    public static final MyMapper<UserFace, String> KEY_ADAPTER = new MyMapper<UserFace, String>() {
        @Override
        public String apply(UserFace userFace) {
            return userFace.name;
        }
    };

    public static final UzumAdapter<UserFace> UZUM_ADAPTER = new UzumAdapter<UserFace>() {
        @Override
        public UserFace read(UzumReader in) {
            return new UserFace(in.readString(), in.readValue(STRING_ARRAY));
        }

        @Override
        public void write(UzumWriter out, UserFace val) {
            out.write(val.name);
            out.write(val.faceEncodes, STRING_ARRAY);
        }
    };
}
