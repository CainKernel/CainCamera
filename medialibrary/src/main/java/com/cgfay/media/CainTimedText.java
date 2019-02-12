package com.cgfay.media;

import android.graphics.Rect;

public class CainTimedText {

    private Rect mTextBounds = null;
    private String mTextChars = null;

    public CainTimedText(Rect bounds, String text) {
        mTextBounds = bounds;
        mTextChars = text;
    }

    public Rect getBounds() {
        return mTextBounds;
    }

    public String getText() {
        return mTextChars;
    }
}
