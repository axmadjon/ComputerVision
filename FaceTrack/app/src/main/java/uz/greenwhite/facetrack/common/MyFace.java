package uz.greenwhite.facetrack.common;

import android.graphics.Rect;

import com.google.android.gms.vision.face.Face;

public class MyFace {

    public final Face face;
    public final Rect mBound;

    public MyFace(Face face, Rect bound) {
        this.face = face;
        this.mBound = bound;
    }

    protected void println() {
        System.out.print("id: " + face.getId());
        System.out.print(String.format(", happiness:  %.2f", face.getIsSmilingProbability()));
        System.out.print(String.format(", right eye:  %.2f", face.getIsRightEyeOpenProbability()));
        System.out.println(String.format(", left eye: %.2f", face.getIsLeftEyeOpenProbability()));
    }
}
