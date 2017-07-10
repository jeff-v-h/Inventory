package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.ItemContract.ItemEntry;
import com.example.android.inventory.data.Utils;

import java.io.InputStream;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    /** To hold the uri for editing item when sent from InventoryActivity */
    private Uri mCurrentItemUri;

    private static final int EXISTING_ITEM_LOADER = 0;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get intent and uri if editor started by clicking on an item to update
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        // Set the title of EditorActivity depending on whether creating or updating item
        if (mCurrentItemUri == null) {
            setTitle(R.string.editor_title_create_new);
            // Invalidate options menu so "delete" is not an option when creating new item
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_title_edit_item);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

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

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

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

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
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
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
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

        // Insert or update to database depending on whether creating or updating item
        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            // Toast depending on whether insert successful
            if (newUri == null) {
                Toast.makeText(this, R.string.editor_item_insert_unsuccessful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_item_insert_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.editor_item_update_unsuccessful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_item_update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_SUPPLIER, ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY, ItemEntry.COLUMN_ITEM_IMAGE };

        switch (id) {
            case EXISTING_ITEM_LOADER:
                // Returns new CursorLoader
                return new CursorLoader( this, mCurrentItemUri, projection, null, null, null);
            default:
                // An invalid id was passed in
                Toast.makeText(this, "An invalid id was passed in", Toast.LENGTH_SHORT).show();
                Log.i(LOG, "An invalid id was passed in");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            Log.i(LOG, "Cursor is null or has under 1 row");
            return;
        }

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            // Obtain data from cursor
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            Integer price = cursor.getInt(priceColumnIndex);
            Integer quantity = cursor.getInt(quantityColumnIndex);
            byte[] imageByteArray = cursor.getBlob(imageColumnIndex);

            // Set data into respective editing fields
            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            Glide.with(getApplicationContext()).load(imageByteArray).into(mImageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mImageView.setImageDrawable(null);
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing item
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }
}
