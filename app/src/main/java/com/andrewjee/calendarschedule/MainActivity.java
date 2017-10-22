package com.andrewjee.calendarschedule;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.andrewjee.calendarschedule.db.TaskContract;
import com.andrewjee.calendarschedule.db.TaskDbHelper;

import java.util.ArrayList;
import static com.andrewjee.calendarschedule.R.layout.item_todo;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button_go_cal;
    private TextView date;
    private ArrayAdapter<String> itemsAdapter;
    private TaskDbHelper mHelper;
    private ListView mTaskListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        date = (TextView) findViewById(R.id.date);
        button_go_cal = (Button) findViewById(R.id.button_go_cal);

        Intent incomingIntent = getIntent(); // gets intent from calendar activity
        String theDate = incomingIntent.getStringExtra("date");
        date.setText(theDate); // shows the selected date

        button_go_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                // basically whenever this button is pressed, you will go to calendar
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        mHelper = new TaskDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_DATE},
                null, null, null, null, null);
        while(cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            Log.d(TAG, "Task: " + cursor.getString(idx));
        }
        cursor.close();
        db.close();
        updateUI();
    }

    public boolean inside(String s){
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_DATE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            if (s.equals(cursor.getString(idx))){
                return true;}
        }
        return false;
    }

    public void onAddItem(View v){
        // what goes on when you press add item
        // this will add a new item basically
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        date = (TextView) findViewById(R.id.date);
        String itemText = etNewItem.getText().toString();
        String save = itemText;
        String dateText = date.getText().toString();
        if (!dateText.equals("")){
            int i = 1;
            while (inside(itemText)){
                itemText = save;
                String next = " (" + i +")";
                itemText = itemText + next;
                i++;
            }
            itemsAdapter.add(itemText);
            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, itemText);
            values.put(TaskContract.TaskEntry.COL_DATE, dateText);
            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
            updateUI();
            etNewItem.setText("");
        }
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        mTaskListView = (ListView) findViewById(R.id.lvItems);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        date = (TextView) findViewById(R.id.date);
        String dateText = date.getText().toString();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_DATE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int day = cursor.getColumnIndex(TaskContract.TaskEntry.COL_DATE);
            if (dateText.equals(cursor.getString(day))){
                taskList.add(cursor.getString(idx));}
        }

        if (itemsAdapter == null) {
            itemsAdapter = new ArrayAdapter<>(this,
                    item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(itemsAdapter);
        } else {
            itemsAdapter.clear();
            itemsAdapter.addAll(taskList);
            itemsAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        SQLiteDatabase db = mHelper.getWritableDatabase();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        date = (TextView) findViewById(R.id.date);
        String dateText = date.getText().toString();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_DATE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int idy = cursor.getColumnIndex(TaskContract.TaskEntry.COL_DATE);
            if (task.equals(cursor.getString(idx)) && dateText.equals(cursor.getString(idy))) {
                db.delete(TaskContract.TaskEntry.TABLE,
                        TaskContract.TaskEntry.COL_TASK_TITLE + " = ?", new String[]{cursor.getString(idx)});
            }
        }
        db.close();
        updateUI();
    }
}
