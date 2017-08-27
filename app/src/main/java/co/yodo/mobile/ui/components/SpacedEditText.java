package co.yodo.mobile.ui.components;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;

import co.yodo.mobile.R;

/**
 * This element inserts spaces between characters in the edit text and expands the width of the
 * spaces using spannables. This is required since Android's letter spacing is not available until
 * API 21.
 */
public final class SpacedEditText extends AppCompatEditText {
    private float proportion;
    private SpannableStringBuilder originalText;

    public SpacedEditText(Context context) {
        super(context);
    }

    public SpacedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        originalText = new SpannableStringBuilder("");
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SpacedEditText);
        //Controls the ScaleXSpan applied on the injected spaces
        proportion = array.getFloat(R.styleable.SpacedEditText_spacingProportion, 1);
        array.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = new SpannableStringBuilder(text);
        final SpannableStringBuilder spacedOutString = getSpacedOutString(text);
        super.setText(spacedOutString, BufferType.SPANNABLE);
    }

    /**
     * Set the selection after recalculating the index intended by the caller.
     */
    @Override
    public void setSelection(int index) {
        //if the index is the leading edge, there are no spaces before it.
        //for all other cases, the index is preceeded by index - 1 spaces.
        int spacesUptoIndex;
        if (index == 0) {
            spacesUptoIndex = 0;
        } else {
            spacesUptoIndex = index - 1;
        }
        final int recalculatedIndex = index + spacesUptoIndex;

        super.setSelection(recalculatedIndex);
    }

    private SpannableStringBuilder getSpacedOutString(CharSequence text) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        final int textLength = text.length();
        int lastSpaceIndex = -1;

        //Insert a space in front of all characters upto the last character
        //Scale the space without scaling the character to preserve font appearance
        for (int i = 0; i < textLength - 1; i++) {
            builder.append(text.charAt(i));
            builder.append(" ");
            lastSpaceIndex += 2;
            builder.setSpan(new ScaleXSpan(proportion), lastSpaceIndex, lastSpaceIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //Append the last character
        if (textLength != 0) builder.append(text.charAt(textLength - 1));

        return builder;
    }

    public Editable getUnspacedText() {
        return this.originalText;
    }
}