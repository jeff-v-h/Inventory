package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.ItemContract.ItemEntry;

/**
 * {@link ItemCursorAdapter} is an adapter for a listView that uses a {@link Cursor} of item
 * data as its data source
 */

public class ItemCursorAdapter extends CursorAdapter {
    /**
     * Public constructor
     * @param context the context
     * @param c the cursor from which to get the data
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data set yet. Data is set via java functionally
     * @param context app context
     * @param cursor Cursor from which to get data
     * @param parent parent to which the new view is attached to
     * @return the newly created list view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = (TextView) view.findViewById(R.id.product_name);
        TextView supplierView = (TextView) view.findViewById(R.id.supplier);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);

        // Find index for each data column to be able to extract properties from cursor
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

        // Extract properties from cursor using the above column indexes
        String name = cursor.getString(nameColumnIndex);
        String supplier = cursor.getString(supplierColumnIndex);
        Integer price = cursor.getInt(priceColumnIndex);
        Integer quantity = cursor.getInt(quantityColumnIndex);
        byte[] imageByteArray = cursor.getBlob(imageColumnIndex);

        // If the columns which are allowed to be null (supplier & image) are empty/null,
        // then use some default text that states it is unknown, so the TextView isn't blank.
        if (TextUtils.isEmpty(supplier)) {
            supplier = context.getString(R.string.unknown_supplier);
        }

        // Images load asynchronously, which means when image loading completes at that time
        // listView adapter uses old imageView to hold and display image.
        Glide.with(context).load(imageByteArray).into(imageView);
        //Glide.with(context).load(imageByteArray).placeholder(R.drawable.placeholder_thumbnail).into(imageView);
        // TODO: 9/07/2017 fix way to use .placeholder for Glide
        // TODO: 9/07/2017 Images still change while scrolling
        /*
        if (imageByteArray != null) {
            Bitmap image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            imageView.setImageBitmap(image);
        } */

        // Populate the fields with extracted data (image view is done above)
        nameView.setText(name);
        supplierView.setText(supplier);
        priceView.setText(String.valueOf(price));
        quantityView.setText(String.valueOf(quantity));
    }
}
