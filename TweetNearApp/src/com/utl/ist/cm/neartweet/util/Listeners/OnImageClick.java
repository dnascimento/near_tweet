package com.utl.ist.cm.neartweet.util.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

public class OnImageClick
        implements OnClickListener {

    Uri imageURL;
    Activity activity;

    public OnImageClick(Uri url, Activity activity) {
        super();
        imageURL = url;
        this.activity = activity;
    }


    @Override
    public void onClick(View v) {
        try {
            Intent openGallery = new Intent();
            openGallery.setAction(Intent.ACTION_VIEW);
            System.out.println(imageURL);
            Uri imageURI = Uri.parse("file://" + imageURL.getPath());
            openGallery.setDataAndType(imageURI, "image/*");
            activity.startActivity(Intent.createChooser(openGallery, "Choose your app"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public Uri getUrl() {
        return imageURL;
    }


    public void setUrl(Uri url) {
        imageURL = url;
    }


}
