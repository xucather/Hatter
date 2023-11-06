package edu.msu.catherinemadhatter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * The view we will draw out hatter in
 */
public class HatterView extends View {

    /**
     * Feather Bitmap
     */
    public Bitmap featherBitmap = null;

    // Key string for imageUri
    private static final String imageKey = "imageUri";

    /**
     * First touch status
     */
    private Touch touch1 = new Touch();

    /**
     * Second touch status
     */
    private Touch touch2 = new Touch();

    /**
     * Paint to use when drawing the custom color hat
     */
    private Paint customPaint;

    /**
     * The bitmap to draw the hat
     */
    private Bitmap hatBitmap = null;

    /**
     * The bitmap to draw the hat band. We draw this
     * only when drawing the custom color hat, so we
     * don't color the hat band
     */
    private Bitmap hatbandBitmap = null;

    /*
     * The ID values for each of the hat types. The values must
     * match the index into the array hats_spinner in arrays.xml.
     */
    public static final int HAT_BLACK = 0;
    public static final int HAT_GRAY = 1;
    public static final int HAT_CUSTOM = 2;

    /**
     * Get the current hat type
     * @return one of the hat type values HAT_BLACK, etc.
     */
    public int getHat() {
        return params.hat;
    }

    /**
     * Set the hat type
     * @param hat hat type value, HAT_BLACK, etc.
     */
    public void setHat(int hat) {
        params.hat = hat;
        hatBitmap = null;
        hatbandBitmap = null;

        switch(hat) {
            case HAT_BLACK:
                hatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hat_black);
                break;

            case HAT_GRAY:
                hatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hat_gray);
                break;

            case HAT_CUSTOM:
                hatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hat_white);
                hatbandBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hat_white_band);
                break;
        }

        invalidate();
    }

    /**
     * Image drawing scale
     */
    private float imageScale = 1;

    /**
     * Image left margin in pixels
     */
    private float marginLeft = 0;

    /**
     * Image top margin in pixels
     */
    private float marginTop = 0;

    /**
     * The image bitmap. None initially.
     */
    private Bitmap imageBitmap = null;

    /**
     * The current parameters
     */
    private Parameters params = new Parameters();

    /**
     * Set an image uri
     * @param imageUri uri to image file
     */
    public void setImageUri(Uri imageUri) {
        params.imageUri = imageUri;
        if (imageUri != null)
        {
            try {
                InputStream input = getContext().getContentResolver().openInputStream(imageUri);
                //
                //  Get the camera orientation to rotate the picture to the correct orientation
                //
                ExifInterface exif = new ExifInterface(input);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                int angle = 0;

                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    angle = 90;
                }
                else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    angle = 180;
                }
                else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    angle = 270;
                }

                Matrix mat = new Matrix();
                mat.postRotate(angle);
                //
                //  Close and reopen the image to get its bitmap
                //
                input.close();
                input = getContext().getContentResolver().openInputStream(imageUri);
                Bitmap bmp = BitmapFactory.decodeStream(input, null, null);
                //
                //  Apply the rotation matrix to correct the orientation
                //
                //noinspection UnnecessaryLocalVariable
                Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                //
                //  Set the bitmap and invalidate the view
                //
                imageBitmap = correctBmp;
                invalidate();
            }
            catch (IOException e) {
                Log.w("TAG", "-- Error in setting image");
            }
            catch(OutOfMemoryError e) {
                Log.w("TAG", "-- OOM Error in setting image");
            }
        }

    }

    public HatterView(Context context) {
        super(context);
        init(null, 0);
    }

    public HatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HatterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @SuppressWarnings("unused")
    private void init(AttributeSet attrs, int defStyle) {
        setHat(HAT_BLACK);

        customPaint = new Paint();
        customPaint.setColorFilter(new LightingColorFilter(params.color, 0));

        loadFeatherBitmap();
    }

    /**
     * Handle a draw event
     * @param canvas canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If there is no image to draw, we do nothing
        if(imageBitmap == null) {
            return;
        }

        /*
         * Determine the margins and scale to draw the image
         * centered and scaled to maximum size on any display
         */
        // Get the canvas size
        float wid = canvas.getWidth();
        float hit = canvas.getHeight();

        // What would be the scale to draw the where it fits both
        // horizontally and vertically?
        float scaleH = wid / imageBitmap.getWidth();
        float scaleV = hit / imageBitmap.getHeight();

        // Use the lesser of the two
        imageScale = scaleH < scaleV ? scaleH : scaleV;

        // What is the scaled image size?
        float iWid = imageScale * imageBitmap.getWidth();
        float iHit = imageScale * imageBitmap.getHeight();

        // Determine the top and left margins to center
        marginLeft = (wid - iWid) / 2;
        marginTop = (hit - iHit) / 2;

        /*
         * Draw the image bitmap
         */
        canvas.save();
        canvas.translate(marginLeft,  marginTop);
        canvas.scale(imageScale, imageScale);
        canvas.drawBitmap(imageBitmap, 0, 0, null);



        /*
         * Draw the hat
         */
        canvas.translate(params.hatX,  params.hatY);
        canvas.scale(params.hatScale/imageScale, params.hatScale/imageScale);
        canvas.rotate(params.hatAngle);

//        canvas.drawBitmap(hatBitmap, 0, 0, null);

        if(params.hat == HAT_CUSTOM) {
            canvas.drawBitmap(hatBitmap, 0, 0, customPaint);
        } else {
            canvas.drawBitmap(hatBitmap, 0, 0, null);
        }

        if(params.drawthefeather) {
            // Android scaled images that it loads. The placement of the
            // feather is at 322, 22 on the original image when it was
            // 500 pixels wide. It will have to move based on how big
            // the hat image actually is.
            float factor = hatBitmap.getWidth() / 500.0f;
            canvas.drawBitmap(featherBitmap, 322 * factor, 22 * factor, null);
        }

        if(hatbandBitmap != null) {
            canvas.drawBitmap(hatbandBitmap, 0, 0, null);
        }

        canvas.restore();
    }

    /**
     * Handle a touch event
     * @param event The touch event
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int id = event.getPointerId(event.getActionIndex());

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touch1.id = id;
                touch2.id = -1;
                getPositions(event);
                touch1.copyToLast();
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if(touch1.id >= 0 && touch2.id < 0) {
                    touch2.id = id;
                    getPositions(event);
                    touch2.copyToLast();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touch1.id = -1;
                touch2.id = -1;
                invalidate();
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                if(id == touch2.id) {
                    touch2.id = -1;
                } else if(id == touch1.id) {
                    // Make what was touch2 now be touch1 by
                    // swapping the objects.
                    Touch t = touch1;
                    touch1 = touch2;
                    touch2 = t;
                    touch2.id = -1;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                getPositions(event);
                move();
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Get the current custom hat color
     * @return hat color integer value
     */
    public int getColor() {
        return params.color;
    }

    /**
     * Set the current custom hat color
     * @param color hat color integer value
     */
    public void setColor(int color) {
        params.color = color;

        // Create a new filter to tint the bitmap
        customPaint.setColorFilter(new LightingColorFilter(color, 0));
        invalidate();
    }

    private static class Parameters implements Serializable {

        /**
         * Boolean on drawing the feather on the hat
         */
        public boolean drawthefeather = false;

        /**
         * Path to the image file if one exists
         */
        public transient Uri imageUri = null;

        /**
         * The current hat type
         */
        public int hat = HAT_BLACK;

        /**
         * X location of hat relative to the image
         */
        public float hatX = 0;

        /**
         * Y location of hat relative to the image
         */
        public float hatY = 0;

        /**
         * Hat scale, also relative to the image
         */
        public float hatScale = 0.25f;

        /**
         * Hat rotation angle
         */
        public float hatAngle = 0;

        /**
         * Custom hat color
         */
        public int color = Color.CYAN;

    }

    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private static class Touch {
        /**
         * Touch id
         */
        public int id = -1;

        /**
         * Current x location
         */
        public float x = 0;

        /**
         * Current y location
         */
        public float y = 0;

        /**
         * Previous x location
         */
        public float lastX = 0;

        /**
         * Previous y location
         */
        public float lastY = 0;

        /**
         * Change in x value from previous
         */
        public float dX = 0;

        /**
         * Change in y value from previous
         */
        public float dY = 0;

        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x;
            lastY = y;
        }

        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX;
            dY = y - lastY;
        }
    }

    /**
     * Get the positions for the two touches and put them
     * into the appropriate touch objects.
     * @param event the motion event
     */
    private void getPositions(MotionEvent event) {
        for(int i=0;  i<event.getPointerCount();  i++) {

            // Get the pointer id
            int id = event.getPointerId(i);

            // Get coordinates
//            float x = event.getX(i);
//            float y = event.getY(i);
            // Convert to image coordinates
            float x = (event.getX(i) - marginLeft) / imageScale;
            float y = (event.getY(i) - marginTop) / imageScale;

            if(id == touch1.id) {
                touch1.copyToLast();
                touch1.x = x;
                touch1.y = y;
            } else if(id == touch2.id) {
                touch2.copyToLast();
                touch2.x = x;
                touch2.y = y;
            }
        }

        invalidate();
    }

    /**
     * Handle movement of the touches
     */
    private void move() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if(touch1.id < 0) {
            return;
        }

        if(touch1.id >= 0) {
            // At least one touch
            // We are moving
            touch1.computeDeltas();

            params.hatX += touch1.dX;
            params.hatY += touch1.dY;
        }

        if(touch2.id >= 0) {
            // Two touches


            /*
             * Rotation
             */
            float angle1 = angle(touch1.lastX, touch1.lastY, touch2.lastX, touch2.lastY);
            float angle2 = angle(touch1.x, touch1.y, touch2.x, touch2.y);
            float da = angle2 - angle1;
            rotate(da, touch1.x, touch1.y);

            /*
             * Scaling
             */

            // What would be the scale to draw the where it fits both
            // horizontally and vertically?

            float lastWid  = (touch1.lastX - touch2.lastX);
            float lastHit = (touch1.lastY - touch2.lastY);
            float lastDistance = (float) Math.sqrt( Math.pow(lastWid,2) + Math.pow(lastHit,2));

            float wid  = Math.abs(touch2.x - touch1.x);
            float hit = Math.abs(touch2.y - touch1.y);
            float distance = (float) Math.sqrt( Math.pow(wid,2) + Math.pow(hit,2));

            // Scale up
            if (lastDistance < distance)
            {
                float deltaDistance = distance - lastDistance;
                params.hatScale = (float) (params.hatScale + (deltaDistance * 0.01));
            }

            // Scale Down
            else
            {

                float deltaDistance = lastDistance - distance;
                params.hatScale = (float) (params.hatScale - (deltaDistance * 0.01));
            }



        }
    }

    /**
     * Rotate the image around the point x1, y1
     * @param dAngle Angle to rotate in degrees
     * @param x1 rotation point x
     * @param y1 rotation point y
     */
    public void rotate(float dAngle, float x1, float y1) {
        params.hatAngle += dAngle;

        // Compute the radians angle
        double rAngle = Math.toRadians(dAngle);
        float ca = (float) Math.cos(rAngle);
        float sa = (float) Math.sin(rAngle);
        float xp = (params.hatX - x1) * ca - (params.hatY - y1) * sa + x1;
        float yp = (params.hatX - x1) * sa + (params.hatY - y1) * ca + y1;

        params.hatX = xp;
        params.hatY = yp;
    }

    /**
     * Determine the angle for two touches
     * @param x1 Touch 1 x
     * @param y1 Touch 1 y
     * @param x2 Touch 2 x
     * @param y2 Touch 2 y
     * @return computed angle in degrees
     */
    private float angle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * Save the view state to a bundle
     * @param key key name to use in the bundle
     * @param bundle bundle to save to
     */
    public void putToBundle(String key, Bundle bundle) {
        bundle.putParcelable(imageKey,params.imageUri);
        bundle.putSerializable(key, params);
    }

    /**
     * Get the view state from a bundle
     * @param key key name to use in the bundle
     * @param bundle bundle to load from
     */
    public void getFromBundle(String key, Bundle bundle) {
        params = (Parameters)bundle.getSerializable(key);
        params.imageUri = bundle.getParcelable(imageKey);

        // Ensure the options are all set
        setColor(params.color);
        setImageUri(params.imageUri);
        setHat(params.hat);
        loadFeatherBitmap();

    }

    public void handleFeather()
    {
        params.drawthefeather = !params.drawthefeather;
        loadFeatherBitmap();
        invalidate();
    }

    public boolean getFeatherState()
    {
        return params.drawthefeather;
    }

    public void setFeatherState(boolean drawFeather)
    {
        params.drawthefeather = drawFeather;
    }

    public void loadFeatherBitmap()
    {
        if (params.drawthefeather)
        {
            featherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.feather);
        }

    }




}