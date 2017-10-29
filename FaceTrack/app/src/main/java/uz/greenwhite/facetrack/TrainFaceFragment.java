package uz.greenwhite.facetrack;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.my.jni.dlib.DLibLandmarks68Detector;

import java.io.IOException;

import uz.greenwhite.facetrack.arg.ArgRecognition;
import uz.greenwhite.facetrack.bean.UserFace;
import uz.greenwhite.facetrack.common.CameraSourcePreview;
import uz.greenwhite.facetrack.common.FaceOverlayView;
import uz.greenwhite.facetrack.common.ICameraMetadata;
import uz.greenwhite.facetrack.common.OnFaceListener;
import uz.greenwhite.facetrack.common.TrainVisionFaceDetector;
import uz.greenwhite.facetrack.ds.Pref;
import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.collection.MyPredicate;
import uz.greenwhite.lib.job.JobMate;
import uz.greenwhite.lib.job.Promise;
import uz.greenwhite.lib.job.ShortJob;
import uz.greenwhite.lib.mold.Mold;
import uz.greenwhite.lib.mold.MoldContentFragment;
import uz.greenwhite.lib.view_setup.UI;
import uz.greenwhite.lib.view_setup.ViewSetup;

public class TrainFaceFragment extends MoldContentFragment implements ICameraMetadata {

    public static void open(Activity activity, ArgRecognition arg) {
        Mold.openContent(activity, TrainFaceFragment.class,
                Mold.parcelableArgument(arg, ArgRecognition.UZUM_ADAPTER));
    }

    public ArgRecognition getArgRecognition() {
        return Mold.parcelableArgument(this, ArgRecognition.UZUM_ADAPTER);
    }


    private static final String DLIB_LANDMARK_PATH = "storage/emulated/0/Download/my.demo.dlib/shape_predictor_68_face_landmarks.dat";
    private static final String DLIB_RECOGNITION_PATH = "storage/emulated/0/Download/my.demo.dlib/dlib_face_recognition_resnet_model_v1.dat";


    //TODO libmobile_vision_face.so

    private Detector<Face> detector;

    private CameraSourcePreview mCameraView;
    private FaceOverlayView mOverlayView;
    private DLibLandmarks68Detector dlibDetector;
    private TrainVisionFaceDetector visionFaceDetector;

    private final JobMate jobMate = new JobMate();
    private ViewSetup vsRoot;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.vsRoot = new ViewSetup(inflater, container, R.layout.camera_recognition);
        return this.vsRoot.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mCameraView = vsRoot.id(R.id.csp_camera);
        this.mOverlayView = vsRoot.id(R.id.fov_overlay);

    }


    private void init() {
        this.detector = new FaceDetector.Builder(getActivity())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .build();

        if (dlibDetector == null) {
            jobMate.executeWithDialog(getActivity(), new ShortJob<DLibLandmarks68Detector>() {
                @Override
                public DLibLandmarks68Detector execute() throws Exception {
                    try {
                        final DLibLandmarks68Detector dlibDetector = new DLibLandmarks68Detector();

                        dlibDetector.prepareLandmark(DLIB_LANDMARK_PATH);
                        dlibDetector.prepareRecognition(DLIB_RECOGNITION_PATH);
                        if (dlibDetector.isFaceLandmarksDetectorReady()) {
                            return dlibDetector;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).done(new Promise.OnDone<DLibLandmarks68Detector>() {
                @Override
                public void onDone(DLibLandmarks68Detector dLibLandmarks68Detector) {
                    TrainFaceFragment.this.dlibDetector = dLibLandmarks68Detector;
                    TrainFaceFragment.this.visionFaceDetector.setDLibDetector(dlibDetector);
                }
            }).fail(new Promise.OnFail() {
                @Override
                public void onFail(Throwable throwable) {
                    UI.alertError(getActivity(), throwable);
                }
            });
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        init();

        this.visionFaceDetector = new TrainVisionFaceDetector(this, detector, mOverlayView);

        if (dlibDetector != null) {
            this.visionFaceDetector.setDLibDetector(dlibDetector);
        }

        final int previewWidth = 320;
        final int previewHeight = 240;

        // Set the preview config.
        if (isPortraitMode()) {
            mOverlayView.setCameraPreviewSize(previewHeight, previewWidth);
        } else {
            mOverlayView.setCameraPreviewSize(previewWidth, previewHeight);
        }

        // Create camera source.
        final CameraSource source = new CameraSource.Builder(getActivity(), visionFaceDetector)
                .setRequestedPreviewSize(previewWidth, previewHeight)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30f)
                .build();

        // Open the camera.
        try {
            mCameraView.start(source);
        } catch (IOException e) {
            e.printStackTrace();
        }

        visionFaceDetector.setOnFaceListener(new OnFaceListener() {

            @Override
            public void detect(Face face, final Bitmap bitmap, final Rect bound) {
                if (dlibDetector != null) {
                    jobMate.executeWithDialog(getActivity(), new ShortJob<String[]>() {
                        @Override
                        public String[] execute() throws Exception {
                            return dlibDetector.recognitionFace(bitmap, bound.left,
                                    bound.top, bound.right, bound.bottom);
                        }
                    }).done(new Promise.OnDone<String[]>() {
                        @Override
                        public void onDone(String[] results) {
                            ArgRecognition arg = getArgRecognition();
                            Pref pref = new Pref(getActivity());
                            MyArray<UserFace> users = MyArray.nvl(pref.load(FaceApp.PREF_USERS, UserFace.UZUM_ADAPTER.toArray()));

                            final UserFace found = users.find(arg.userFace.name, UserFace.KEY_ADAPTER);
                            users = users.filter(new MyPredicate<UserFace>() {
                                @Override
                                public boolean apply(UserFace val) {
                                    return !found.name.equals(val.name);
                                }
                            });

                            users = users.append(new UserFace(found.name, found.faceEncodes.append(MyArray.from(results))));

                            pref.save(FaceApp.PREF_USERS, users, UserFace.UZUM_ADAPTER.toArray());
                            Mold.makeSnackBar(getActivity(), "Success add new FaceEncode").show();
                        }
                    });
                }
            }

            @Override
            public void error(String error) {
                vsRoot.textView(R.id.tv_messages).setVisibility(View.VISIBLE);
                vsRoot.textView(R.id.tv_messages).setText(UI.toRedText(error));
            }
        });

        ViewSetup vsFooter = new ViewSetup(getActivity(), R.layout.train_bottom_frame);
        BottomSheetBehavior sheetBehavior = Mold.makeBottomSheet(getActivity(), vsFooter.view);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        vsFooter.id(R.id.btn_get_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visionFaceDetector.setClickUserFace(true);
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        // Close camera.
        mCameraView.release();

    }

    @Override
    public void onStop() {
        super.onStop();
        jobMate.stopListening();
    }

    @Override
    public boolean isFacingFront() {
        return CameraSource.CAMERA_FACING_FRONT ==
                mCameraView.getCameraSource()
                        .getCameraFacing();
    }

    @Override
    public boolean isFacingBack() {
        return CameraSource.CAMERA_FACING_BACK ==
                mCameraView.getCameraSource()
                        .getCameraFacing();
    }

    @Override
    public boolean isPortraitMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public boolean isLandscapeMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}
