package com.example.samama.mymohalla;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import static java.security.AccessController.getContext;

/**
 * Created by samam on 2/7/2017.
 */

public class myMohallaTextView extends TextView {

    public myMohallaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public myMohallaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public myMohallaTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),  "fonts/breeserif.otf");
        setTypeface(tf ,1);

    }
}
