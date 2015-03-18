package com.tonyandr.caminoguide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;

import com.tonyandr.caminoguide.R;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Created by Tony on 03-Mar-15.
 */
public class MyCustomLocationOverlay extends MyLocationNewOverlay {

    protected final Paint mPaint = new Paint();
    protected final Paint mCirclePaint = new Paint();

    private Context context;


    private final Point mMapCoordsProjected = new Point();
    private final Point mMapCoordsTranslated = new Point();

    protected boolean mDrawAccuracyEnabled = true;

    /** Coordinates the feet of the person are located scaled for display density. */


    // to avoid allocations during onDraw
    private final float[] mMatrixValues = new float[9];
    private final Matrix mMatrix = new Matrix();

    public MyCustomLocationOverlay(Context context, MapView mapView) {
        this(context, new GpsMyLocationProvider(context), mapView);
        this.context = context;
    }

    public MyCustomLocationOverlay(Context context, IMyLocationProvider myLocationProvider, MapView mapView) {
        this(myLocationProvider, mapView, new DefaultResourceProxyImpl(context));
        this.context = context;
    }

    public MyCustomLocationOverlay(IMyLocationProvider myLocationProvider, MapView mapView, ResourceProxy resourceProxy) {
        super(myLocationProvider, mapView, resourceProxy);
    }


    protected void drawMyLocation(final Canvas canvas, final MapView mapView, final Location lastFix) {
        final Projection pj = mapView.getProjection();
        pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);
        Bitmap directionArrow = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.direction_arrow);
        float mDirectionArrowCenterX;
        float mDirectionArrowCenterY;
        mDirectionArrowCenterX = directionArrow.getWidth() / 2.0f - 0.5f;
        mDirectionArrowCenterY = directionArrow.getHeight() / 2.0f - 0.5f;

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    mapView.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);
        }

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        if (DEBUGMODE) {
            final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
                    / mMatrixValues[Matrix.MSCALE_X];
            final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
                    / mMatrixValues[Matrix.MSCALE_Y];
            canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
            canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
            canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
            canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
        }

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);
        if (lastFix.hasBearing()) {
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), mMapCoordsTranslated.x, mMapCoordsTranslated.y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
            // Draw the bitmap
            canvas.drawBitmap(directionArrow, mMapCoordsTranslated.x
                            - mDirectionArrowCenterX, mMapCoordsTranslated.y - mDirectionArrowCenterY,
                    mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-mMapView.getMapOrientation(), mMapCoordsTranslated.x,
                    mMapCoordsTranslated.y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
            // Draw the bitmap
            canvas.drawBitmap(mPersonBitmap, mMapCoordsTranslated.x - mPersonHotspot.x,
                    mMapCoordsTranslated.y - mPersonHotspot.y, mPaint);
            canvas.restore();
        }
    }




}
