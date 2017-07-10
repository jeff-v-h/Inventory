package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.ItemContract.ItemEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** tag for log messages */
    public static final String LOG = InventoryActivity.class.getSimpleName();

    private static final int ITEM_LOADER = 0;

    private ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView itemListView = (ListView) findViewById(R.id.list);
        // Setup the adapter with a null cursor to begin with. If there is data in the database,
        // the CursorLoader will swap in with cursor containing the data
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                // NOTE: id starts at 1 similar to _ID column in database table
                // Form content URI that represents specific item that was clicked on
                // i.e "content://com.example.android.inventory/items/#" where # = id of item
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                // add URI data for the URI to be sent with the intent.
                // Will be accessed with .getData() in the EditorActivity
                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        // Initialise the LoaderManager
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_inventory.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                getLoaderManager().initLoader(ITEM_LOADER, null, this);
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Pop up confirmation dialog for deleting all pets
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertDummyData() {
        // Placeholder data until content provider and then cursorAdapters are setup properly
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Theraband");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, "DJO Global");
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 5);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 10);
        // insert this placeholder data into the database
        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder. Set the message and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button
                int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
                if (rowsDeleted > 0) {
                    Toast.makeText(InventoryActivity.this, getString(R.string.inventory_deleted_all),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Cancel" button, so dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE };
        // Takes action based on the ID of the loader that's being created
        switch (id) {
            case ITEM_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
            default:
                Log.i(LOG, "An invalid id was passed in for CursorLoader");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
