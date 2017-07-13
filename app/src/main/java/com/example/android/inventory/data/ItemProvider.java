package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.data.ItemContract.ItemEntry;

/**
 * Content Provider for Inventory App. It uses CRUD operations to use the database as needed.
 * Ie. to query, update or delete data from the database
 */

public class ItemProvider extends ContentProvider {

    public static final String LOG = ItemProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the pets table */
    private static final int ITEMS = 100;
    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int ITEMS_ID = 101;

    private ItemDbHelper mDbHelper;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEMS_ID);
    }

    /** Initialise database helper object to gain access to items database */
    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor; // This cursor will hold the result of the query

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // query ITEMS table with given projection, selection, selection args and sort order.
                // result cursor may contain several rows
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEMS_ID:
                // Extracting out exact ID given
                selection = ItemEntry._ID + "=?";
                // ContentUris.parseId(Uri) will convert the last path segment of the Uri  to a long
                // .valueOf() will then convert the number to a string
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // Then query the items table for the specific Id using these selection and
                // selectionArgs
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // Some values cannot be null. Check if they are valid
        String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        Integer price = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        byte[] imageByteArray = values.getAsByteArray(ItemEntry.COLUMN_ITEM_IMAGE);
        if (name == null) {
            Toast.makeText(getContext(), "Item requires a name", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Item requires a name");
        }
        if (price == null || price < 0) {
            Toast.makeText(getContext(), "Item requires valid price", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Item requires valid price");
        }
        if (quantity == null || quantity < 0) {
            Toast.makeText(getContext(), "Item requires valid quantity", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Item requires valid quantity");
        }
        // If no image is provided, since image column cannot be null, provide placeholder image
        if (imageByteArray == null) {
            Bitmap placeholderBitmap =  BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.placeholder_thumbnail);
            imageByteArray = Utils.convertBitmapToByteArray(placeholderBitmap);
            values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageByteArray);
        }

        // Insert the item into database with the value given
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then insertion failed. Log error and return null
        if (id == -1) {
            Log.e(LOG, "Failed to insert row for " + uri);
            return null;
        }
        // Always notify a change in the database to the client
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new URI with the ID appended
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection Args
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEMS_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEMS_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // First need to check which columns are being updated by using .containsKey() method
        // Then check if the ContentValues are suitable to change
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        if (values.containsKey(ItemEntry.COLUMN_ITEM_PRICE)) {
            Integer price = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }
        if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity");
            }
        }
        if (values.containsKey(ItemEntry.COLUMN_ITEM_IMAGE)) {
            byte[] imageByteArray = values.getAsByteArray(ItemEntry.COLUMN_ITEM_IMAGE);
            if (imageByteArray == null) {
                Bitmap placeholderBitmap =  BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.placeholder_thumbnail);
                imageByteArray = Utils.convertBitmapToByteArray(placeholderBitmap);
                values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageByteArray);
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Update the rows in the database with the provided content values
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        // notify change
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
