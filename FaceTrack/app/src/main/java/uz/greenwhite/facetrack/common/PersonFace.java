package uz.greenwhite.facetrack.common;

import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.uzum.UzumAdapter;
import uz.greenwhite.lib.uzum.UzumReader;
import uz.greenwhite.lib.uzum.UzumWriter;

public class PersonFace {

    public final String nr;
    public final String nc;
    public final MyArray<Encode> encodes;

    public PersonFace(String nr, String nc, MyArray<Encode> encodes) {
        this.nr = nr;
        this.nc = nc;
        this.encodes = encodes;
    }

    public String[][] getFaceEncodeToString() {
        String[][] result = new String[encodes.size() + 1][3];
        result[0] = new String[]{nr, nc};

        for (int i = 0; i < encodes.size(); i++) {
            Encode val = encodes.get(i);
            result[i + 1] = new String[]{val.r, val.c, val.value};
        }

        return result;
    }

    public static final UzumAdapter<PersonFace> UZUM_ADAPTER = new UzumAdapter<PersonFace>() {

        @Override
        public PersonFace read(UzumReader in) {
            return new PersonFace(in.readString(), in.readString(), in.readArray(Encode.UZUM_ADAPTER));
        }

        @Override
        public void write(UzumWriter out, PersonFace val) {
            out.write(val.nr);
            out.write(val.nc);
            out.write(val.encodes, Encode.UZUM_ADAPTER);
        }
    };

    static class Encode {

        public final String r;
        public final String c;
        public final String value;

        public Encode(String r, String c, String value) {
            this.r = r;
            this.c = c;
            this.value = value;
        }

        public static final UzumAdapter<Encode> UZUM_ADAPTER = new UzumAdapter<Encode>() {
            @Override
            public Encode read(UzumReader in) {
                return new Encode(in.readString(), in.readString(), in.readString());
            }

            @Override
            public void write(UzumWriter out, Encode val) {
                out.write(val.r);
                out.write(val.c);
                out.write(val.value);
            }
        };
    }

}
