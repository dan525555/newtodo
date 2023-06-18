package com.example.newtodo;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateTask extends AppCompatActivity {

    private Task task;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText endDateEditText;
    private Spinner categorySpinner;
    private CheckBox notificationCheckBox;
    private CheckBox completedCheckBox;
    private Button attachmentButton;
    private Button clearButton;
    private TextView attachmentTextView;
    private Button cancelButton;
    private Button createButton;
    private ActivityResultLauncher<Intent> attachmentLauncher;

    private TaskDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);

        databaseHelper = new TaskDatabaseHelper(this);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        notificationCheckBox = findViewById(R.id.notificationCheckBox);
        completedCheckBox = findViewById(R.id.completedCheckBox);
        attachmentButton = findViewById(R.id.attachmentButton);
        clearButton = findViewById(R.id.clearButton);
        attachmentTextView = findViewById(R.id.attachmentTextView);
        cancelButton = findViewById(R.id.cancelButton);
        createButton = findViewById(R.id.createButton);
        attachmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    onActivityResult(result.getResultCode(), result.getData());
                });
        attachmentButton.setOnClickListener(v -> onAttachmentButtonClick());
        clearButton.setOnClickListener(v -> onClearButtonClick());
        cancelButton.setOnClickListener(v -> onCancelButtonClick());
        createButton.setOnClickListener(v -> onCreateButtonClick());

    }

    public void onAttachmentButtonClick() {
        // Create an intent to pick a file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        attachmentLauncher.launch(intent);
    }

    private void onActivityResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri attachmentUri = data.getData();

            attachmentTextView.setText(attachmentUri.toString());
        } else {
            attachmentTextView.setText("");
        }
    }

    public void onClearButtonClick() {
        attachmentTextView.setText("");
    }

    public void onCancelButtonClick() {
        finish();
        super.onBackPressed();
    }

    public void onCreateButtonClick() {
        createTask();
    }


    private void createTask() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String endDateString = endDateEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        boolean notificationEnabled = notificationCheckBox.isChecked();
        boolean completed = completedCheckBox.isChecked();
        String attachmentPath = attachmentTextView.getText().toString();

        if (validate()) {
            Calendar currentDateTime = Calendar.getInstance();
            Calendar endDate = parseEndDate(endDateString);
            String path = copyAttachmentToAppStorage(attachmentPath, currentDateTime, title);
            if ((path.isEmpty()&& attachmentPath.isEmpty())||!path.isEmpty()) {
                task = new Task(title, description, currentDateTime, endDate, completed, notificationEnabled, category, path);
                long val = databaseHelper.addTask(task);

                if (notificationEnabled) {

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("taskId", val);
                    setResult(RESULT_OK, resultIntent);

                }

                Toast.makeText(this, "Zadanie dodane", Toast.LENGTH_SHORT).show();
                finish();
            } else {

                Toast.makeText(this, "Błąd zapisu pliku", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private Calendar parseEndDate(String endDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date endDate = sdf.parse(endDateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            return calendar;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean validate() {
        String title = titleEditText.getText().toString();
        if (title.isEmpty()) {
            titleEditText.setError("Pole nie może być puste");
            return false;
        }

        String description = descriptionEditText.getText().toString();
        if (description.isEmpty()) {
            descriptionEditText.setError("Pole nie może być puste");
            return false;
        }

        String endDateString = endDateEditText.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date endDate = sdf.parse(endDateString);
            Date currentDate = new Date();
            if (endDate.before(currentDate)) {
                endDateEditText.setError("Data nie może być wcześniejsza niż obecna data");
                return false;
            }
        } catch (java.text.ParseException e) {
            endDateEditText.setError("Nieprawidłowy format daty");
            return false;
        }

        String attachmentPath = attachmentTextView.getText().toString();
        if (!attachmentPath.isEmpty()) {
            if (!isValidAttachmentPath(attachmentPath)) {
                attachmentTextView.setError("Nieprawidłowa ścieżka załącznika");
                return false;
            }
        }

        return true;
    }

    private boolean isValidAttachmentPath(String attachmentPath) {
        try {
            Uri uri = Uri.parse(attachmentPath);
            ContentResolver contentResolver = getContentResolver();
            if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                File file = new File(uri.getPath());
                return file.exists();
            } else {
                InputStream inputStream = contentResolver.openInputStream(uri);
                return inputStream != null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String copyAttachmentToAppStorage(String attachmentPath, Calendar today, String title) {
        String extension = getExtensionFromUri(Uri.parse(attachmentPath));
        String filename = "attachment_" + today.getTimeInMillis() + "_" + title + extension;
        File outputFile = new File(getFilesDir(), filename);

        try (InputStream inputStream = getContentResolver().openInputStream(Uri.parse(attachmentPath));
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getExtensionFromUri(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        if (extension == null) {
            String path = uri.getPath();
            if (path != null) {
                int extensionStart = path.lastIndexOf(".");
                if (extensionStart != -1 && extensionStart < path.length() - 1) {
                    extension = path.substring(extensionStart + 1);
                }
            }
        }
        return (extension != null) ? "." + extension : "";
    }


}