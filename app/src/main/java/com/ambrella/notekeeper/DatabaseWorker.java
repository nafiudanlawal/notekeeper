package com.ambrella.notekeeper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseWorker {
    private SQLiteDatabase mDatabase;

    public DatabaseWorker(SQLiteDatabase database) {
        mDatabase = database;
    }

    public void insertSampleCourses(){
        insertCourse("android_intents", "Android Programming with Intents");
        insertCourse("android_async", "Android Async Programming and Services");
        insertCourse("java_lang", "Java Fundamentals: The Java Language");
        insertCourse("java_core", "Java Fundamentals: The Core Platform");
    }



    public void insertSampleNotes(){
        insertNote("android_intents", "Dynamic intent resolution", "Wow, intents allow components to be resolves at runtime");
        insertNote("android_intents", "Delegating intents", "PendingIntents are powerful; they delegate much than just a component invocations");

        insertNote("android_async", "Service default threads", "Did you know that by default an Android Service will ie up the UI thread?");
        insertNote("android_async", "Long running operations", "Foreground Services can be tied to a notification icon");

        insertNote("java_lang", "Service default threads",
                "Did you know that by default an Android Service will tie up the UI thread?");
        insertNote("java_lang", "Long running operations",
                "Foreground Services can be tied to a notification icon");

        insertNote("java_core", "Parameters",
                "Leverage variable-length parameter lists");
        insertNote("java_core", "Anonymous classes",
                "Anonymous classes simplify implementing one-use types");
    }

    private void insertCourse(String courseId, String title) {
        ContentValues values = new ContentValues();
        values.put(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE, title);

        long newRowId = mDatabase.insert(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, null, values);
    }

    private void insertNote(String courseId, String title, String text) {
        ContentValues values = new ContentValues();
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE, title);
        values.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TEXT, text);

        long newRowId = mDatabase.insert(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, null, values);
    }
}
