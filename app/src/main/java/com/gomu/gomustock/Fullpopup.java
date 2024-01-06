package com.gomu.gomustock;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class Fullpopup extends Dialog {
        private TextView txt_contents;
        public Fullpopup(@NonNull Context context, String contents) {
            super(context);
            setContentView(R.layout.dialog_full_popup);
            txt_contents = findViewById(R.id.fullpop);
            txt_contents.setText(contents);
        }
}
