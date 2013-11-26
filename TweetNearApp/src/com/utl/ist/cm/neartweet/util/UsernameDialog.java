package com.utl.ist.cm.neartweet.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.utl.ist.cm.neartweet.R;

public class UsernameDialog extends DialogFragment {

	public static final int USERNAME_MIN_LENGTH = 1;

	private IFragmentCommunicationListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.username_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setView(view);
		builder.setTitle(getArguments().getString("dialog_title"));

		String prevUsername = Utils.getStringFromSharedPrefs(getActivity(), R.string.username_key);

		final EditText usernameInput = (EditText) view
				.findViewById(R.id.nameEditor);
		usernameInput.setText(prevUsername);

		final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						mListener.onUsernameChange(usernameInput.getText().toString());

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

		final AlertDialog dialog = builder.create();

		// Disable 'Ok' button
		usernameInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

				if (s.length() < USERNAME_MIN_LENGTH) {
					okButton.setEnabled(false);
				} else
					okButton.setEnabled(true);

			}
		});
		;

		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				if (usernameInput.getText().length() < USERNAME_MIN_LENGTH) {
					Button okButton = dialog
							.getButton(AlertDialog.BUTTON_POSITIVE);
					okButton.setEnabled(false);
				}
			}
		});

		return dialog;
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
