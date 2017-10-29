package uz.greenwhite.facetrack;


import uz.greenwhite.lib.GWSLOG;
import uz.greenwhite.lib.mold.MoldApi;
import uz.greenwhite.lib.mold.MoldApplication;

public class FaceApp extends MoldApplication {

    public static final String PREF_USERS = "users";

    @Override
    public void onCreate() {
        super.onCreate();

        GWSLOG.DEBUG = BuildConfig.DEBUG;
        MoldApi.APPLICATION_VERSION_NAME = BuildConfig.VERSION_NAME;
        MoldApi.APPLICATION_VERSION_CODE = BuildConfig.VERSION_CODE;
    }
}
