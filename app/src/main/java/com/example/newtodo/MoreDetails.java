package com.example.newtodo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;

public class MoreDetails extends AppCompatActivity {

    private TaskDatabaseHelper databaseHelper;
    private long taskId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_details);

        databaseHelper = new TaskDatabaseHelper(this);

        taskId = getIntent().getLongExtra("taskId", 1);

        Task task = databaseHelper.getTask(taskId);
        Button update = findViewById(R.id.buttonModify);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ModifyTask.class);
                intent.putExtra("taskId", taskId);
                v.getContext().startActivity(intent);
            }
        });

        Button delete = findViewById(R.id.deleteCancel);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = task.getAttachmentPath();
                databaseHelper.deleteTask(task);
                if (!path.isEmpty()) {
                    delFile(path);
                }
                Toast.makeText(getApplicationContext(), "Zadanie usunięto", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        setInformation();

    }


    @SuppressLint("SetTextI18n")
    public void setInformation() {
        taskId = getIntent().getLongExtra("taskId", 1);

        Task task = databaseHelper.getTask(taskId);
        TextView titleTextView = findViewById(R.id.textViewTaskTitle);
        TextView startDateTextView = findViewById(R.id.textViewDateStart);
        TextView endDateTextView = findViewById(R.id.textViewDateEnd);
        TextView descriptionTextView = findViewById(R.id.textViewTaskDescription);
        TextView categoryTextView = findViewById(R.id.textViewCategory);
        TextView notificationTextView = findViewById(R.id.textViewNotification);
        TextView statusTextView = findViewById(R.id.textViewStatus);
        TextView attachmentTextView = findViewById(R.id.textViewAttachment);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        titleTextView.setText("Tytuł zadania: " + task.getTitle());
        startDateTextView.setText("Data stworzenia: " + sdf.format(task.getCreationTime().getTime()));
        endDateTextView.setText("Data Zakończenia: " + sdf.format(task.getExecutionTime().getTime()));
        descriptionTextView.setText("Opis zadania: " + task.getDescription());
        categoryTextView.setText("Kategoria : " + task.getCategory());
        if (task.isNotificationEnabled())
            notificationTextView.setText("Powiadomienie: tak");
        else
            notificationTextView.setText("Powiadomienie: nie");
        if (task.isCompleted())
            statusTextView.setText("Status: Zakończone");
        else
            statusTextView.setText("Status: Niezakończone");
        if (task.getAttachmentPath().isEmpty())
            attachmentTextView.setText("Załacznik: brak");
        else
            attachmentTextView.setText("Załącznik: " + task.getAttachmentPath());


    }

    @Override
    protected void onResume() {
        super.onResume();
        setInformation();
    }

    private int delFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return 3;
        }
    }
}
