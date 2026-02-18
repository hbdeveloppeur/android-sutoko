package fr.purpletear.friendzone4.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class EditTextErrorWithoutMessage extends AppCompatEditText {

    public EditTextErrorWithoutMessage(Context context) {
        super(context);
    }

    public EditTextErrorWithoutMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextErrorWithoutMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, icon, null);
    }
}
