package com.example.fmkmeter.utils;

import android.content.Context;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.ArrayList;
import java.util.List;

public class D2DeviceUtils {
    D2xxManager ftdid2xx;
    private FT_Device ftDev = null;
    Context context;
    List<String> listDevicesName = new ArrayList<String>();
    D2xxManager.FtDeviceInfoListNode[] deviceList = null;
    private static final int DELAY = 100;
    byte[] buff;
    int openIndex = 0;
    int currentIndex = -1;

    public D2DeviceUtils(Context _context) {
        context = _context;
        try {
            ftdid2xx = D2xxManager.getInstance(context);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
    }

    public FT_Device getDevice() {
        return ftDev;
    }

    public D2xxManager.FtDeviceInfoListNode getDeviceNode() {
        if (deviceList != null)
            return deviceList[openIndex];
        else
            return null;
    }

    public String getDeviceName(D2xxManager.FtDeviceInfoListNode device) {
        String deviceName = "";
        switch (device.type) {
            case D2xxManager.FT_DEVICE_232B:
                deviceName = "FT232B";
                break;
            case D2xxManager.FT_DEVICE_8U232AM:
                deviceName = "FT8U232AM";
                break;
            case D2xxManager.FT_DEVICE_UNKNOWN:
                deviceName = "Unknown device";
                break;
            case D2xxManager.FT_DEVICE_2232:
                deviceName = "FT2232";
                break;
            case D2xxManager.FT_DEVICE_232R:
                deviceName = "FT232R";
                break;
            case D2xxManager.FT_DEVICE_2232H:
                deviceName = "FT2232H";
                break;
            case D2xxManager.FT_DEVICE_4232H:
                deviceName = "FT4232H";
                break;
            case D2xxManager.FT_DEVICE_232H:
                deviceName = "FT232H";
                break;
            case D2xxManager.FT_DEVICE_X_SERIES:
                deviceName = "FTDI X_SERIES";
                break;
            default:
                deviceName = "FT232B";
                break;
        }
        return deviceName;
    }

    public List<String> getDeviceList() {
        int devCount = 0;
        try {
            String deviceName = "";
            devCount = ftdid2xx.createDeviceInfoList(context);
            //Log.i("FtdiModeControl", "Device number = " + Integer.toString(devCount));
            if (devCount > 0) {
                //binding.tvDevCount.setVisibility(View.GONE);
                deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
                ftdid2xx.getDeviceInfoList(devCount, deviceList);
                for (D2xxManager.FtDeviceInfoListNode device : deviceList) {
                    deviceName = getDeviceName(device);
                    listDevicesName.add(deviceName);
                }
            }
        } catch (Exception ex) {
            //binding.tvRead.setText(ex.toString());
        }
        return listDevicesName;
    }

    public String connectDevice(int deviceIndex) throws InterruptedException {
        int devCount = 0;
        String str = "";
        openIndex = deviceIndex;
        try {
            devCount = ftdid2xx.createDeviceInfoList(context);
            //Log.i("FtdiModeControl", "Device number = " + Integer.toString(devCount));
            if (devCount > 0) {
                //binding.tvDevCount.setVisibility(View.GONE);
                str = connectFunction();
            } else {
                //binding.tvDevCount.setVisibility(View.VISIBLE);
                //binding.tvDevCount.setText("Number of devices: 0");
                str = "Number of devices: 0";
            }
        } catch (Exception ex) {
            Log.e("D2DeviceUtils", "Exception connectDevice " + ex);
            str = ex.toString();
            //binding.tvRead.setText(ex.toString());
        }
        return str;
    }

    public String connectFunction() {
        int tmpProtNumber = openIndex + 1;
        String str = "";
        //if (currentIndex != openIndex) {
        if (null == ftDev) {
            ftDev = ftdid2xx.openByIndex(context, openIndex);
        } else {
            synchronized (ftDev) {
                if (!ftDev.isOpen())
                    ftDev = ftdid2xx.openByIndex(context, openIndex);
            }
        }
        //uart_configured = false;
        /*} else {
            str="Device port " + tmpProtNumber + " is already opened";
            //Toast.makeText(context, "Device port " + tmpProtNumber + " is already opened", Toast.LENGTH_LONG).show();
            return str;
        }*/

        if (ftDev == null) {
            str = "Open device port(" + tmpProtNumber + ") NG, ftDev == null";
            //Toast.makeText(context, "open device port(" + tmpProtNumber + ") NG, ftDev == null", Toast.LENGTH_LONG).show();
            return str;
        }

        if (true == ftDev.isOpen()) {
            currentIndex = openIndex;
            str = "Open device port(" + tmpProtNumber + ") OK";
            //Toast.makeText(context, "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();
        } else {
            str = "Open device port(" + tmpProtNumber + ") NG";
            // Toast.makeText(context, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
        }
        return str;
    }

    public boolean isOpened() {
        if (ftDev == null)
            return false;
        else
            return ftDev.isOpen();
    }

    public void restartDevice() {
        int iavailable = ftDev.getQueueStatus();
        if (iavailable > 0)
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        if (ftDev.isOpen())
            ftDev.close();
        ftDev = ftdid2xx.openByIndex(context, openIndex);

        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.restartInTask();
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSettings() {
        buff = new byte[3];
        buff[0] = (byte) 0xA5;
        buff[1] = 4;
        buff[2] = (byte) 240;
        int i = ftDev.getQueueStatus();
        ftDev.write(buff, buff.length);
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void singleIzm() {
        buff = new byte[2];
        buff[0] = (byte) 0xA5;
        buff[1] = 0x03;
        int i = ftDev.getQueueStatus();
        int i1 = ftDev.write(buff, buff.length);
    }

    public void startIzm() {
        buff = new byte[2];
        buff[0] = (byte) 0xA5;
        buff[1] = 0x06;
        int i = ftDev.getQueueStatus();
        ftDev.write(buff, buff.length);
    }

    public void finishIzm() {
        buff = new byte[2];
        buff[0] = (byte) 0xA5;
        buff[1] = 0x02;
        ftDev.write(buff, buff.length);
    }
}
