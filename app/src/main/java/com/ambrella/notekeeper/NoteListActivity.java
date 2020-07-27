package com.ambrella.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                startActivity(intent);
            }
        });

        initializeDisplayContent();
    }

    private void initializeDisplayContent() {
        final ListView listView = (ListView) findViewById(R.id.list_notes);

        List<NoteInfo> courses = DataManager.getInstance().getNotes();

        ArrayAdapter<NoteInfo>  adapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courses);

        listView.setAdapter(adapterNotes);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                NoteInfo note = (NoteInfo) listView.getItemAtPosition(position);

                intent.putExtra("NOTE_INFO", note);

                startActivity(intent);
            }
        });
    }
}