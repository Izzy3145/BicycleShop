package com.example.android.bicycleshop.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

import org.xml.sax.ContentHandler;

import static android.R.attr.name;
import static android.R.attr.type;
import static com.example.android.bicycleshop.R.id.model;
import static com.example.android.bicycleshop.R.id.supplier;


/**
 * Created by izzystannett on 09/07/2017.
 */

public class BicycleProvider extends ContentProvider {

    //set up log tag
    public static final String LOG_TAG = BicycleProvider.class.getSimpleName();

    //set up URI matcher to filter out any jibberish URIs

    //matcher code for whole bicycles table
    private static final int BICYCLES = 100;
    //matcher code for single row of bicycles table
    private static final int BICYCLE_ID = 101;

    //create URIMatcher object with a default -1 code
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //static initializer which is run the first time anything is called from this class
    //the addURI method creates pairs of Content URIs and codes to return when idenfied
    static {
        sUriMatcher.addURI(BicycleContract.CONTENT_AUTHORITY, BicycleContract.PATH_BICYCLES, BICYCLES);
        sUriMatcher.addURI(BicycleContract.CONTENT_AUTHORITY, BicycleContract.PATH_BICYCLES + "/#", BICYCLE_ID);

    }

    //create the database by creating an object of BicycleDbHelper
    private BicycleDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BicycleDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Get readable database so we can query it
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //the cursor that will hold the result of the query
        Cursor cursor;

        //use the UriMatcher's match method to return the code of the uri
        int match = sUriMatcher.match(uri);

        //return an appropriate result, in the form of the cursor, depending on the uri code
        switch (match) {
            case BICYCLES:
                //for BICYCLES code, return queried data for all rows in the database
                cursor = database.query(BicycleContract.BicycleEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case BICYCLE_ID:
                //for an ID code, extract only the row requested
                //the selection will give "_id=?"
                //the selectionArgs will extract the actual ID
                selection = BicycleEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(BicycleEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification uri on the cursor
        //so if the data within the content uri is changed, we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BICYCLES:
                return BicycleEntry.CONTENT_LIST_TYPE;
            case BICYCLE_ID:
                return BicycleEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //get content URI code and only act on it if the whole table is stated
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BICYCLES:
                return insertBicycle(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);
        }
    }

    private Uri insertBicycle(Uri uri, ContentValues values) {
        //check for null values, and throw error if null
        String model = values.getAsString(BicycleEntry.COLUMN_BIKE_MODEL);
        if (model == null) {
            throw new IllegalArgumentException("Model needs specifying");
        }

        Integer type = values.getAsInteger(BicycleEntry.COLUMN_BIKE_TYPE);
        if (type == null || !BicycleEntry.isValidType(type)) {
            throw new IllegalArgumentException("Valid bicycle type needs specifying");
        }

        Integer quantity = values.getAsInteger(BicycleEntry.COLUMN_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity needs specified");
        }

        String price = values.getAsString(BicycleEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Price needs specifying");
        }

        String supplier = values.getAsString(BicycleEntry.COLUMN_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier needs specifying");
        }

        //get writeable database to insert row into
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //insert new row with given ContentValues for the bicycle
        long id = database.insert(BicycleEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for: " + uri);
            return null;
        }

        //notify all listeners that the contents of this uri has changed
        getContext().getContentResolver().notifyChange(uri, null);

        // if insert has been successful, return the new content URI with unique ID at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //track the number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BICYCLES:
                rowsDeleted = database.delete(BicycleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BICYCLE_ID:
                selection = BicycleEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(BicycleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //use the UriMatcher's match method to return the code of the uri
        int match = sUriMatcher.match(uri);

        //return an appropriate result, in the form of the cursor, depending on the uri code
        switch (match) {
            case BICYCLES:
                //for BICYCLES code, return queried data for all rows in the database
                return updateBicycle(uri, values, selection, selectionArgs);
            case BICYCLE_ID:
                //for an ID code, extract only the row requested
                //the selection will give "_id=?"
                //the selectionArgs will extract the actual ID
                selection = BicycleEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBicycle(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update unknown URI " + uri);
        }
    }

    private int updateBicycle(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //check for null values, and throw error if null
        if(values.containsKey(BicycleEntry.COLUMN_BIKE_MODEL)) {
            String model = values.getAsString(BicycleEntry.COLUMN_BIKE_MODEL);
            if (model == null) {
                throw new IllegalArgumentException("Model needs specifying");
            }
        }

        if(values.containsKey(BicycleEntry.COLUMN_BIKE_TYPE)) {
            Integer type = values.getAsInteger(BicycleEntry.COLUMN_BIKE_TYPE);
            if (type == null || !BicycleEntry.isValidType(type)) {
                throw new IllegalArgumentException("Valid bicycle type needs specifying");
            }
        }

        if(values.containsKey(BicycleEntry.COLUMN_QUANTITY)){
            Integer quantity = values.getAsInteger(BicycleEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity needs specified");
            }
        }

        if(values.containsKey(BicycleEntry.COLUMN_PRICE)) {
            String price = values.getAsString(BicycleEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Price needs specifying");
            }
        }

        if(values.containsKey(BicycleEntry.COLUMN_SUPPLIER)) {
            String supplier = values.getAsString(BicycleEntry.COLUMN_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier needs specifying");
            }
        }

        //only proceed if values have been provided, otherwise return 0
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //perform update on selection and return number of rows updated
        int rowsUpdated = database.update(BicycleEntry.TABLE_NAME, values, selection, selectionArgs);

        //if some rows have been updated, activate the notifyChange to all listeners
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return the number of rows updated
        return rowsUpdated;
    }
}
