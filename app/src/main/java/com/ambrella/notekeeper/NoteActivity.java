package com.ambrella.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private NoteInfo mNote;
    private EditText etTitle, etText;
    private Spinner mSpinner;
    private List<CourseInfo> mCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etTitle = (EditText) findViewById(R.id.et_note_text);
        etText = (EditText) findViewById(R.id.et_note_text);
        mSpinner = (Spinner) findViewById(R.id.spinner_courses);
        mCourses = DataManager.getInstance().getCourses();

        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCourses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapterCourses);

        readDisplayStateValues();

        displayNote();
    }

    private void displayNote() {
        if(mNote != null){
            int index = mCourses.indexOf(mNote.getCourse());
            mSpinner.setSelection(index);
            etTitle.setText(mNote.getTitle());
            etText.setText(mNote.getText());
        }
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        if(intent != null){
            mNote = intent.getParcelableExtra("NOTE_INFO");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}