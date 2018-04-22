package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

public class PetCursorAdapter extends CursorAdapter
{

    public PetCursorAdapter(Context context, Cursor c)
    {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        // Find views to populate
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView summaryView = (TextView) view.findViewById(R.id.summary);

        // Extract data from cursor
        String name = cursor.getString((cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME)));
        String breed = cursor.getString((cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED)));

        // If there is no pet breed in the entry, set the view to show "Unknown Breed"
        if(TextUtils.isEmpty(breed))
        {
            breed = context.getString(R.string.unknown_pet_breed);
        }

        // Populate the fields with the extracted data
        nameView.setText(name);
        summaryView.setText(breed);
    }
}
