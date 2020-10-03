package com.ambrella.notekeeper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.CursorLoader;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ambrella.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.ambrella.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.ambrella.notekeeper.NoteKeeperProviderContract.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import static com.ambrella.notekeeper.NoteKeeperProviderContract.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    Toolbar mToolbar;
    NavigationView mNavigationView;
    private RecyclerView mRecyclerItems;
    private RecyclerView.LayoutManager mNotesLayoutManager;
    private GridLayoutManager mCourseLayoutManager;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private boolean mNotesQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                startActivity(intent);
            }
        });

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mToolbar = findViewById(R.id.nav_toolbar);
        mDrawerLayout = findViewById(R.id.nav_drawer);

        setSupportActionBar(mToolbar);
        mNavigationView = findViewById(R.id.nav_viewer);
        mNavigationView.setNavigationItemSelectedListener(this);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mActionBarDrawerToggle.syncState();

        initializeDisplayContent();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES, null, this );
        //mNoteRecyclerAdapter.notifyDataSetChanged();
        //displayNotes();
    }

    private void loadNotes() {
        NoteKeeperOpenHelper helper = new NoteKeeperOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        final String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                NoteInfoEntry.getQName(NoteInfoEntry._ID)
            };

        String notesOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + ", " + NoteInfoEntry.COLUMN_NOTE_TITLE;

        String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                " ON " + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID) +
                " = " + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID);

        Cursor notesCursor = db.query(tableWithJoin,
                noteColumns,null, null, null,null, notesOrderBy);

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notesCursor);

    }

    private void loadCourses() {
        NoteKeeperOpenHelper helper = new NoteKeeperOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        final String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry._ID };
        String courseOrderBy = CourseInfoEntry.COLUMN_COURSE_ID;


        Cursor coursesCursor = db.query(CourseInfoEntry.TABLE_NAME,
                courseColumns,null, null, null,null, courseOrderBy);
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, coursesCursor);

    }
    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        mRecyclerItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new GridLayoutManager(this, 2);

        loadNotes();

        loadCourses();

        displayNotes();
    }



    private void displayCourses(){
        mRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }
    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_viewer);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_courses:
                displayCourses();
                break;
            case R.id.nav_notes:
                displayNotes();
                break;
            case R.id.nav_share:
                handleSelection("Don't you think you have shared enough");
                break;
            case R.id.nav_send:
                handleSelection("Send");
                break;
            default:
                handleSelection("Unknown Selection");
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen((GravityCompat.START))){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    private void handleSelection(String msg) {
        View view = findViewById(R.id.nav_toolbar);
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES){
            final String[] noteColumns = new String[]{
                    Notes._ID,
                    Notes.COLUMN_NOTE_TITLE,
                    Courses.COLUMN_COURSE_TITLE
                };

            final String notesOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, notesOrderBy);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES){
            mNoteRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }


}