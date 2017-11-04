package uz.greenwhite.facetrack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import uz.greenwhite.facetrack.bean.UserFace;
import uz.greenwhite.facetrack.common.PersonFace;
import uz.greenwhite.facetrack.ds.Pref;
import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.mold.Mold;
import uz.greenwhite.lib.mold.MoldDialogFragment;
import uz.greenwhite.lib.uzum.Uzum;
import uz.greenwhite.lib.view_setup.UI;
import uz.greenwhite.lib.view_setup.ViewSetup;

public class CreateUserFaceDialog extends MoldDialogFragment {

    public static void show(FragmentActivity activity) {
        CreateUserFaceDialog d = new CreateUserFaceDialog();
        d.show(activity.getSupportFragmentManager(), "create_user_face");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ViewSetup viewSetup = new ViewSetup(getActivity(), R.layout.create_user_face);

        final AlertDialog d = new AlertDialog.Builder(getActivity())
                .setTitle("User Name")
                .setView(viewSetup.view)
                .setNegativeButton("Close", null)
                .setPositiveButton("Create", null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = d.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createUser(viewSetup);
                    }
                });
            }
        });

        return d;
    }

    private void createUser(ViewSetup viewSetup) {
        EditText editText = viewSetup.editText(R.id.et_user_name);
        String userName = editText.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            UI.alertError(getActivity(), "user name is empty!");
            return;
        }

        Pref pref = new Pref(getActivity());
        MyArray<UserFace> userFaces = MyArray.nvl(pref.load(FaceApp.PREF_USERS, UserFace.UZUM_ADAPTER.toArray()));

        if (userFaces.nonEmpty() && userFaces.contains(userName, UserFace.KEY_ADAPTER)) {
            UI.alertError(getActivity(), "User already exists!");
            return;
        }

        userFaces = userFaces.append(new UserFace(userName, MyArray.<PersonFace>emptyArray()));

        pref.save(FaceApp.PREF_USERS, Uzum.toJson(userFaces, UserFace.UZUM_ADAPTER.toArray()));

        Mold.makeSnackBar(getActivity(), "Success create user").show();

        Mold.getContentFragment(getActivity()).reloadContent();

        dismiss();
    }
}
