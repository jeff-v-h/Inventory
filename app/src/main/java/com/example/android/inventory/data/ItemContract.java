package com.example.android.inventory.data;

import android.provider.BaseColumns;

/**
 * API contract for the Inventory app
 */

public final class ItemContract {
    // To prevent someone from accidentally instantiating the contract class, give empty constructor
    private ItemContract() {}

    // content://com.example.android.inventory/items

    public static final class ItemEntry implements BaseColumns {
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

