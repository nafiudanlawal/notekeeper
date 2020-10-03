package com.ambrella.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.ambrella.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.ambrella.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.ambrella.notekeeper.NoteKeeperProviderContract.Courses;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int NOTE_ID_NOT_SET = -1;
    public static final String NOTE_POSITION = "NOTE_POSITION";
    public static final String NOTE_ID = "NOTE_ID";
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo mNote;
    private EditText etTitle, etText;
    private Spinner mSpinner;
    private int note_position, mNoteId;
    private boolean mCancelling = false;
    private boolean mIsNewNote;
    private NoteActivityViewModel  mViewModel;
    private NoteKeeperOpenHelper mOpenHelper;
    private Cursor mNoteCursor;
    private int mNoteTitleIndex;
    private int mNoteTextIndex;
    private int mNoteCourseIdIndex;
    private Intent mIntent;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mNotesQueryFinished;
    private boolean mCoursesQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpenHelper = new NoteKeeperOpenHelper(this);

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
        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,  null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[]{android.R.id.text1}, 0);

        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapterCourses);

        getLoaderManager().initLoader(LOADER_COURSES,null,this);

        readDisplayStateValues();

        if(!mIsNewNote)
            getLoaderManager().initLoader(LOADER_NOTES, null, this);


    }

    private void loadCourses() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor courseCursor = db.query( CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(courseCursor);
    }

    private void saveOriginalNoteValue() {
        if(mIsNewNote){
            return;
        }else {
            mViewModel.setOriginalNoteCourseId(mNoteCursor.getString(mNoteCourseIdIndex));
            mViewModel.setOriginalNoteTitle(mNoteCursor.getString(mNoteTitleIndex));
            mViewModel.setOriginalNoteText(mNoteCursor.getString(mNoteTextIndex));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCancelling){
            if(mIsNewNote){
                deleteNoteFromDatabase(mNoteId);
            }
        }else {
            saveNote();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void deleteNoteFromDatabase(int noteId) {
        final String selection = NoteInfoEntry._ID + " = ? ";
        final String [] selectionsArgs = {Integer.toString(noteId)};
         AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionsArgs);
                return null;
            };
        };

        task.execute();
        return;
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
        String course_id = selectedCourseId();
        String title = etTitle.getText().toString();
        String text = etText.getText().toString();

        saveNoteToDatabase(title, text, course_id);
    }

    private String selectedCourseId() {
        int selectedPos = mSpinner.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPos);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);

        return courseId;
    }

    private void saveNoteToDatabase(String title, String text, String course_id){
        final String selection = NoteInfoEntry._ID + " =  ?" ;
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, course_id);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, title);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, text);
        @SuppressWarnings("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                return null;
            }
        };

        task.execute();

    }
    private void displayNote() {
        if(mNoteCursor != null) {
            if(mNoteCursor.moveToFirst()) {
                String courseId = mNoteCursor.getString(mNoteCourseIdIndex);
                String noteTitle = mNoteCursor.getString(mNoteTitleIndex);
                String noteText = mNoteCursor.getString(mNoteTextIndex);

                int index = getIndexOfCourseId(courseId);
                mSpinner.setSelection(index);
                etTitle.setText(noteTitle);
                etText.setText(noteText);
            }
        }
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();
        while(more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if(courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        mIntent = getIntent();
        if(mIntent != null){
            mNoteId = mIntent.getIntExtra(NOTE_ID, NOTE_ID_NOT_SET);
            if(mNoteId != NOTE_ID_NOT_SET){
                mIsNewNote = false;
                //mNote = DataManager.getInstance().getNotes().get(mNoteId);
            }else{
                mIsNewNote = true;
                createNewNote();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void createNewNote() {
        final ContentValues values = new ContentValues();

        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");
         AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                return null;
            }
        };

         task.execute();

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
        switch (id){
            case R.id.action_send_mail:
                sendMail();
                break;

            case R.id.action_cancel:
                mCancelling = true;
                finish();
                break;

            case R.id.action_next:
                moveNext();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNodeIndex = DataManager.getInstance().getNotes().size() - 1;

        item.setEnabled(mNoteId < lastNodeIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();


        mNoteId++;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalNoteValue();

        displayNote();
        invalidateOptionsMenu();
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

    @Override
    protected void onDestroy() {
        mOpenHelper.close();
        if(mNoteCursor != null)
            mNoteCursor.close();
        super.onDestroy();
    }


    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
        Uri uri = Uri.parse("content://com.ambrella.notekeeper.provider");

        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();

                String noteId = Integer.toString(mNoteId);

                String selection = NoteInfoEntry._ID + " =  ?" ;
                String[] selectionArgs = {noteId};

                final String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT };
                return db.query(NoteInfoEntry.TABLE_NAME,
                        noteColumns,
                        selection, selectionArgs, null, null, null);
            }
        };
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if(id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);

        /*return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };
                return db.query( CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };*/
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        if(id == LOADER_NOTES){
            loadFinishedNotes(cursor);
        }else if(id == LOADER_COURSES){
            loadFinishedCourses(cursor);
        }
    }

    private void loadFinishedCourses(Cursor cursor) {
        mAdapterCourses.changeCursor(cursor);
        mCoursesQueryFinished = true;
        displayNoteWhenQueryIsFinished();
    }

    private void loadFinishedNotes(Cursor cursor) {
        mNoteCursor = cursor;
        mNoteTextIndex = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteTitleIndex = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteCourseIdIndex = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNotesQueryFinished = true;
        displayNoteWhenQueryIsFinished();

    }

    private void displayNoteWhenQueryIsFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished) {
            displayNote();
            saveOriginalNoteValue();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        if(id == LOADER_NOTES){
            if(mNoteCursor != null)
                mNoteCursor.close();
        }else if(id == LOADER_COURSES){
            mAdapterCourses.changeCursor(null);
        }

    }
}