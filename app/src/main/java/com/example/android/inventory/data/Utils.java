package com.example.android.inventory.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Utility class to help with processing images
 *
 * 2 ways to use images with SQLite. One is to store the image as a Binary Large Object (BLOB)
 * The second is to store the image in the phone internal or external storage and then
 * save the path to this file into the database.
 *
 * The current method will store the image as BLOB into the database:
 * We need to convert our image path to a bitmap then to bytes (byte array).
 *
 * Completed by following: http://www.coderzheaven.com/2012/12/23/store-image-android-sqlite-retrieve-it/
 */

public class Utils {
    /** Convert bitmap to byte array */
    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /** Convert the image (as byte array) into Bitmap */
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
