package com.saiga.find.messagefinder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class MyDialogFragment extends DialogFragment {

    public MyDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme);
  //      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.MyDialogTheme);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // setting the dialog with custom layout
        builder.setView(inflater.inflate(R.layout.dialog_layout, null));
        //builder.setTitle(R.string.dialog_text);
        //builder.setMessage(R.string.helper_text_keyword);
        builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // when user presses SET, call request permissions
                        ((MainActivity)MyDialogFragment.this.requireActivity()).requestContactPermission();


                    }
                })
                .setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When user presses dismiss,
                        // call this so that, when user comes back to the app, the permission dialog doesn't appear again
                        ((MainActivity)MyDialogFragment.this.requireActivity()).setPopup(true);

                    }
                })
                .setNeutralButton("Dont Show Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // when user presses neutral, mark auto suggestion as off in persistent storage
                        ((MainActivity)MyDialogFragment.this.requireActivity()).setUserConfig(true);
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // set the autosuggestions Enabled variable to false;
        ((MainActivity)requireActivity()).setPopup(false);

    }
}
