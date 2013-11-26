package com.utl.ist.cm.neartweet.util;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


public class Storage {
    static String LOG_TAG = "STORAGE";
    static String IMAGE_PREFIX = "Near_tweet_";
    static String IMAGE_SUFIX = ".jpg";

    @SuppressLint("SimpleDateFormat")
    public static Uri saveImageToGallery(
            Bitmap image,
                Context context,
                String fileUsername) throws Exception {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(root, "Near_tweet");


        if (!isExternalStorageWritable()) {
            Log.e(LOG_TAG, "External is not writablle");
        }
        if (!dir.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created: " + dir.getAbsolutePath());
        }


        String fileName = IMAGE_PREFIX + fileUsername;
        File imageFile = File.createTempFile(fileName, IMAGE_SUFIX, dir);

        FileOutputStream stream;
        stream = new FileOutputStream(imageFile);
        byte[] byteImage = BitmapHelper.bitmap2ByteArr(image);
        stream.write(byteImage);
        stream.close();

        return Uri.fromFile(imageFile);
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



}
