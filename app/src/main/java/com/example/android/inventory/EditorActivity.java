package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ItemContract.ItemEntry;
import com.example.android.inventory.data.Utils;

import java.io.InputStream;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    private static final String LOG = EditorActivity.class.getSimpleName();

    /** EditText fields where the user enters the product's name, supplier, price, quantity & image */
    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private Button mImageButton;
    private ImageView mImageView;

    /** request code for app to identify the image picked via use of intent */
    public static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read from user input
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mImageButton = (Button) findViewById(R.id.edit_image_button);
        mImageView = (ImageView) findViewById(R.id.edit_image_view);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                // Once image selected, it is passed onto onActivityResult() to be handled
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request code we're responding to and Make sure the request was successful
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            // openInputStream() needs FileNotFoundException handled, therefore in try/catch block
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mImageView.setImageBitmap(selectedImage);
                imageStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.image_select_unsuccessful, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.image_not_picked, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database and then exit activity
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                // showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                /* if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                } */

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                /* DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true; */
        }
        return super.onOptionsItemSelected(item);
    }

    /** Method to gather data from user input and then save to the database, which will
     * automatically be shown in InventoryActivity's listView */
    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        BitmapDrawable imageDrawable = (BitmapDrawable) mImageView.getDrawable();

        // price is NOT NULL in database. price needs data
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Item requires valid price", Toast.LENGTH_SHORT).show();
            return;
        }
        int priceAsInt = Integer.parseInt(priceString);

        // If the quantity is not provided by the user, don't try to parse into an integer value.
        // Use 0 by default, otherwise a blank quantityAsInt will be parsed and cause app crash
        int quantityAsInt = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantityAsInt = Integer.parseInt(quantityString);
        }

        // if an image is selected, convert it to a byte array and also store into content values
        byte[] imageByteArray = null;
        if (imageDrawable != null) {
            Log.i(LOG, "Image drawable is NOT null");
            Bitmap selectedImage = imageDrawable.getBitmap();
            imageByteArray = Utils.convertBitmapToByteArray(selectedImage);
        } else {
            Log.i(LOG, "Image drawable is null");
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceAsInt);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityAsInt);
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageByteArray);

        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Toast.makeText(this, R.string.image_insert_successful, Toast.LENGTH_SHORT).show();
    }



    /*
    // Temporary method to test retrieving image from database and setting into the imageView
    private void getImageFromDb() {
        // Access database and query to get a cursor back for the wanted image
        SQLiteDatabase database = mItemDbHelper.getReadableDatabase();
        String[] columnsWanted = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_IMAGE
        };
        // selection is the row we want ie. the row with name "Temporary testing data"
        // SQLite statement: SELECT _id, name, image FROM items WHERE name LIKE 'Temporary testing data'
        String selection = ItemEntry.COLUMN_ITEM_NAME + " LIKE " + "'" + "Temporary testing data" + "'";
        Cursor cursor = database.query(ItemEntry.TABLE_NAME, columnsWanted, selection, null,
                null, null, null);

        // Retrieve the byte array (blob) data from the cursor
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            byte[] imageByteArray = cursor.getBlob(imageColumnIndex);
            Bitmap image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(image);
        } else {
            Toast.makeText(getApplicationContext(), R.string.image_query_unsuccessful,
                    Toast.LENGTH_SHORT).show();
        }
        mItemDbHelper.close();
        cursor.close();
    } */
}
