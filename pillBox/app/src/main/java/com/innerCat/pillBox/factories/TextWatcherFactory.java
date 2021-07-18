package com.innerCat.pillBox.factories;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class TextWatcherFactory {

    /**
     * Gets refill TextWatcher.
     *
     * @param refillInput the refill input
     * @param okButton    the ok button
     * @return the refill
     */
    public static TextWatcher getRefill( EditText refillInput, Button okButton ) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                try {
                    String trimmedRefillInput = refillInput.getText().toString().trim();
                    Integer.parseInt(trimmedRefillInput);
                    boolean refillInputPass = trimmedRefillInput.length() > 0;
                    okButton.setEnabled(refillInputPass);
                } catch (NumberFormatException e) {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged( Editable s ) {}
        };
    }

    /**
     * Gets text and image button TextWatcher.
     *
     * @param input    the input
     * @param okButton the ok button
     * @return the text and image button
     */
    public static TextWatcher getTextAndImageButton( EditText input, ImageButton okButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                if (input.getText().toString().trim().length() > 0) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged( Editable s ) {}
        };
    }

    /**
     * Gets text and image button TextWatcher. Used for the collapsingToolbarLayout in
     * MainActivity and RefillActivity
     *
     * @param input    the input
     * @param okButton the ok button
     * @return the text and image button
     */
    public static TextWatcher getTitleTextAndImageButton( EditText input,
                                                          CollapsingToolbarLayout toolbarLayout,
                                                          MenuItem okButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                String str = input.getText().toString();
                if (str.trim().length() > 0) {
                    toolbarLayout.setTitle(str);
                    okButton.setEnabled(true);
                } else {
                    toolbarLayout.setTitle("Title");
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged( Editable s ) {}
        };
    }

    /**
     * Gets text and image button TextWatcher.
     *
     * @param input    the input
     * @param okButton the ok button
     * @return the text and button
     */
    public static TextWatcher getTextAndButton( EditText input, Button okButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                if (input.getText().toString().trim().length() > 0) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged( Editable s ) {}
        };
    }
}
