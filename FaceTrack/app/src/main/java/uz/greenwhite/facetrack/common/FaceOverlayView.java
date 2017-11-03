package uz.greenwhite.facetrack.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import uz.greenwhite.facetrack.R;

public class FaceOverlayView extends AppCompatImageView {

    private final Object mMutex = new Object();

    // Stroke & paint.
    private final Paint mFaceRectPaint;
    private final Paint mUserNamePaint;
    private final Paint mUserNameBackgroundPaint;

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

        mFaceRectPaint = new Paint();
        mFaceRectPaint.setStrokeWidth(2);
        mFaceRectPaint.setStyle(Paint.Style.STROKE);
        mFaceRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mUserNameBackgroundPaint = new Paint();
        mUserNameBackgroundPaint.setStrokeWidth(4);
        mUserNameBackgroundPaint.setStyle(Paint.Style.FILL);
        mUserNameBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        mUserNamePaint = new Paint();
        mUserNamePaint.setColor(ContextCompat.getColor(context, R.color.white));
        mUserNamePaint.setTextSize(30.0f);
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


        for (MyFace face : mFaces) {

            if (!TextUtils.isEmpty(face.userName) && !"-1".equals(face.userName)) {
                mFaceRectPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_green));
                mUserNameBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_green));

            } else if ("-1".equals(face.userName)) {
                mFaceRectPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_red));
                mUserNameBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_red));

            } else {
                mFaceRectPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_wait));
                mUserNameBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_wait));

            }

            canvas.drawRect(face.mBound, mFaceRectPaint);

            String userName = TextUtils.isEmpty(face.userName) ? "recognition" :
                    ("-1".equals(face.userName) || face.userName.startsWith("N") ? "Not Found" : face.userName);

            int textWidth = (int) mUserNamePaint.measureText(userName);

            canvas.drawRect(new Rect(
                    face.mBound.left - 2,
                    face.mBound.bottom,
                    face.mBound.left + textWidth + 10,
                    face.mBound.bottom + 40
            ), mUserNameBackgroundPaint);

            canvas.drawText(userName,
                    face.mBound.left + 5,
                    face.mBound.bottom + 30,
                    mUserNamePaint);
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