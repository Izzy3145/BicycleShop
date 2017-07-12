package com.example.android.bicycleshop;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bicycleshop.data.BicycleContract;
import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

import static android.R.attr.name;
import static android.R.attr.type;

/**
 * Created by izzystannett on 09/07/2017.
 */

public class BicycleCursorAdapter extends CursorAdapter {

    //set up constructor
    public BicycleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //make a blank list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //binds data to the list item view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView modelTextView = (TextView)view.findViewById(R.id.model_list_item);
        TextView priceTextView = (TextView)view.findViewById(R.id.price_list_item);
        TextView quantityTextView = (TextView)view.findViewById(R.id.quantity_list_item);

        //find the columns of the attributes that we want to display
        int modelColumnIndex = cursor.getColumnIndex(BicycleEntry.COLUMN_BIKE_MODEL);
        int priceColumnIndex = cursor.getColumnIndex(BicycleEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BicycleEntry.COLUMN_QUANTITY);

        //retrieve data from the appropriate columns
        String bicycleModel = cursor.getString(modelColumnIndex);
        String bicyclePrice = cursor.getString(priceColumnIndex);
        String bicycleQuantity = cursor.getString(quantityColumnIndex);

        //update the text views with the values from the database
        modelTextView.setText(bicycleModel);
        priceTextView.setText(bicyclePrice);
        quantityTextView.setText(bicycleQuantity);
    }
}
