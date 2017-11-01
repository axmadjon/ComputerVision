package uz.greenwhite.facetrack.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import uz.greenwhite.facetrack.R;

public class FaceOverlayView extends AppCompatImageView {

    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private final Object mMutex = new Object();

    // Stroke & paint.
    private static final float WIDTH = 4.f;
    private final int mStrokeWidth;
    private final Paint mStrokePaint;
    private final Paint mIdPaint;
    private final Matrix mRenderMatrix = new Matrix();

    // State
    private float mScaleFromPreviewToView = 1f;
    private final List<MyFace> mFaces = new CopyOnWriteArrayList<>();

    public FaceOverlayView(Context context) {
        this(context, null);
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = getContext()
                .getResources().getDisplayMetrics().density;

        mStrokeWidth = (int) (density * WIDTH);

        mStrokePaint = new Paint();
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);


        mIdPaint = new Paint();
        mIdPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mIdPaint.setTextSize(ID_TEXT_SIZE);
    }

    public void setCameraPreviewSize(final int width, final int height) {
        mScaleFromPreviewToView = Math.min((float) getWidth() / width,
                (float) getHeight() / height);
    }

    public void setFaces(List<MyFace> faces) {
        synchronized (mMutex) {
            mFaces.clear();
            for (MyFace face : faces) {
                mFaces.add(face);
            }
        }
        postInvalidate();
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        mRenderMatrix.reset();
        mRenderMatrix.setScale(mScaleFromPreviewToView, mScaleFromPreviewToView);

        canvas.save();
        canvas.concat(mRenderMatrix);

        mStrokePaint.setStrokeWidth(mStrokeWidth / mScaleFromPreviewToView);

        for (MyFace face : mFaces) {
            canvas.drawRect(face.mBound, mStrokePaint);

            String userName = TextUtils.isEmpty(face.userName) ? "recognition" :
                    ("-1".equals(face.userName) ? "NotFound" : face.userName);
            canvas.drawText("user: " + userName,
                    face.mBound.left, face.mBound.bottom + 20, mIdPaint);

            canvas.drawText("id: " + face.face.getId(),
                    face.mBound.left, face.mBound.bottom + 40, mIdPaint);
            canvas.drawText("happiness: " + String.format("%.2f", face.face.getIsSmilingProbability()),
                    face.mBound.left, face.mBound.bottom + 60, mIdPaint);
            canvas.drawText("right eye: " + String.format("%.2f", face.face.getIsRightEyeOpenProbability()),
                    face.mBound.left, face.mBound.bottom + 80, mIdPaint);
            canvas.drawText("left eye: " + String.format("%.2f", face.face.getIsLeftEyeOpenProbability()),
                    face.mBound.left, face.mBound.bottom + 100, mIdPaint);

//            face.println();
        }
        canvas.restore();

        super.onDrawForeground(canvas);
    }


    //##############################################################################################

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view
     * scale.
     */
    public float scaleX(float horizontal) {
        return horizontal * 1.0F;
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public float scaleY(float vertical) {
        return vertical * 1.0F;
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateX(float x) {
        return this.getWidth() - scaleX(x);
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateY(float y) {
        return scaleY(y);
    }

}