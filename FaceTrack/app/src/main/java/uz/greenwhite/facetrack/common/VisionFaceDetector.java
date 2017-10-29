package uz.greenwhite.facetrack.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.my.jni.dlib.DLibLandmarks68Detector;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class VisionFaceDetector extends Detector<MyFace> {

    // State.
    protected final SparseArray<MyFace> mDetFaces = new SparseArray<>();

    protected final ICameraMetadata mCameraMetadata;
    protected final Detector<Face> faceDetector;
    protected DLibLandmarks68Detector dLibDetector;

    public VisionFaceDetector(ICameraMetadata mCameraMetadata,
                              Detector<Face> faceDetector,
                              FaceOverlayView overlayView) {
        this.mCameraMetadata = mCameraMetadata;
        this.faceDetector = faceDetector;

        setProcessor(new PostProcessor(overlayView));
    }

    public void setDLibDetector(DLibLandmarks68Detector dLibDetector) {
        if (this.dLibDetector == null) {
            this.dLibDetector = dLibDetector;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////


    protected final Rect getFaceBound(Face face, int uprightPreviewWidth) {
        final float x;
        if (mCameraMetadata.isFacingFront()) {
            // The facing-front preview is horizontally mirrored and it's
            // no harm for the algorithm to find the face bound, but it's
            // critical for the algorithm to align the landmarks. I need
            // to mirror it again.
            //
            // For example:
            //
            // <-------------+ (1) The mirrored coordinate.
            // +-------------> (2) The not-mirrored coordinate.
            // |       |-----| This is x in the (1) system.
            // |   |---|       This is w in both (1) and (2) systems.
            // |   ?           This is what I want in the (2) system.
            // |   .---.
            // |   | F |
            // |   '---'
            // |
            // v
            x = uprightPreviewWidth - face.getPosition().x - face.getWidth();
        } else {
            x = face.getPosition().x;
        }
        final float y = face.getPosition().y;
        final float w = face.getWidth();
        final float h = face.getHeight();
        final Rect bound = new Rect((int) (x), (int) (y), (int) (x + w), (int) (y + h));

        // The face bound that DLib landmarks algorithm needs is slightly
        // different from the one given by Google Vision API, so I change
        // it a bit from the experience of try-and-error.
        bound.inset(bound.width() / 10, bound.height() / 6);
        bound.offset(0, bound.height() / 4);
        return bound;
    }


    // TODO: This method could be an util method.
    // TODO: Rotation and facing are important parameters.
    protected final Matrix getCameraToViewTransform(final Frame frame) {
        final Matrix transform = new Matrix();
        switch (frame.getMetadata().getRotation()) {
            case Frame.ROTATION_90:
                transform.postRotate(90);
                break;
            case Frame.ROTATION_180:
                transform.postRotate(180);
                break;
            case Frame.ROTATION_270:
                transform.postRotate(270);
                break;
        }

        if (mCameraMetadata.isFacingFront()) {
            transform.postScale(-1, 1);
        }

        return transform;
    }


    protected final Bitmap getBitmapFromFrame(final Frame frame, final Matrix transform) {
        if (frame.getBitmap() != null) {
            return frame.getBitmap();
        } else {
            final int width = frame.getMetadata().getWidth();
            final int height = frame.getMetadata().getHeight();
            final YuvImage yuvImage = new YuvImage(
                    frame.getGrayscaleImageData().array(),
                    ImageFormat.NV21, width, height, null);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, outputStream);

            final byte[] jpegArray = outputStream.toByteArray();
            final Bitmap rawBitmap = BitmapFactory.decodeByteArray(
                    jpegArray, 0, jpegArray.length);

            final int bw = rawBitmap.getWidth();
            final int bh = rawBitmap.getHeight();

            return Bitmap.createBitmap(rawBitmap,
                    0, 0, bw, bh,
                    transform, false);
        }
    }

    protected final int getUprightPreviewWidth(Frame frame) {
        if (mCameraMetadata.isPortraitMode()) {
            return frame.getMetadata().getHeight();
        } else {
            return frame.getMetadata().getWidth();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class PostProcessor implements Detector.Processor<MyFace> {

        final FaceOverlayView mOverlay;

        // Data.
        final List<MyFace> mFaces = new ArrayList<>();

        PostProcessor(FaceOverlayView overlay) {
            mOverlay = overlay;
        }

        @Override
        public void release() {
            // DO NOTHING.
        }

        @Override
        public void receiveDetections(Detections<MyFace> detections) {
            mFaces.clear();
            if (detections == null) {
                mOverlay.setFaces(mFaces);
                return;
            }

            final SparseArray<MyFace> faces = detections.getDetectedItems();

            if (faces == null) return;

            for (int i = 0; i < faces.size(); ++i) {
                mFaces.add(faces.get(faces.keyAt(i)));
            }
            mOverlay.setFaces(mFaces);
        }
    }
}
