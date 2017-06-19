package abc.flaq.apps.instastudycategories.design;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class TypeWriter extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500;
    private boolean mAnimating;

    public TypeWriter(Context context) {
        super(context);
    }

    public TypeWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if (mIndex <= mText.length()) {
                mAnimating = true;
                mHandler.postDelayed(characterAdder, mDelay);
            } else {
                mAnimating = false;
            }
        }
    };

    public void animateText(CharSequence text) {
        mAnimating = true;
        mText = text;
        mIndex = 0;
        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long ms) {
        mDelay = ms;
    }

    public Boolean isAnimating() {
        return mAnimating;
    }

}
