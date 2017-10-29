package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Pair;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.util.HashMap;

import uz.greenwhite.lib.job.JobApi;
import uz.greenwhite.lib.job.Promise;
import uz.greenwhite.lib.job.ShortJob;
import uz.greenwhite.lib.job.internal.Manager;
import uz.greenwhite.lib.util.Util;

public class RecognitionVisionFaceDetector extends VisionFaceDetector {

    public volatile HashMap<String, String> userFaces = new HashMap<>();

    public RecognitionVisionFaceDetector(ICameraMetadata mCameraMetadata,
                                         Detector<Face> faceDetector,
                                         FaceOverlayView overlayView) {
        super(mCameraMetadata, faceDetector, overlayView);
        userFaces.clear();
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

            MyFace myFace = new MyFace(face, bound, Util.nvl(userFaces.get("" + face.getId())));
            mDetFaces.put(i, myFace);
            startRecognition(bitmap, myFace);
        }

        return mDetFaces;
    }

    private static final Object mLock = new Object();
    private static volatile boolean mRecognitionLock = false;

    private void startRecognition(final Bitmap bitmap, final MyFace myFace) {
        if (userFaces.containsKey("" + myFace.face.getId())) return;

        if (this.dLibDetector != null) {
            if (mRecognitionLock) return;

            synchronized (mLock) {
                if (mRecognitionLock) return;

                mRecognitionLock = true;
                System.out.println("start:recognitionFaceInBitmapRect");
                Manager.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        JobApi.execute(new ShortJob<Pair<Face, String>>() {
                            @Override
                            public Pair<Face, String> execute() throws Exception {
                                String result = dLibDetector.recognitionContains(bitmap,
                                        myFace.mBound.left, myFace.mBound.top, myFace.mBound.right, myFace.mBound.bottom);

                                System.out.println("Result: " + result);

                                mRecognitionLock = false;
                                return new Pair<>(myFace.face, result);
                            }
                        }).done(new Promise.OnDone<Pair<Face, String>>() {
                            @Override
                            public void onDone(Pair<Face, String> result) {
                                userFaces.put("" + result.first.getId(), result.second);
                            }
                        });
                    }
                });
            }
        }
    }
}
