package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import uz.greenwhite.lib.job.internal.Manager;

public class TrainVisionFaceDetector extends VisionFaceDetector {

    private OnFaceListener onFaceListener;
    private boolean clickUserFace = false;

    public TrainVisionFaceDetector(ICameraMetadata mCameraMetadata,
                                   Detector<Face> faceDetector,
                                   FaceOverlayView overlayView) {
        super(mCameraMetadata, faceDetector, overlayView);
    }

    public void setOnFaceListener(OnFaceListener onFaceListener) {
        this.onFaceListener = onFaceListener;
    }

    public void setClickUserFace(boolean clickUserFace) {
        this.clickUserFace = clickUserFace;
    }

    @Override
    public SparseArray<MyFace> detect(Frame frame) {
        mDetFaces.clear();

        final SparseArray<Face> faces = faceDetector.detect(frame);

        if (faces.size() == 0) return mDetFaces;

        final int ow = getUprightPreviewWidth(frame);
        final Matrix transform = getCameraToViewTransform(frame);

        if (onFaceListener != null && faces.size() > 1) {
            onFaceListener.error("Detect more face in camera!");
        }

        // Translate the face bounds into something that DLib detector knows.
        final Face face = faces.get(faces.keyAt(0));

        final Rect bound = getFaceBound(face, ow);

        MyFace myFace = new MyFace(face, bound);
        mDetFaces.put(0, myFace);

        if (clickUserFace && onFaceListener != null) {
            clickUserFace = false;
            final Bitmap bitmap = getBitmapFromFrame(frame, transform);
            Manager.handler.post(new Runnable() {
                @Override
                public void run() {
                    onFaceListener.detect(face, bitmap, bound);
                }
            });

        }

        if (clickUserFace) {
            clickUserFace = false;
        }

        return mDetFaces;
    }

}
