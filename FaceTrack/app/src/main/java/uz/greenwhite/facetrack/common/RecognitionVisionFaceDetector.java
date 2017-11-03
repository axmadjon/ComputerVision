package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.util.HashSet;
import java.util.Set;

import uz.greenwhite.lib.job.JobApi;
import uz.greenwhite.lib.job.Promise;
import uz.greenwhite.lib.job.ShortJob;
import uz.greenwhite.lib.job.internal.Manager;
import uz.greenwhite.lib.util.Util;

public class RecognitionVisionFaceDetector extends VisionFaceDetector {

    private volatile SparseArray<String> faceRecognet = new SparseArray<>();
    private volatile SparseArray<Long> faceRecognitionTime = new SparseArray<>();

    private final int supportThread;

    public RecognitionVisionFaceDetector(ICameraMetadata mCameraMetadata,
                                         Detector<Face> faceDetector,
                                         FaceOverlayView overlayView) {
        super(mCameraMetadata, faceDetector, overlayView);
        faceRecognet.clear();
        faceRecognitionTime.clear();
        this.supportThread = Runtime.getRuntime().availableProcessors();
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

            MyFace myFace = new MyFace(face, bound, Util.nvl(faceRecognet.get(face.getId())));
            mDetFaces.put(i, myFace);
            startRecognition(bitmap, myFace);
        }

        return mDetFaces;
    }

    private static final Object mLock = new Object();
    private volatile Set<Integer> processFaceIds = new HashSet<>();

    private void startRecognition(final Bitmap bitmap, final MyFace myFace) {
        String recognitionUserName = Util.nvl(faceRecognet.get(myFace.face.getId()));
        Long faceRecognitionLastTime = Util.nvl(faceRecognitionTime.get(myFace.face.getId()), 0L);

        long currentTimeMillis = System.currentTimeMillis();

        if (("-1".equals(recognitionUserName) &&
                (currentTimeMillis - faceRecognitionLastTime) <= 5000) ||

                (!TextUtils.isEmpty(recognitionUserName) &&
                        (currentTimeMillis - faceRecognitionLastTime) <= 10000)) {
            return;
        }

        if (this.dLibDetector != null &&
                processFaceIds.size() < supportThread &&
                !processFaceIds.contains(myFace.face.getId())) {

            synchronized (mLock) {
                if (processFaceIds.size() >= supportThread ||
                        processFaceIds.contains(myFace.face.getId())) return;

                processFaceIds.add(myFace.face.getId());

                Manager.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Face face = myFace.face;
                        final int faceId = face.getId();

                        JobApi.execute(new ShortJob<String>() {
                            @Override
                            public String execute() throws Exception {
                                return dLibDetector.recognitionContains(
                                        bitmap,
                                        myFace.mBound.left,
                                        myFace.mBound.top,
                                        myFace.mBound.right,
                                        myFace.mBound.bottom);
                            }
                        }).done(new Promise.OnDone<String>() {
                            @Override
                            public void onDone(String result) {
                                faceRecognet.put(faceId, result);
                                faceRecognitionTime.put(faceId, System.currentTimeMillis());
                            }
                        }).always(new Promise.OnAlways<String>() {
                            @Override
                            public void onAlways(boolean b, String faceStringPair, Throwable throwable) {
                                processFaceIds.remove(faceId);
                                faceRecognitionTime.remove(faceId);
                            }
                        });
                    }
                });

            }
        }
    }
}
