package com.mkulesh.micromath.formula;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.mkulesh.micromath.R;

/**
 * Created by family on 10/14/17.
 */
public class PaletteButton extends AppCompatImageButton
{
    public enum Category
    {
        UPDATE_INTERVAL,
        UPDATE_TERM
    }

    private String code = null;
    private final boolean[] enabled = new boolean[Category.values().length];

    public PaletteButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        enableAll();
    }

    public PaletteButton(Context context)
    {
        super(context);
        enableAll();
    }

    public PaletteButton(Context context, int imageId, int descriptionId, String code)
    {
        super(context);
        final int buttonSize = context.getResources().getDimensionPixelSize(R.dimen.activity_toolbar_height) - 2
                * context.getResources().getDimensionPixelSize(R.dimen.activity_palette_vertical_padding);
        setImageResource(imageId);
        setBackgroundResource(R.drawable.clickable_background);
        setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
        if (descriptionId != Palette.NO_BUTTON)
        {
            setContentDescription(context.getResources().getString(descriptionId));
            setLongClickable(true);
        }
        this.code = code;
        enableAll();
    }

    public String getCode()
    {
        return code;
    }

    private void enableAll()
    {
        for (int i = 0; i < enabled.length; i++)
        {
            enabled[i] = true;
        }
    }

    public void setEnabled(Category t, boolean value)
    {
        enabled[t.ordinal()] = value;
        super.setEnabled(true);
        for (int i = 0; i < enabled.length; i++)
        {
            if (!enabled[i])
            {
                super.setEnabled(false);
                break;
            }
        }
    }
}
