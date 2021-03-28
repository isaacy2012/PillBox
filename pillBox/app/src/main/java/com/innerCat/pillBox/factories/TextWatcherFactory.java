package com.innerCat.pillBox.factories;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

public class TextWatcherFactory {
    public static TextWatcher getNonEmptyTextAndStockWatcher( EditText nameInput, EditText stockInput, Button okButton ) {
       return new TextWatcher() {
           @Override
           public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

           @Override
           public void onTextChanged( CharSequence s, int start, int before, int count ) {
               try {
                   String trimmedStockInput = stockInput.getText().toString().trim();
                   int stockInputInt = Integer.parseInt(trimmedStockInput);
                   boolean stockInputPass = trimmedStockInput.length() > 0;
                   boolean nameInputPass = nameInput.getText().toString().trim().length() > 0;
                   okButton.setEnabled(nameInputPass && stockInputPass);
               } catch (NumberFormatException e) {
                   okButton.setEnabled(false);
               }
           }

           @Override
           public void afterTextChanged( Editable s ) {}
       };
    }

    public static TextWatcher getNonEmptyRefillWatcher( EditText refillInput, Button okButton ) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                try {
                    String trimmedRefillInput = refillInput.getText().toString().trim();
                    int refillInputInt = Integer.parseInt(trimmedRefillInput);
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

    public static TextWatcher getNonEmptyTextWatcher( EditText input, Button okButton) {
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
