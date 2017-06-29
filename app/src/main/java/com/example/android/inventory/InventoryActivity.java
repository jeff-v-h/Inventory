package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.ItemContract.ItemEntry;
import com.example.android.inventory.data.ItemDbHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class InventoryActivity extends AppCompatActivity {

    /** tag for log messages */
    public static final String LOG = InventoryActivity.class.getSimpleName();

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

        enableStrictMode();

        Drawable image = loadImageFromWeb("http://ep.yimg.com/ay/yhst-51964671464679/latex-free-exercise-bands-150-feet-6.gif");
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageDrawable(image);
        // TODO: SQLite database setup. Next steps are: 1. to setup storage of images into the database
        // TODO: 2. Setup content provider to manage uri requests


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
                //getSupportLoaderManager().initLoader(PET_LOADER, null, this);
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Pop up confirmation dialog for deleting all pets
                // showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertDummyData() {
        ItemDbHelper itemDbHelper = new ItemDbHelper(this);
        // writable database is suitable for reading and writing into
        SQLiteDatabase database = itemDbHelper.getWritableDatabase();

        // Placeholder data until content provider and then cursoradapters are setup properly
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Theraband");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, "DJO Global");
        values.put(ItemEntry.COLUMN_ITEM_PRICE, "5");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, "10");
        // insert this placeholder data into the database
        long insertId = database.insert(ItemEntry.TABLE_NAME, null, values);

        // Projection of the columns form the database for the query to return
        String[] columnsWanted = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME };
        // selection is the id of the row we just inserted
        String selection = ItemEntry._ID + " = " + insertId;
        Cursor queryCursor = database.query(ItemEntry.TABLE_NAME, columnsWanted, selection,
                null, null, null, null);
        // Extract the name: find column index of "name" column then getString
        queryCursor.moveToFirst();
        String mItemName = "";
        if (queryCursor.moveToFirst()) {
            int nameColumnIndex = queryCursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            mItemName = queryCursor.getString(nameColumnIndex);
        }

        TextView textView = (TextView) findViewById(R.id.list);
        textView.setText("The item name is " + mItemName);
    }

    /** Method that takes in a url of an image to return a Drawable object of that image.
     * Permission to access internet asked for in AndroidManifest.xml file */
    public static Drawable loadImageFromWeb(String url) {
        try {
            InputStream inputStream = (InputStream) new URL(url).getContent();
            Drawable drawable = Drawable.createFromStream(inputStream, "src name");
            return drawable;
        } catch (MalformedURLException e) {
            Log.i(LOG, "MalformedURLException caught");
            return null;
        } catch (IOException e) {
            Log.i(LOG, "IOException caught");
            return null;
        }
    }

    // class to temporarily work around NetworkOnMainThread exception
    public void enableStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}
