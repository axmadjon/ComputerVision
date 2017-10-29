package uz.greenwhite.facetrack.arg;

import uz.greenwhite.facetrack.bean.UserFace;
import uz.greenwhite.lib.uzum.UzumAdapter;
import uz.greenwhite.lib.uzum.UzumReader;
import uz.greenwhite.lib.uzum.UzumWriter;

public class ArgRecognition {

    public static final int NEW_USER = 1;
    public static final int RECOGNITION = 2;

    public final UserFace userFace;
    public final int recognitionType;

    public ArgRecognition(UserFace userFace, int recognitionType) {
        this.userFace = userFace;
        this.recognitionType = recognitionType;
    }

    public ArgRecognition(int recognitionType) {
        this.userFace = UserFace.EMPTY;
        this.recognitionType = recognitionType;
    }

    public ArgRecognition(UzumReader in) {
        this.userFace = in.readValue(UserFace.UZUM_ADAPTER);
        this.recognitionType = in.readInt();
    }

    public void write(UzumWriter out) {
        out.write(this.userFace, UserFace.UZUM_ADAPTER);
        out.write(this.recognitionType);
    }

    public static final UzumAdapter<ArgRecognition> UZUM_ADAPTER = new UzumAdapter<ArgRecognition>() {
        @Override
        public ArgRecognition read(UzumReader uzumReader) {
            return new ArgRecognition(uzumReader);
        }

        @Override
        public void write(UzumWriter uzumWriter, ArgRecognition argRecognition) {
            argRecognition.write(uzumWriter);
        }
    };
}
