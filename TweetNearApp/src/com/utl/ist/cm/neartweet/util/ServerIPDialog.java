package com.utl.ist.cm.neartweet.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.utl.ist.cm.neartweet.R;

public class ServerIPDialog extends DialogFragment {

	private IFragmentCommunicationListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.server_ip_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setView(view);
		builder.setTitle("Change Server IP");

		String prevServerIP = Utils.getStringFromSharedPrefs(getActivity(), R.string.server_ip_key);
		
		final EditText serverIPInput = (EditText) view
				.findViewById(R.id.serverIPEditor);
		serverIPInput.setText(prevServerIP);

		final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						mListener.onServerIPChange(serverIPInput.getText().toString());

						inputMethodManager.hideSoftInputFromWindow(
								view.getWindowToken(), 0);
					}
				}).setNegativeButton(getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						inputMethodManager.hideSoftInputFromWindow(
								view.getWindowToken(), 0);
					}
				});
		
		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (IFragmentCommunicationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement IFragmentCommunicationListener");
		}
	}
}
