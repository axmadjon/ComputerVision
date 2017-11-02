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
    private final Paint mStrokePaintWait;
    private final Paint mStrokePaintFound;
    private final Paint mStrokePaintNotFound;
    private final Paint mIdPaintWait;
    private final Paint mIdPaintFound;
    private final Paint mIdPaintNotFound;
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

        mStrokePaintWait = new Paint();
        mStrokePaintWait.setStrokeWidth(mStrokeWidth);
        mStrokePaintWait.setColor(ContextCompat.getColor(context, R.color.color_wait));
        mStrokePaintWait.setStyle(Paint.Style.STROKE);
        mStrokePaintWait.setStrokeCap(Paint.Cap.ROUND);

        mStrokePaintFound = new Paint();
        mStrokePaintFound.setStrokeWidth(mStrokeWidth);
        mStrokePaintFound.setColor(ContextCompat.getColor(context, R.color.color_green));
        mStrokePaintFound.setStyle(Paint.Style.STROKE);
        mStrokePaintFound.setStrokeCap(Paint.Cap.ROUND);

        mStrokePaintNotFound = new Paint();
        mStrokePaintNotFound.setStrokeWidth(mStrokeWidth);
        mStrokePaintNotFound.setColor(ContextCompat.getColor(context, R.color.color_red));
        mStrokePaintNotFound.setStyle(Paint.Style.STROKE);
        mStrokePaintNotFound.setStrokeCap(Paint.Cap.ROUND);


        mIdPaintWait = new Paint();
        mIdPaintWait.setColor(ContextCompat.getColor(context, R.color.color_wait));
        mIdPaintWait.setTextSize(ID_TEXT_SIZE);

        mIdPaintFound = new Paint();
        mIdPaintFound.setColor(ContextCompat.getColor(context, R.color.color_green));
        mIdPaintFound.setTextSize(ID_TEXT_SIZE);

        mIdPaintNotFound = new Paint();
        mIdPaintNotFound.setColor(ContextCompat.getColor(context, R.color.color_red));
        mIdPaintNotFound.setTextSize(ID_TEXT_SIZE);
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
            Paint printRect = mStrokePaintWait;
            Paint printText = mIdPaintWait;

            if (face.userName.startsWith("Y")) {
                printRect = mStrokePaintFound;
                printText = mIdPaintFound;

            } else if (face.userName.startsWith("N")) {
                printRect = mStrokePaintNotFound;
                printText = mIdPaintNotFound;
            }

            printRect.setStrokeWidth(mStrokeWidth / mScaleFromPreviewToView);
            canvas.drawRect(face.mBound, printRect);

            String userName = TextUtils.isEmpty(face.userName) ? "recognition" :
                    ("-1".equals(face.userName) ? "NotFound" : face.userName);
            canvas.drawText("user: " + userName,
                    face.mBound.left, face.mBound.bottom + 25, printText);

            canvas.drawText("id: " + face.face.getId(),
                    face.mBound.left, face.mBound.bottom + 45, printText);
            canvas.drawText("happiness: " + String.format("%.2f", face.face.getIsSmilingProbability()),
                    face.mBound.left, face.mBound.bottom + 65, printText);
            canvas.drawText("right eye: " + String.format("%.2f", face.face.getIsRightEyeOpenProbability()),
                    face.mBound.left, face.mBound.bottom + 85, printText);
            canvas.drawText("left eye: " + String.format("%.2f", face.face.getIsLeftEyeOpenProbability()),
                    face.mBound.left, face.mBound.bottom + 150, printText);

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