package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.android.gms.vision.face.Face;

public interface OnFaceListener {

    void detect(Face face, Bitmap bitmap, Rect bound);

    void error(String error);

}