package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API contract for the Inventory app
 */

public final class ItemContract {
    // To prevent someone from accidentally instantiating the contract class, give empty constructor
    private ItemContract() {}

    /** The "content authority" is the name for the entire content provider */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * The path appended to base content URI for looking at the items data.
     * content://com.example.android.inventory/items
     */
    public static final String PATH_ITEMS = "items";

    /**
     * Inner class that defines constant values for the items table in the inventory database.
     * Each entry in the table represents a single item type.
     */
    public static final class ItemEntry implements BaseColumns {

        /** The content URI to access the items data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // A MIME type is a standardised way for ContentProviders to define a data type by giving
        // it a unique name. It is used in getType() method in the (Item)ContentProvider class
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         * ie. vnd.android.cursor.dir/com.example.android.inventory/items
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         * ie. vnd.android.cursor.item/com.example.android.inventory/items
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;


        /** Name for database table of items */
        public static final String TABLE_NAME = "items";

        /** The unique ID number for each product (only for use in the database table)
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /** Image of the item */
        public static final String COLUMN_ITEM_IMAGE = "image";

        /** Name of the item */
        public static final String COLUMN_ITEM_NAME = "name";

        /** Supplier of the item */
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";

        /** Price of the item */
        public static final String COLUMN_ITEM_PRICE = "price";

        /** Quantity of the item */
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
    }
}

