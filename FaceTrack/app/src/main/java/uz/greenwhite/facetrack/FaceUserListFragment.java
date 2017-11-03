package uz.greenwhite.facetrack;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import uz.greenwhite.facetrack.arg.ArgRecognition;
import uz.greenwhite.facetrack.bean.UserFace;
import uz.greenwhite.facetrack.ds.Pref;
import uz.greenwhite.lib.collection.MyArray;
import uz.greenwhite.lib.mold.Mold;
import uz.greenwhite.lib.mold.MoldContentRecyclerFragment;
import uz.greenwhite.lib.mold.RecyclerAdapter;
import uz.greenwhite.lib.view_setup.UI;
import uz.greenwhite.lib.view_setup.ViewSetup;

public class FaceUserListFragment extends MoldContentRecyclerFragment<UserFace> {

    public static void open(Activity activity) {
        Mold.openContent(activity, FaceUserListFragment.class);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Mold.setTitle(getActivity(), "Users");

        setEmptyText("Face is empty\nYou can create a new UserFace");

        Mold.makeFloatAction(getActivity(), R.drawable.ic_add_black_24dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateUserFaceDialog.show(getActivity());
            }
        });

        reloadContent();
    }

    @Override
    protected void onItemClick(RecyclerAdapter.ViewHolder holder, UserFace item) {
        if (item.faceEncodes.nonEmpty()) {
            UI.alertError(getActivity(), "You can train only one Face!");
        } else {
            TrainFaceFragment.open(getActivity(), new ArgRecognition(item, ArgRecognition.NEW_USER));
        }
    }

    @Override
    public void reloadContent() {
        Pref pref = new Pref(getActivity());
        setListItems(MyArray.nvl(pref.load(FaceApp.PREF_USERS, UserFace.UZUM_ADAPTER.toArray())));
    }

    @Override
    protected int adapterGetLayoutResource() {
        return android.R.layout.simple_list_item_2;
    }

    @Override
    protected void adapterPopulate(ViewSetup viewSetup, UserFace userFace) {
        viewSetup.textView(android.R.id.text1).setText(userFace.name);
        viewSetup.textView(android.R.id.text2).setText(String.format("User Face Encode Lenght: %s",
                String.valueOf(userFace.faceEncodes.size())));
    }
}
