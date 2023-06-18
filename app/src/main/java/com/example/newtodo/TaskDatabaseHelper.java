package com.example.newtodo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TaskDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "task_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CREATION_TIME = "creation_time";
    private static final String COLUMN_EXECUTION_TIME = "execution_time";
    private static final String COLUMN_COMPLETED = "completed";
    private static final String COLUMN_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_ATTACHMENT_PATH = "attachment_path";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_TASKS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_CREATION_TIME + " TEXT, " +
                COLUMN_EXECUTION_TIME + " TEXT, " +
                COLUMN_COMPLETED + " INTEGER, " +
                COLUMN_NOTIFICATION_ENABLED + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_ATTACHMENT_PATH + " TEXT" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                @SuppressLint("Range") String creationTimeString = cursor.getString(cursor.getColumnIndex(COLUMN_CREATION_TIME));
                Calendar creationTime = parseCalendar(creationTimeString);
                @SuppressLint("Range") String executionTimeString = cursor.getString(cursor.getColumnIndex(COLUMN_EXECUTION_TIME));
                Calendar executionTime = parseCalendar(executionTimeString);
                @SuppressLint("Range") boolean completed = cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)) == 1;
                @SuppressLint("Range") boolean notificationEnabled = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_ENABLED)) == 1;
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                @SuppressLint("Range") String attachmentPath = cursor.getString(cursor.getColumnIndex(COLUMN_ATTACHMENT_PATH));

                Task task = new Task(id, title, description, creationTime, executionTime, completed,
                        notificationEnabled, category, attachmentPath);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    private Calendar parseCalendar(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date endDate = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            return calendar;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_CREATION_TIME,dateFormat.format(task.getCreationTime().getTime())  );
        values.put(COLUMN_EXECUTION_TIME, dateFormat.format(task.getExecutionTime().getTime()) );
        values.put(COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_NOTIFICATION_ENABLED, task.isNotificationEnabled() ? 1 : 0);
        values.put(COLUMN_CATEGORY, task.getCategory());
        values.put(COLUMN_ATTACHMENT_PATH, task.getAttachmentPath());
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }



    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_CREATION_TIME,dateFormat.format(task.getCreationTime().getTime())  );
        values.put(COLUMN_EXECUTION_TIME, dateFormat.format(task.getExecutionTime().getTime()) );
        values.put(COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_NOTIFICATION_ENABLED, task.isNotificationEnabled() ? 1 : 0);
        values.put(COLUMN_CATEGORY, task.getCategory());
        values.put(COLUMN_ATTACHMENT_PATH, task.getAttachmentPath());

        long insertedRowId = db.insert(TABLE_TASKS, null, values);
        db.close();

        return insertedRowId;
    }

    public Task getTask(long id) {
        if(id==-1)
            return null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[]{
                        COLUMN_ID,
                        COLUMN_TITLE,
                        COLUMN_DESCRIPTION,
                        COLUMN_CREATION_TIME,
                        COLUMN_EXECUTION_TIME,
                        COLUMN_COMPLETED,
                        COLUMN_NOTIFICATION_ENABLED,
                        COLUMN_CATEGORY,
                        COLUMN_ATTACHMENT_PATH}, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        @SuppressLint("Range") long taskId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        @SuppressLint("Range")  String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        @SuppressLint("Range")   String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        @SuppressLint("Range")   String creationTimeString = cursor.getString(cursor.getColumnIndex(COLUMN_CREATION_TIME));
        @SuppressLint("Range")    Calendar creationTime = parseCalendar(creationTimeString);
        @SuppressLint("Range")    String executionTimeString = cursor.getString(cursor.getColumnIndex(COLUMN_EXECUTION_TIME));
        @SuppressLint("Range")   Calendar executionTime = parseCalendar(executionTimeString);
        @SuppressLint("Range")    boolean completed = cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)) == 1;
        @SuppressLint("Range")  boolean notificationEnabled = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_ENABLED)) == 1;
        @SuppressLint("Range")   String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
        @SuppressLint("Range")  String attachmentPath = cursor.getString(cursor.getColumnIndex(COLUMN_ATTACHMENT_PATH));

        Task task = new Task(taskId, title, description, creationTime, executionTime, completed,
                notificationEnabled, category, attachmentPath);

        cursor.close();
        db.close();

        return task;
    }
    public long findTaskId(Task task) {
        // Pobierz wszystkie zadania z bazy danych
        List<Task> tasks = getAllTasks();

        for (Task dbTask : tasks) {
            if (areTasksEqual(task, dbTask)) {
                return dbTask.getId();
            }
        }

        return -1;
    }

    private boolean areTasksEqual(Task task1, Task task2) {
        if (task1.getTitle().equals(task2.getTitle())
                && task1.getDescription().equals(task2.getDescription())
                && task1.getCreationTime().getTime() == task2.getCreationTime().getTime()
                && task1.getExecutionTime().getTime() == task2.getExecutionTime().getTime()
                && task1.isCompleted() == task2.isCompleted()
                && task1.isNotificationEnabled() == task2.isNotificationEnabled()
                && task1.getCategory().equals(task2.getCategory())
                && Objects.equals(task1.getAttachmentPath(), task2.getAttachmentPath())) {
            return true;
        }

        return false;
    }



}