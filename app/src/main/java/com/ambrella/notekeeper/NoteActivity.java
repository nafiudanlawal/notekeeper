package com.ambrella.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final int NOTE_POSTION_NOT_SET = -1;
    private NoteInfo mNote;
    private EditText etTitle, etText;
    private Spinner mSpinner;
    private List<CourseInfo> mCourses;
    private int note_position, mNotePosition;
    private boolean mCancelling = false;
    private boolean mIsNewNote;
    private NoteActivityViewModel  mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.mIsNewlyCreated && savedInstanceState != null){
            mViewModel.restoreState(savedInstanceState);
        }

        mViewModel.mIsNewlyCreated = false;

        etTitle = findViewById(R.id.et_note_title);
        etText = findViewById(R.id.et_note_text);
        mSpinner = findViewById(R.id.spinner_courses);
        mCourses = DataManager.getInstance().getCourses();

        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCourses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalNoteValue();

        displayNote();
    }

    private void saveOriginalNoteValue() {
        if(mIsNewNote){
            return;
        }else {
            mViewModel.setOriginalNoteCourseId(mNote.getCourse().getCourseId());
            mViewModel.setOriginalNoteTitle(mNote.getTitle());
            mViewModel.setOriginalNoteText(mNote.getText());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCancelling){
            if(mIsNewNote){
                DataManager.getInstance().removeNote(note_position);
            } else {
                storePreviousNoteValues();
            }
        }else {
            saveNote();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(outState != null)
            mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        mNote.setCourse(DataManager.getInstance().getCourse(mViewModel.getOriginalNoteCourseId()));
        mNote.setTitle(mViewModel.getOriginalNoteTitle());
        mNote.setText(mViewModel.getOriginalNoteText());
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinner.getSelectedItem());
        mNote.setTitle(etTitle.getText().toString());
        mNote.setText(etText.getText().toString());

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
            mNotePosition = intent.getIntExtra("NOTE_POSITION", NOTE_POSTION_NOT_SET);
            if(mNotePosition != NOTE_POSTION_NOT_SET){
                mIsNewNote = false;
                mNote = DataManager.getInstance().getNotes().get(mNotePosition);
            }else{
                mIsNewNote = true;
                createNewNote();
            }
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        note_position = dm.createNewNote();
        mNote = dm.getNotes().get(note_position);

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
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        }
        else if(id == R.id.action_cancel){
            mCancelling = true;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMail() {
        CourseInfo course = (CourseInfo) mSpinner.getSelectedItem();
        String title = etTitle.getText().toString();
        String text = "Check my Test Email I leant from pluralsight: " +
                        course.getTitle() + "\n\n " + etText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}