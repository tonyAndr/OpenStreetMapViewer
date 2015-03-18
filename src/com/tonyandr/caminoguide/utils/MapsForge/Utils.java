package com.tonyandr.caminoguide.utils.MapsForge;

/**
 * Created by Tony on 18-Mar-15.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Utility functions that can be used across different mapsforge based
 * activities.
 */
public final class Utils {
    /**
     * Compatibility method.
     *
     * @param a
     *            the current activity
     */
    @TargetApi(11)
    public static void enableHome(Activity a) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            a.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Compatibility method.
     *
     * @param view
     *            the view to set the background on
     * @param background
     *            the background
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    public static Marker createMarker(Context c, int resourceIdentifier,
                               LatLong latLong) {
        Drawable drawable = c.getResources().getDrawable(resourceIdentifier);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
    }

    public static Paint createPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    public static Marker createTappableMarker(final Context c, int resourceIdentifier,
                                       LatLong latLong) {
        Drawable drawable = c.getResources().getDrawable(resourceIdentifier);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        bitmap.incrementRefCount();
        return new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
            @Override
            public boolean onTap(LatLong geoPoint, Point viewPosition,
                                 Point tapPoint) {
                if (contains(viewPosition, tapPoint)) {
                    Toast.makeText(c,
                            "The Marker was tapped " + geoPoint.toString(),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        };
    }

    static Bitmap viewToBitmap(Context c, View view) {
        view.measure(MeasureSpec.getSize(view.getMeasuredWidth()),
                MeasureSpec.getSize(view.getMeasuredHeight()));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Drawable drawable = new BitmapDrawable(c.getResources(),
                android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
        view.setDrawingCacheEnabled(false);
        return AndroidGraphicFactory.convertToBitmap(drawable);
    }

    private Utils() {
        throw new IllegalStateException();
    }

}