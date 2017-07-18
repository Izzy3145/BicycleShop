package com.example.android.bicycleshop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

/**
 * Created by izzystannett on 09/07/2017.
 */
//Database helper to help manage database creation and version management

public class BicycleDbHelper extends SQLiteOpenHelper {

    //name of database file
    private static final String DATABASE_NAME = "bikeshop.db";

    //database version, if schema changes then the version must change
    private static final int DATABASE_VERSION = 1;

    //definte the database helper constructor
    public BicycleDbHelper (Context context){
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //this method is called when the database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the bicyce table
        String SQL_CREATE_BICYCLES_TABLE =  "CREATE TABLE " + BicycleEntry.TABLE_NAME + " ("
                + BicycleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BicycleEntry.COLUMN_IMAGE + "TEXT, "
                + BicycleEntry.COLUMN_BIKE_MODEL + " TEXT NOT NULL, "
                + BicycleEntry.COLUMN_BIKE_TYPE + " INTEGER NOT NULL, "
                + BicycleEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + BicycleEntry.COLUMN_QUANTITY + " INTEGER, "
                + BicycleEntry.COLUMN_SUPPLIER + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BICYCLES_TABLE);

    }

    //upgrade method is called when database needs to be updated to new version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
