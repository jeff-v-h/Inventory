package com.example.android.inventory.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    /** Once an input stream is obtained from an image uri, convert it to a byte array */
    public static byte[] convertImageInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
