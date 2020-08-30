package com.ambrella.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ambrella.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder>{
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int mCourseIdPos;
    private int mIdPos;

    public CourseRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(context);
        loadCoursePositions();
    }

    private void loadCoursePositions() {
        mCourseIdPos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mIdPos = mCursor.getColumnIndex(CourseInfoEntry._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mTvCourse.setText(mCursor.getString(mCourseIdPos));
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 :mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView mTvCourse;
        public int mCurrentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvCourse = (TextView) itemView.findViewById(R.id.tv_course);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Course Selected", Snackbar.LENGTH_LONG ).show();
                }
            });
        }
    }


}
