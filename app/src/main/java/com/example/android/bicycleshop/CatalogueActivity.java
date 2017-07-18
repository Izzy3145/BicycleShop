package com.example.android.bicycleshop;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.bicycleshop.data.BicycleContract;
import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

public class CatalogueActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    BicycleCursorAdapter mCursorAdapter;

    //identifier for the Bicycle Loader
    private static final int BICYCLE_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);

        //find the listView that will be populated with Bicycle data
        ListView bicycleListView = (ListView) findViewById(R.id.list);

        //set up the list view's empty view
        View emptyView = (View)findViewById(R.id.empty_view);
        bicycleListView.setEmptyView(emptyView);

        //set the list view to the adapter
        //there is no data until the loader has run so set cursor to null
        mCursorAdapter = new BicycleCursorAdapter(this, null);
        bicycleListView.setAdapter(mCursorAdapter);

        //start the loader
        getLoaderManager().initLoader(BICYCLE_LOADER, null, this);

        //set up floating action button for adding a new bicycle to the database
        // clicking the floating actoin button will open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogueActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //set on click listener to take the use to Edit Screen
        bicycleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(CatalogueActivity.this, EditorActivity.class);

                //create the specific Content URI for the item clicked on,
                //by appending the id (input to this method) onto the content URI
                Uri currentBicycleUri = ContentUris.withAppendedId(BicycleContract.CONTENT_URI, id);

                //we can set this uri as a data entry onto the intent
                intent.setData(currentBicycleUri);

                //and start that particular intent
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that includes the columns we want to display
        String [] projection = {
                BicycleEntry._ID,
                BicycleEntry.COLUMN_IMAGE,
                BicycleEntry.COLUMN_BIKE_MODEL,
                BicycleEntry.COLUMN_PRICE,
                BicycleEntry.COLUMN_QUANTITY
                };

        //loader to execute the Content Providers query
        return new CursorLoader(this, BicycleContract.CONTENT_URI, projection, null,
                                null, null);
        }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    //update the CursorAdapter with the new cursor
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    //delete all data in CursorAdapter when reset is called
        mCursorAdapter.swapCursor(null);
    }

    //TEMPORARY methods

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        //get Uri from drawable and convert URI to string
        Uri uri = Uri.parse("android.resource://com.example.android.bicycleshop/drawable/bicycle_shadow");
        String imageString = uri.toString();
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BicycleEntry.COLUMN_IMAGE, imageString);
        values.put(BicycleEntry.COLUMN_BIKE_MODEL, "Ribble R872");
        values.put(BicycleEntry.COLUMN_BIKE_TYPE, BicycleEntry.TYPE_HYBRID);
        values.put(BicycleEntry.COLUMN_PRICE, "820");
        values.put(BicycleEntry.COLUMN_QUANTITY, 5);
        values.put(BicycleEntry.COLUMN_SUPPLIER, "ribble@gmail.com");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(BicycleContract.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(BicycleContract.CONTENT_URI, null, null);
        Log.v("CatalogueActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.catalogue_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
