/*******************************************************************************
 * microMathematics Plus - Extended visual calculator
 * *****************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mkulesh.micromath.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.properties.ImageProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class CustomImageView extends CustomTextView implements OnLongClickListener, OnClickListener
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_IMAGE_TYPE = "image_type";
    private static final String STATE_IMAGE_BITMAP = "image_bitmap";
    private static final String STATE_IMAGE_SVG = "image_svg";
    private static final String XML_PROP_BIN_ENCODING = "binEncoding";
    private static final String XML_PROP_IMAGE_TYPE = "imgType";
    private static final String XML_PROP_IMAGE_PNG = "png";
    private static final String XML_PROP_IMAGE_SVG = "svg";
    private static final int BASE64_OPTIONS = Base64.NO_WRAP | Base64.NO_PADDING;

    public enum ImageType
    {
        NONE,
        BITMAP,
        SVG
    }

    private ImageType imageType = ImageType.NONE;
    private Bitmap bitmap = null;
    private SVG svg = null;
    private String svgData = null;
    private final RectF rect = new RectF();
    private int originalWidth = 0, originalHeight = 0;

    /*********************************************************
     * Creating
     *********************************************************/

    public CustomImageView(Context context)
    {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        super.prepare(SymbolType.TEXT, activity, termChangeIf);
        setText(getContext().getResources().getString(R.string.image_fragment_text));
        clear();
    }

    @Override
    public void updateTextSize(ScaledDimensions dimen, int termDepth)
    {
        super.updateTextSize(dimen, termDepth);
        setPadding(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
    }

    public ImageType getImageType()
    {
        return imageType;
    }

    public int getOriginalWidth()
    {
        return originalWidth + 2 * strokeWidth;
    }

    public int getOriginalHeight()
    {
        return originalHeight + 2 * strokeWidth;
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    public void loadImage(ImageProperties parameters)
    {
        clear();
        final String fileName = parameters.fileName;

        if (fileName == null || fileName.length() == 0)
        {
            // not an error: just erase image end exit
            // Note: parentDocument can be empty (for example, welcome asset at the first start) 
            return;
        }

        Uri imageUri = null;
        if (parameters.isAsset())
        {
            imageUri = Uri.parse(fileName);
        }
        else
        {
            imageUri = FileUtils.catUri(getContext(), parameters.parentDirectory, fileName);
        }

        if (imageUri == null)
        {
            final String error = String.format(getContext().getResources().getString(R.string.error_file_read),
                    fileName);
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            return;
        }

        final String fileExt = FileUtils.getFileExt(FileUtils.getFileName(getContext(), imageUri)).toLowerCase(
                Locale.getDefault());

        if (fileExt.equals(".svg"))
        {
            // first, try to load image as SVG
            if (!loadSVG(imageUri))
            {
                return;
            }
        }

        if (imageType == ImageType.NONE)
        {
            // second, try to load image as a bitmap
            if (!loadBitmap(imageUri))
            {
                return;
            }
        }

        if (imageType == ImageType.NONE)
        {
            // finally, try to load image as SVG
            loadSVG(imageUri);
        }

        // error if nothing loaded
        if (imageType == ImageType.NONE)
        {
            final String error = String.format(getContext().getResources().getString(R.string.error_file_read),
                    fileName);
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Parcelable interface: procedure writes the formula state
     */
    @Override
    @SuppressLint("MissingSuperCall")
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_IMAGE_TYPE, imageType.toString());
        switch (imageType)
        {
        case NONE:
            // nothing to do
            break;
        case BITMAP:
            if (bitmap != null)
            {
                bundle.putParcelable(STATE_IMAGE_BITMAP, bitmap);
            }
            break;
        case SVG:
            if (svgData != null)
            {
                bundle.putString(STATE_IMAGE_SVG, svgData);
            }
            break;
        }
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @Override
    @SuppressLint("MissingSuperCall")
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            final ImageType type = ImageType.valueOf(bundle.getString(STATE_IMAGE_TYPE));
            switch (type)
            {
            case NONE:
                // nothing to do
                break;
            case BITMAP:
                setBitmap((Bitmap) bundle.getParcelable(STATE_IMAGE_BITMAP));
                break;
            case SVG:
                setSvg(bundle.getString(STATE_IMAGE_SVG));
                break;
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        switch (imageType)
        {
        case NONE:
            serializer.cdsect("");
            break;
        case BITMAP:
            if (bitmap != null)
            {
                try
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    final String encodedImage = Base64.encodeToString(stream.toByteArray(), BASE64_OPTIONS);
                    serializer.attribute(FormulaList.XML_NS, XML_PROP_BIN_ENCODING, "base64");
                    serializer.attribute(FormulaList.XML_NS, XML_PROP_IMAGE_TYPE, XML_PROP_IMAGE_PNG);
                    serializer.cdsect(encodedImage);
                    stream.close();
                }
                catch (OutOfMemoryError ex)
                {
                    String error = getContext().getResources().getString(R.string.error_out_of_memory);
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    return;
                }
                catch (Exception ex)
                {
                    String error = getContext().getResources().getString(R.string.error_out_of_memory);
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            else
            {
                serializer.cdsect("");
            }
            break;
        case SVG:
            if (svgData != null)
            {
                final String encodedImage = Base64.encodeToString(svgData.getBytes(), BASE64_OPTIONS);
                serializer.attribute(FormulaList.XML_NS, XML_PROP_BIN_ENCODING, "base64");
                serializer.attribute(FormulaList.XML_NS, XML_PROP_IMAGE_TYPE, XML_PROP_IMAGE_SVG);
                serializer.cdsect(encodedImage);
            }
            else
            {
                serializer.cdsect("");
            }
            break;
        }
    }

    public void readFromXml(XmlPullParser parser)
    {
        clear();
        try
        {
            // On android version < 11, the standard XmlPullParser implementation has a bug:
            // it throws a UnsupportedOperationException at getting CDSECT using nextToken().
            // Therefore, XmlPullParserFactory.newInstance().newPullParser() shall be used
            // to create parser instance in order to get it worked on Android versions 8-10.
            final String type = parser.getAttributeValue(null, XML_PROP_IMAGE_TYPE);
            if (type == null)
            {
                throw new Exception("image type is unknown");
            }
            final int event = parser.nextToken();
            if (event == XmlPullParser.CDSECT)
            {
                final String imageText = parser.getText();
                if (imageText == null || imageText.length() == 0)
                {
                    throw new Exception("empty CDSECT");
                }
                byte[] imageDecoded = Base64.decode(imageText, BASE64_OPTIONS);
                if (imageDecoded == null)
                {
                    throw new Exception("cannot decode image, string lenght = " + imageText.length());
                }
                if (XML_PROP_IMAGE_PNG.equalsIgnoreCase(type))
                {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageDecoded);
                    setBitmap(BitmapFactory.decodeStream(imageStream));
                    imageStream.close();
                }
                else if (XML_PROP_IMAGE_SVG.equalsIgnoreCase(type))
                {
                    setSvg(new String(imageDecoded));
                }
            }
            else
            {
                throw new Exception("CDSECT is not found");
            }
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, e.getLocalizedMessage());
        }
    }

    /*********************************************************
     * Painting
     *********************************************************/

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas c)
    {
        try
        {
            rect.set(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight() - 1,
                    this.getBottom() - this.getTop() - getPaddingBottom() - 1);
            paint.setColor(getCurrentTextColor());
            paint.setStrokeWidth(strokeWidth);
            if (imageType == ImageType.SVG && svg != null)
            {
                final int width = (int) rect.width();
                final int height = (int) rect.height();
                final PictureDrawable pictureDrawable = new PictureDrawable(svg.renderToPicture(width, height));
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                final Canvas c1 = new Canvas(bitmap);
                c1.drawPicture(pictureDrawable.getPicture());
                c.drawBitmap(bitmap, null, rect, paint);
            }
            else if (imageType == ImageType.BITMAP && bitmap != null)
            {
                c.drawBitmap(bitmap, null, rect, paint);
            }
            else
            {
                super.onDraw(c);
            }
        }
        catch (OutOfMemoryError ex)
        {
            String error = getContext().getResources().getString(R.string.error_out_of_memory);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            return;
        }
        catch (Exception ex)
        {
            String error = getContext().getResources().getString(R.string.error_out_of_memory);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            return;
        }
    }

    /*********************************************************
     * Special methods
     *********************************************************/

    private void clear()
    {
        imageType = ImageType.NONE;
        bitmap = null;
        svg = null;
        svgData = null;
        // Auto-setup of image size depending on display size
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        originalHeight = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels) - 2
                * getContext().getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        originalWidth = originalHeight;
    }

    private void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
        imageType = this.bitmap == null ? ImageType.NONE : ImageType.BITMAP;
        if (bitmap != null)
        {
            originalWidth = bitmap.getWidth();
            originalHeight = bitmap.getHeight();
        }
    }

    private void setSvg(String svgData)
    {
        svg = null;
        try
        {
            svg = SVG.getFromString(svgData);
            originalWidth = (int) svg.getDocumentWidth();
            originalHeight = (int) svg.getDocumentHeight();
            svg.setDocumentWidth("100%");
            svg.setDocumentHeight("100%");
            svg.setDocumentViewBox(0, 0, originalWidth, originalHeight);
        }
        catch (SVGParseException e)
        {
            // nothing to do
        }
        if (svg != null)
        {
            this.svgData = svgData;
        }
        imageType = this.svg == null ? ImageType.NONE : ImageType.SVG;
    }

    private boolean loadBitmap(Uri imageUri)
    {
        InputStream stream = FileUtils.getInputStream(getContext(), imageUri);
        if (stream != null)
        {
            try
            {
                setBitmap(BitmapFactory.decodeStream(stream));
            }
            catch (Exception e)
            {
                // nothing to do
            }
            FileUtils.closeStream(stream);
            return true;
        }
        return false;
    }

    private String getStringFromInputStream(InputStream stream) throws IOException
    {
        final BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        final StringBuilder total = new StringBuilder(stream.available());
        String line;
        while ((line = r.readLine()) != null)
        {
            total.append(line);
        }
        return total.toString();
    }

    private boolean loadSVG(Uri imageUri)
    {
        InputStream stream = FileUtils.getInputStream(getContext(), imageUri);
        if (stream != null)
        {
            try
            {
                setSvg(getStringFromInputStream(stream));
            }
            catch (Exception e)
            {
                // nothing to do
            }
            FileUtils.closeStream(stream);
            return true;
        }
        return false;
    }
}
