package com.example.fmkmeter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SaveFileDialogFragment extends DialogFragment {
    public static final String TAG = "DownloadUrlDialog";
    //DownloadDialogListener mListener;
    //TextView numberDeviceValue;
    private EditText etUchastok;
    private EditText etPut;
    private EditText etNOpora;
    private EditText etNIzm;
    private TextView tvError;
    private TableRow tblRowNIzm;
    private static String saveUchastok="";
    private static String saveNIzm="";
    private static boolean isAutoSave = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public interface SaveFileDialogListener {
        public void onSaveDialogPositiveClick(String uchastok, String put, String nOpora, String nIzm);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    static SaveFileDialogListener mListener;


    public static SaveFileDialogFragment newInstance(SaveFileDialogListener _mListener, String _saveUchastok, String _saveNIzm) {
        SaveFileDialogFragment infoDialog = new SaveFileDialogFragment();
        saveUchastok=_saveUchastok;
        saveNIzm=_saveNIzm;
        mListener=_mListener;
        return infoDialog;
    }

    public static SaveFileDialogFragment newInstance(SaveFileDialogListener _mListener, boolean _isAutoSave) {
        SaveFileDialogFragment infoDialog = new SaveFileDialogFragment();
        isAutoSave = _isAutoSave;
        saveNIzm="1";
        mListener=_mListener;
        return infoDialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.maket_save_dialog, null);
        etUchastok = view.findViewById(R.id.et_uchastok);
        etPut = view.findViewById(R.id.et_put);
        etNOpora = view.findViewById(R.id.et_nopora);
        etNIzm = view.findViewById(R.id.et_nizm);
        tvError = view.findViewById(R.id.tv_dlg_error);
        etUchastok.setText(saveUchastok);
        tblRowNIzm = view.findViewById(R.id.tbl_row_nizm);
        if(isAutoSave)
            tblRowNIzm.setVisibility(View.GONE);
        else {
            tblRowNIzm.setVisibility(View.VISIBLE);
            etNIzm.setText(saveNIzm);
        }
        /*if(savedInstanceState!=null){
            saveUchastok = savedInstanceState.getString(KEY_UCHASTOK_FIELD);
            saveNIzm = savedInstanceState.getString(KEY_NIZM_FIELD);
            etUchastok.setText(saveUchastok);
            etNIzm.setText(saveNIzm);
        }*/

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dlg_save_title))
                .setView(view)
                .setPositiveButton(getString(R.string.dlg_info_yes), null/*new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }*/)
                .setNegativeButton(getString(R.string.dlg_permission_cancel), null/*new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }*/);
        if(isAutoSave)
            adb.setMessage(R.string.dlg_autosave_message);
        return adb.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvError.setVisibility(View.GONE);
                    saveUchastok = etUchastok.getText().toString();
                    String put = etPut.getText().toString();
                    String nOpora = etNOpora.getText().toString();
                    if(!isAutoSave)
                        saveNIzm = etNIzm.getText().toString();

                    if(saveUchastok.equals("") || saveNIzm.equals("")) {
                        tvError.setVisibility(View.VISIBLE);
                    }
                    else {
                        mListener.onSaveDialogPositiveClick(saveUchastok, put, nOpora, saveNIzm);
                        dismiss();
                    }
                }
            });
        }
    }

  /*  @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_UCHASTOK_FIELD, saveUchastok);
        outState.putString(KEY_NIZM_FIELD, saveNIzm);
    }*/

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "onCancel");
    }
}
