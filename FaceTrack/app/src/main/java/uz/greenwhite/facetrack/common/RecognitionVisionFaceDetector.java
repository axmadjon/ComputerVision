package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import uz.greenwhite.lib.job.JobApi;
import uz.greenwhite.lib.job.ShortJob;

public class RecognitionVisionFaceDetector extends VisionFaceDetector {

    public RecognitionVisionFaceDetector(ICameraMetadata mCameraMetadata,
                                         Detector<Face> faceDetector,
                                         FaceOverlayView overlayView) {
        super(mCameraMetadata, faceDetector, overlayView);
    }


    @Override
    public SparseArray<MyFace> detect(Frame frame) {
        mDetFaces.clear();

        final SparseArray<Face> faces = faceDetector.detect(frame);

        if (faces.size() == 0) return mDetFaces;

        final int ow = getUprightPreviewWidth(frame);
        final Matrix transform = getCameraToViewTransform(frame);
        final Bitmap bitmap = getBitmapFromFrame(frame, transform);

        // Translate the face bounds into something that DLib detector knows.
        for (int i = 0; i < faces.size(); ++i) {
            final Face face = faces.get(faces.keyAt(i));

            Rect bound = getFaceBound(face, ow);

            MyFace myFace = new MyFace(face, bound);
            mDetFaces.put(i, myFace);
            startRecognition(bitmap, myFace);
        }

        return mDetFaces;
    }

    private static final Object mLock = new Object();
    private static volatile boolean mRecognitionLock = false;

    private void startRecognition(final Bitmap bitmap, final MyFace myFace) {
        if (this.dLibDetector != null) {
            if (mRecognitionLock) return;

            synchronized (mLock) {
                if (mRecognitionLock) return;

                mRecognitionLock = true;
                System.out.println("start:recognitionFaceInBitmapRect");
                JobApi.execute(new ShortJob<Void>() {
                    @Override
                    public Void execute() throws Exception {
                        String result = dLibDetector.recognitionContains(bitmap,
                                myFace.mBound.left, myFace.mBound.top, myFace.mBound.right, myFace.mBound.bottom);
                        System.out.println("Result: " + result);

                        mRecognitionLock = false;
                        return null;
                    }
                });
            }
        }
    }
}
