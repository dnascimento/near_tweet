package com.utl.ist.cm.neartweet.util.Tasks;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.utl.ist.cm.neartweet.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class DownloadPicture extends
        AsyncTask<String, Void, Bitmap> {


    private final WeakReference<ImageView> imageViewReference;
    private MainActivity activity;

    public DownloadPicture(ImageView imageView, MainActivity mainActivity) {
        imageViewReference = new WeakReference<ImageView>(imageView);
        activity = mainActivity;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Log.v("URL Download:", "Start Thread");
        return downloadBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                activity.setThumbnailImage(bitmap);
               
            }
        }
    }

    public Bitmap downloadBitmap(String urlString) {
        AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        HttpGet getRequest = new HttpGet(urlString);

        Log.v("URL Download:", "Start download from: " + urlString);
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownload", "Error " + statusCode
                        + " while retrieving bitmap from " + urlString);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            getRequest.abort();
            Log.w("ImageDownload", "Error  while retrieving bitmap from " + urlString);
        } finally {
            if (client != null) {
                client.close();
            }
        }


        return null;
    }
}
