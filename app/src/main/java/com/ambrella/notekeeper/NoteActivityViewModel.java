package com.ambrella.notekeeper;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {


    public static final String ORIGINAL_NOTE_COURSE_ID = "com.ambrella.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.ambrella.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.ambrella.notekeeper.ORIGINAL_NOTE_TEXT";

    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    public boolean mIsNewlyCreated = true;

    public String getOriginalNoteCourseId() {
        return mOriginalNoteCourseId;
    }

    public void setOriginalNoteCourseId(String originalNoteCourseId) {
        mOriginalNoteCourseId = originalNoteCourseId;
    }

    public String getOriginalNoteTitle() {
        return mOriginalNoteTitle;
    }

    public void setOriginalNoteTitle(String originalNoteTitle) {
        mOriginalNoteTitle = originalNoteTitle;
    }

    public String getOriginalNoteText() {
        return mOriginalNoteText;
    }

    public void setOriginalNoteText(String originalNoteText) {
        mOriginalNoteText = originalNoteText;
    }

    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    public void restoreState(Bundle inState){
        mOriginalNoteCourseId = inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
    }
}
