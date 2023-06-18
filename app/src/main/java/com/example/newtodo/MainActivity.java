package com.example.newtodo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;



import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private List<Task> taskList;
    private EditText editTextFindTask;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private TaskDatabaseHelper db;
    private Boolean completedTask;
    private String category;
    private String times;

    private SharedPreferences prefs;

    private ActivityResultLauncher<Intent> createTaskLauncher;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        setContentView(R.layout.activity_main);

        db = new TaskDatabaseHelper(this);
        taskList = db.getAllTasks();
        createNotificationChannel();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        completedTask = prefs.getBoolean("completed_tasks", true);
        category = prefs.getString("task_types", "WSZYSTKO");
        times = prefs.getString("notification_minutes", "0");
        createTaskLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                long value = data.getLongExtra("taskId", -1);
                                if(value!=-1)
                                {


                                    Task t=db.getTask(value);
                                    setAlarm(t);


                                }
                             }

                        }
                    }
                });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateTask.class);
                createTaskLauncher.launch(intent);
            }
        });



        recyclerView = findViewById(R.id.recyclerViewsTasks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(taskList);
        recyclerView.setAdapter(adapter);

        editTextFindTask = findViewById(R.id.editTextFindTask);
        editTextFindTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                adapter.filterList(query);
            }
        });

        updateFilteredTaskList();
    }

    private void updateFilteredTaskList() {
        List<Task> filteredTaskList = filterTasks();
        adapter.setTaskList(filteredTaskList);
        adapter.notifyDataSetChanged();
    }

    private List<Task> filterTasks() {
        category = prefs.getString("task_types", "WSZYSTKO");
        completedTask = prefs.getBoolean("completed_tasks", true);

        List<Task> filteredList = new ArrayList<>();

        for (Task task : taskList) {
            if (!completedTask && task.isCompleted()) {
                continue;
            }

            if (!category.equals("WSZYSTKO") && !task.getCategory().equals(category)) {
                continue;
            }

            filteredList.add(task);
        }

        return filteredList;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAlarm(Task task) {
        times = prefs.getString("notification_minutes", "0");

        Calendar taskEndTime = task.getExecutionTime();
        Calendar alarmTime = Calendar.getInstance();
        long termin=taskEndTime.getTimeInMillis();
        alarmTime.setTimeInMillis(termin - (Integer.parseInt(times) * 60 * 1000));

        Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);
        intent.putExtra("task_title", task.getTitle());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "notify")
                .setContentTitle("Task Reminder")
                .setContentText(task.getTitle())
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(0, builder.build());

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
    }



    public void createNotificationChannel() {

        CharSequence name = "Test Channel";
        String description = "Channel for test notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel("notify", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskList = db.getAllTasks();
        updateFilteredTaskList();
    }


    }


