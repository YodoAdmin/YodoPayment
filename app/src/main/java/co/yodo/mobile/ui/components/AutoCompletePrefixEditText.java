package co.yodo.mobile.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

/**
 * Created by hei on 19/11/17.
 * AutComplete edit text with prefix
 */
public class AutoCompletePrefixEditText extends AppCompatAutoCompleteTextView {
    float mOriginalLeftPadding = -1;

    public AutoCompletePrefixEditText(Context context) {
        super(context);
    }

    public AutoCompletePrefixEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompletePrefixEditText(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculatePrefix();
    }

    private void calculatePrefix() {
        if (mOriginalLeftPadding == -1) {
            String prefix = (String) getTag();
            float[] widths = new float[prefix.length()];
            getPaint().getTextWidths(prefix, widths);
            float textWidth = 0;
            for (float w : widths) {
                textWidth += w;
            }
            mOriginalLeftPadding = getCompoundPaddingLeft();
            setPadding((int) (textWidth + mOriginalLeftPadding),
                    getPaddingRight(), getPaddingTop(),
                    getPaddingBottom());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String prefix = (String) getTag();
        canvas.drawText(prefix, mOriginalLeftPadding, getLineBounds(0, null), getPaint());
    }
}
