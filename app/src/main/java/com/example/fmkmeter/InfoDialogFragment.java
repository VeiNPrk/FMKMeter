package com.example.fmkmeter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;

public class InfoDialogFragment  extends DialogFragment {
    public static final String TAG = "DownloadUrlDialog";
    //DownloadDialogListener mListener;
    //TextView numberDeviceValue;
    TextView deviceName;
    TextView deviceSerialNo;
    TextView deviceDescription;
    TextView deviceID;
    TextView deviceLocation;
    TextView library;
    static D2xxManager.FtDeviceInfoListNode device;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*try {
            mListener = (DownloadDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement DownloadDialogListener");
        }*/
    }

    public static InfoDialogFragment newInstance(D2xxManager.FtDeviceInfoListNode _device) {
        InfoDialogFragment infoDialog = new InfoDialogFragment();
        device=_device;
        return infoDialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.maket_info_dialog, null);
        //numberDeviceValue = view.findViewById(R.id.numDev);
        deviceName = view.findViewById(R.id.device_name);
        //error_Information = view.findViewById(R.id.ErrorInformation);
        deviceSerialNo = view.findViewById(R.id.device_information_serialno);
        deviceDescription = view.findViewById(R.id.device_information_description);
        deviceID = view.findViewById(R.id.device_informatation_deviceid);
        deviceLocation = view.findViewById(R.id.device_informatation_devicelocation);
        library = view.findViewById(R.id.device_informatation_library);
        if(device!=null){
            switch (device.type) {
                case D2xxManager.FT_DEVICE_232B:
                    deviceName.setText("Device Name : FT232B");
                    break;

                case D2xxManager.FT_DEVICE_8U232AM:
                    deviceName.setText("Device Name : FT8U232AM");
                    break;

                case D2xxManager.FT_DEVICE_UNKNOWN:
                    deviceName.setText("Device Name : Unknown device");
                    break;

                case D2xxManager.FT_DEVICE_2232:
                    deviceName.setText("Device Name : FT2232");
                    break;

                case D2xxManager.FT_DEVICE_232R:
                    deviceName.setText("Device Name : FT232R");
                    break;

                case D2xxManager.FT_DEVICE_2232H:
                    deviceName.setText("Device Name : FT2232H");
                    break;

                case D2xxManager.FT_DEVICE_4232H:
                    deviceName.setText("Device Name : FT4232H");
                    break;

                case D2xxManager.FT_DEVICE_232H:
                    deviceName.setText("Device Name : FT232H");
                    break;
                case D2xxManager.FT_DEVICE_X_SERIES:
                    deviceName.setText("Device Name : FTDI X_SERIES");
                    break;
                default:
                    deviceName.setText("Device Name : FT232B");
                    break;
            }
            if (device.serialNumber == null) {
                deviceSerialNo.setText("Device Serial Number: " + device.serialNumber + "(No Serial Number)");
            } else {
                deviceSerialNo.setText("Device Serial Number: " + device.serialNumber);
            }
            if (device.description == null) {
                deviceDescription.setText("Device Description: " + device.description + "(No Description)");
            } else {
                deviceDescription.setText("Device Description: " + device.description);
            }
            deviceLocation.setText("Device Location: " + device.location);
            deviceID.setText("Device ID: " + device.id);
            library.setText("Library Version: " + D2xxManager.getLibraryVersion());
        }
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dlg_info_title))
                .setView(view)
                .setPositiveButton(getString(R.string.dlg_info_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*String url = etUrl.getText().toString();
                        mListener.onYesDownloadClicked(url);*/
                    }
                });
        return adb.create();
    }

    /*public interface DownloadDialogListener {
        public void onYesDownloadClicked(String data);
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