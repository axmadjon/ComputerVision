package uz.greenwhite.facetrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import uz.greenwhite.facetrack.arg.ArgRecognition;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        findViewById(R.id.btn_show_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognitionFragment.open(IntroActivity.this, new ArgRecognition(ArgRecognition.RECOGNITION));
            }
        });

        findViewById(R.id.btn_train_person).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceUserListFragment.open(IntroActivity.this);
            }
        });
    }
}
