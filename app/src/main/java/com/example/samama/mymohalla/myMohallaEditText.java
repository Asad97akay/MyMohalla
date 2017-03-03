package com.example.samama.mymohalla;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by samam on 2/7/2017.
 */

public class myMohallaEditText extends EditText {
    public myMohallaEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public myMohallaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public myMohallaEditText(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),  "fonts/breeserif.otf");
        setTypeface(tf ,1);

    }
}
