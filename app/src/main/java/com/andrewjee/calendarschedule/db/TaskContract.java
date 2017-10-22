package com.andrewjee.calendarschedule.db;

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "com.andrewjee.calendarschedule.db";
    public static final int DB_VERSION = 2;

    public class TaskEntry implements BaseColumns{
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";

        public static final String COL_DATE = "date";
    }
}
