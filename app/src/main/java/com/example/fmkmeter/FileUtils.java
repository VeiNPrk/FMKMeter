package com.example.fmkmeter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import androidx.preference.PreferenceManager;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtils {
    //Context context=null;
    //private static String sOutPutfile = "Films.xml";
    private static final String ns = null;
    //private File mFileOutPut;

    public static String saveFile(List<Signal> signals, List<Signal> integrateSignals, Context context, String sOutPutfile) {
        String returnStr = "";
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.d("Main", context.getString(R.string.msg_ext_stor_not));
            returnStr = context.getString(R.string.msg_ext_stor_not);
        } else {
            //String root = context.getExternalFilesDir("files").toString();
            //File myDir = new File(context.getExternalFilesDir("files").getParent());
            String root = Environment.getExternalStorageDirectory().toString();
            //File myDir = new File(root + "/FmkMeterFiles");
            File myDir = new File(Environment.getExternalStorageDirectory(), "FmkMeterFiles");
            //File(Environment.getExternalStorageDirectory(), "NewDirectory");
            //mediaFile.mkdirs();
            if (!myDir.exists()) {
                if (!myDir.mkdirs()) {
                    Log.d("File", "mkdir not");
                    return "mkdir not";
                }
            }

            File file = new File(myDir, sOutPutfile);
            if (file.exists())
                file.delete();
            //returnStr = writeXml(signals, file, context);
            returnStr = writeTxt(signals, integrateSignals, file, context);
        }
        return returnStr;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static String writeTxt(List<Signal> signals, List<Signal> integrateSignals, File mFileOutPut, Context context) {
        String returnStr = "";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mFileOutPut);
            boolean tf = false;
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            //try {
                tf = sharedPreferences.getBoolean("tf_integr", false);
           /* } catch (NumberFormatException nfe) {
                tf = false;
            }*/
            //OutputStreamWriter myOutWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            for (Signal signal : signals) {
                bw.write(String.valueOf(signal.getValue()));
                bw.newLine();
            }
            if(tf) {
                bw.write("000000000");
                bw.newLine();
                for (Signal signal : integrateSignals) {
                    bw.write(String.valueOf(signal.getValue()));
                    bw.newLine();
                }
            }
            bw.close();
            //myOutWriter.close();
            fileOutputStream.close();

            returnStr = context.getString(R.string.msg_file_save_success) + mFileOutPut.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        }
        return returnStr;
    }

    public static String writeXml(List<Signal> signals, File mFileOutPut, Context context) {
        String returnStr = "";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mFileOutPut);
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            Date date = new Date();
            SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String currentDate = dateFormatWithZone.format(date);

            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(ns, "date");
            xmlSerializer.text(currentDate);
            xmlSerializer.endTag(null, "date");
            xmlSerializer.startTag(null, "doc");

            insertSignals(xmlSerializer, signals);

            xmlSerializer.endTag(ns, "doc");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            fileOutputStream.write(dataWrite.getBytes());
            fileOutputStream.close();
            returnStr = context.getString(R.string.msg_file_save_success) + mFileOutPut.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            returnStr = e.getMessage();
        }
        return returnStr;
    }

    public static void insertSignals(XmlSerializer xmlSerializer, List<Signal> signals) throws IOException {
        final String value = "value";
        String number = "number";
        /*String runningTime = "runningTime";
        String country = "country";
        String director = "director";
        String cast = "cast";*/
        int i = 1;
        for (Signal signal : signals) {
            xmlSerializer.startTag(ns, value);
            xmlSerializer.attribute(ns, number, String.valueOf(i));
            xmlSerializer.text(String.valueOf(signal.getValue()));
            xmlSerializer.endTag(null, value);
            i++;

            /*xmlSerializer.startTag(ns, runningTime);
            xmlSerializer.text(filmObject.getRunningTime());
            xmlSerializer.endTag(ns, runningTime);*/

            /*xmlSerializer.startTag(ns, country);
            xmlSerializer.text(filmObject.getCountry());
            xmlSerializer.endTag(ns, country);

            xmlSerializer.startTag(ns, director);
            xmlSerializer.text(filmObject.getDirector());
            xmlSerializer.endTag(ns, director);

            xmlSerializer.startTag(ns, cast);
            insertCast(xmlSerializer, filmObject.getCast());
            xmlSerializer.endTag(ns, cast);*/
        }
    }
}
