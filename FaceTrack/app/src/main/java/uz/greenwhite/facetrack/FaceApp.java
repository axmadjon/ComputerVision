package uz.greenwhite.facetrack;


import uz.greenwhite.lib.GWSLOG;
import uz.greenwhite.lib.mold.MoldApi;
import uz.greenwhite.lib.mold.MoldApplication;

public class FaceApp extends MoldApplication {

    public static final String PREF_USERS = "users_v2";
    public static final String DLIB_LANDMARK_PATH = "storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat";
    public static final String DLIB_RECOGNITION_PATH = "storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat";


    @Override
    public void onCreate() {
        super.onCreate();

        GWSLOG.DEBUG = BuildConfig.DEBUG;
        MoldApi.APPLICATION_VERSION_NAME = BuildConfig.VERSION_NAME;
        MoldApi.APPLICATION_VERSION_CODE = BuildConfig.VERSION_CODE;
    }
}
