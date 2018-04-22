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

import android.app.AlertDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetProvider;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{

    private CursorAdapter mPetCursorAdapter;
    private static final int PET_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find ListView to populate
        ListView petListView = (ListView) findViewById(R.id.list);

        // Find and set EmptyView
        View emptyView = (RelativeLayout) findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


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

        // Set on item click listener to listed pets
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // Create URI for clicked on Pet
                Uri uri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                // Create Intent for opening the Editor Activity
                Intent openEditPet = new Intent(CatalogActivity.this, EditorActivity.class);

                // Add the uri data to the Intent
                openEditPet.setData(uri);

                // Start the EditorActivity with the Intent
                startActivity(openEditPet);
            }
        });
            // Have it send intent with that pet's URI

        // Init the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);

        // Re-establish the PetCursorAdapter
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        petListView.setAdapter(mPetCursorAdapter);
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

    private void showDeleteAllConfirmationDialog()
    {
        // Create and AlertDialog.Builder and set the message and click listener
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_entries_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // User clicked the delete button
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                if(dialog != null)
                {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the Alert Dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllPets()
    {

        int numberOfPetsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

        if(numberOfPetsDeleted == 0)
        {
            // Unable to delete pets
            Toast.makeText(this, getString(R.string.catalog_delete_pets_failed),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            // Pets were deleted
            Toast.makeText(this, getString(R.string.catalog_delete_pets_successful),
                    Toast.LENGTH_SHORT).show();
        }
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
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Delete all the pets from the database
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        // Define projection for database query
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mPetCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mPetCursorAdapter.swapCursor(null);
    }
}
