package com.innerCat.pillBox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.Refill;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.factories.OnOffsetChangedListenerFactory;
import com.innerCat.pillBox.recyclerViews.RefillAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.Database;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RefillActivity extends AppCompatActivity {

    Database database;
    RecyclerView rvRefills;
    RefillAdapter adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refill_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListenerFactory.create(this));

        //initialise the database
        database = DatabaseFactory.create(this);

        //get the recyclerview in activity layout
        rvRefills = findViewById(R.id.rvRefills);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("itemId", -1);

        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            //NB: This is the new thread in which the database stuff happens
            //today rvItem
            database.getDao().deleteRefillsOlderThanToday(Converters.todayString());
            List<Refill> refills = database.getDao().getRefillsOfItemId(itemId);
            Collections.sort(refills);

            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new RefillAdapter(refills);
                // Attach the adapter to the recyclerview to populate items
                rvRefills.setAdapter(adapter);
                // Set layout manager to position the items
                rvRefills.setLayoutManager(new LinearLayoutManager(this));
            });
        });
    }

    /**
     * Software back button.
     *
     * @param view the view
     */
    public void backButton( View view ) {
        finish();
    }


    /**
     * When the hardware/software back button is pressed
     */
    @Override
    public void onBackPressed() {
        finish();
    }
}