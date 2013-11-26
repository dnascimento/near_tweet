package com.utl.ist.cm.neartweet.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;

import com.utl.ist.cm.neartweet.R;

public class Utils {

	public static String getStringFromSharedPrefs(Context context, int key) {

		SharedPreferences sharedPrefs = context.getSharedPreferences(
				context.getString(R.string.preference_file_key),
				Context.MODE_PRIVATE);

		return sharedPrefs.getString(context.getString(key), "");
	}

	public static void commitStringToSharedPrefs(Context context, int key,
			String value) {

		SharedPreferences.Editor sharedPrefsEditor = context
				.getSharedPreferences(
						context.getString(R.string.preference_file_key),
						Context.MODE_PRIVATE).edit();

		sharedPrefsEditor.putString(context.getString(key), value);
		sharedPrefsEditor.commit();
	}

	public static String getElapsedTime(Date created) {
		long duration = System.currentTimeMillis() - created.getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		if (days > 0) {
			return days + " days";
		}
		if (hours > 0) {
			return hours + " hrs";
		}
		if (minutes > 0) {
			return minutes + " minutes";
		}
		if (seconds < 0) {
			seconds = 0;
		}
		return seconds + " seconds";
	}

	// public static void saveSettingsFile(String filename, String string,
	// MainActivity activity) throws IOException {
	// FileOutputStream fos = activity.openFileOutput(filename,
	// Context.MODE_PRIVATE); // openFileOutput
	// fos.write((string + "\n").getBytes());
	// fos.close();
	// }
	//
	// public static String readSettingsFile(String filename, MainActivity
	// activity)
	// throws IOException {
	// FileInputStream fis = activity.openFileInput(filename); // openFileOutput
	// Scanner in = new Scanner(fis);
	// String res = in.nextLine();
	// fis.close();
	// return res;
	// }
}