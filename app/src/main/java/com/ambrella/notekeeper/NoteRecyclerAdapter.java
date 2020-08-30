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
import com.ambrella.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private  Cursor mCursor;
    private int mNoteTitlePos;
    private int mCoursePos;
    private int mIdPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(context);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if(mCursor == null)
            return;
        mCoursePos = mCursor.getColumnIndex( CourseInfoEntry.COLUMN_COURSE_TITLE );
        mNoteTitlePos = mCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mIdPos = mCursor.getColumnIndex( NoteInfoEntry._ID );
    }

    public void changeCursor(Cursor cursor){
        if(mCursor != null)
            mCursor.close();
        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String course;
        String title;
        int id;
        mCursor.moveToPosition(position);
        course = mCursor.getString( mCoursePos);
        title = mCursor.getString( mNoteTitlePos);
        id = mCursor.getInt(mIdPos);

        holder.mTvCourse.setText(course);
        holder.mTvTitle.setText(title);
        holder.mId = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView mTvCourse;
        public final TextView mTvTitle;
        public int mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvCourse = (TextView) itemView.findViewById(R.id.tv_course);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);

                    mContext.startActivity(intent);
                }
            });
        }
    }


}
