package com.utl.ist.cm.neartweet.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapHelper {

	private static final int THUMBNAIL_SIZE = 300;
	
	public static byte[] bitmap2ByteArr(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// compression of 50%
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	public static Bitmap byteArr2Bitmap(byte[] byteArr) {
		return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
	}

	public static Bitmap byteArr2BitmapThumb(byte[] byteArr) {
		
		Bitmap bitmap = byteArr2Bitmap(byteArr);
		
		float factor = THUMBNAIL_SIZE / (float) bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, THUMBNAIL_SIZE, (int) (bitmap.getHeight() * factor), false);  
	}
}