/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo()
    {

        // Define projection for database query
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // Perform query to get all the rows from the database for our Cursor
        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);

        // Get the TextView for the displayView
        TextView displayView = (TextView) findViewById(R.id.text_view_pet);

        try
        {
            displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
            displayView.append(PetEntry._ID + " - " +
                               PetEntry.COLUMN_PET_NAME + " - " +
                               PetEntry.COLUMN_PET_BREED + " - " +
                               PetEntry.COLUMN_PET_GENDER + " - " +
                               PetEntry.COLUMN_PET_WEIGHT + "\n");

            // Figure out the index in the of each column
            int idColumnIndex     = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex   = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex  = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // While there is still a row in for the cursor to move to
            while(cursor.moveToNext())
            {
                // Get the current entries data
                int currentID       = cursor.getInt(idColumnIndex);
                String currentName  = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender   = cursor.getInt(genderColumnIndex);
                int currentWeight   = cursor.getInt(weightColumnIndex);

                // Append it to the displayView
                displayView.append(currentID + " - " +
                                   currentName + " - " +
                                   currentBreed + " - " +
                                   currentGender + " - " +
                                   currentWeight + "\n");
            }

        } finally
        {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }



    /**
     * Inserts pet info into the database and updates display info
     */
    private void insertPet()
    {
        // Creating pet values for inserting
        ContentValues values = createEntry("Toto", "Terrier", PetEntry.GENDER_MALE, 7);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    /**
     * Creates entry for the database
     * @param name - Name of the pet
     * @param breed - Breed of the pet
     * @param gender - Gender (0 - Unknown, 1 - Male, 2 - Female)
     * @param weight - Weight (in kg) of the pet
     * @return ContentValue for inserting into the database
     */
    private ContentValues createEntry(String name, String breed, int gender, int weight)
    {
        // Creating pet
        ContentValues petValues = new ContentValues();
        petValues.put(PetEntry.COLUMN_PET_NAME, name);
        petValues.put(PetEntry.COLUMN_PET_BREED, breed);
        petValues.put(PetEntry.COLUMN_PET_GENDER, gender);
        petValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        return petValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Insert pet
                insertPet();

                // Update the display info
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
