package com.example.newtodo;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModifyTask extends AppCompatActivity {
    private static final int REQUEST_ATTACHMENT = 1;

    private TaskDatabaseHelper databaseHelper;
    private long taskId;
    private Task task;

    private EditText modifyTitleEditText;
    private EditText modifyDescriptionEditText;
    private EditText modifyEndDateEditText;
    private TextView modifyStartDateTextView;

    private Spinner modifyCategorySpinner;
    private CheckBox modifyNotificationCheckBox;
    private CheckBox modifyCompletedCheckBox;
    private ActivityResultLauncher<Intent> modifyAttachmentLauncher;

    private Button modifyAttachmentButton;
    private Button modifyClearButton;
    private TextView modifyAttachmentTextView;
    private Button modifyCancelButton;
    private Button modifyCreateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_task);

        databaseHelper = new TaskDatabaseHelper(this);

        modifyTitleEditText = findViewById(R.id.modifyTitleEditText);
        modifyDescriptionEditText = findViewById(R.id.modifyDescriptionEditText);
        modifyEndDateEditText = findViewById(R.id.modifyEndDateEditText);
        modifyStartDateTextView = findViewById(R.id.modifyStartDateTextView);

        modifyCategorySpinner = findViewById(R.id.modifyCategorySpinner);
        modifyNotificationCheckBox = findViewById(R.id.modifyNotificationCheckBox);
        modifyCompletedCheckBox = findViewById(R.id.modifyCompletedCheckBox);
        modifyAttachmentTextView = findViewById(R.id.modifyattachmentTextView);


        taskId = getIntent().getLongExtra("taskId", 1);
        task = databaseHelper.getTask(taskId);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        modifyStartDateTextView.setText(sdf.format(task.getCreationTime().getTime()));
        modifyTitleEditText.setText(task.getTitle());
        modifyDescriptionEditText.setText(task.getDescription());
        modifyEndDateEditText.setText(sdf.format(task.getExecutionTime().getTime()));
        // Set the selected item of the spinner based on the task's category
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.task_types, android.R.layout.simple_spinner_item);
        modifyCategorySpinner.setAdapter(adapter);
        if (task.getCategory() != null) {
            int categoryIndex = adapter.getPosition(task.getCategory());
            modifyCategorySpinner.setSelection(categoryIndex);
        }
        modifyNotificationCheckBox.setChecked(task.isNotificationEnabled());
        modifyCompletedCheckBox.setChecked(task.isCompleted());
        if (task.getAttachmentPath() != null)
            modifyAttachmentTextView.setText(task.getAttachmentPath());
        modifyAttachmentButton = findViewById(R.id.modifyattachmentButton);
        modifyClearButton = findViewById(R.id.modifyclearButton);
        modifyCancelButton = findViewById(R.id.modifycancelButton);
        modifyCreateButton = findViewById(R.id.modifycreateButton);
        modifyAttachmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    onActivityResult(result.getResultCode(), result.getData());
                });
        modifyAttachmentButton.setOnClickListener(v -> modifyOnattachmentButtonClick());
        modifyClearButton.setOnClickListener(v -> modifyOnClearButtonClick());
        modifyCancelButton.setOnClickListener(v -> modifyOnCancelButtonClick());
        modifyCreateButton.setOnClickListener(v -> modifyOnCreateButtonClick());

    }

    public void modifyOnattachmentButtonClick() {
        // Create an intent to pick a file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        modifyAttachmentLauncher.launch(intent);
    }

    private void onActivityResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri attachmentUri = data.getData();

            modifyAttachmentTextView.setText(attachmentUri.toString());
        } else {
            modifyAttachmentTextView.setText("");
        }
    }

    public void modifyOnClearButtonClick() {
        modifyAttachmentTextView.setText("");
    }

    public void modifyOnCancelButtonClick() {
        finish();
        super.onBackPressed();
    }

    public void modifyOnCreateButtonClick() {
        modifyTask();
    }

    private void modifyTask() {
        String title = modifyTitleEditText.getText().toString();
        String description = modifyDescriptionEditText.getText().toString();
        String endDateString = modifyEndDateEditText.getText().toString();
        String category = modifyCategorySpinner.getSelectedItem().toString();
        boolean notificationEnabled = modifyNotificationCheckBox.isChecked();
        boolean completed = modifyCompletedCheckBox.isChecked();
        String attachmentPath = modifyAttachmentTextView.getText().toString();

        if (validate()) {
            Calendar endDate = parseEndDate(endDateString);
            String path = task.getAttachmentPath();
            String newPath = copyAttachmentToAppStorage(attachmentPath, task.getCreationTime(), title);
            if ((newPath.isEmpty()&&attachmentPath.isEmpty())||!newPath.isEmpty()) {
                task.setTitle(title);
                task.setDescription(description);
                task.setExecutionTime(endDate);
                task.setCategory(category);
                task.setNotificationEnabled(notificationEnabled);
                task.setCompleted(completed);
                task.setAttachmentPath(newPath);
                if (!path.isEmpty()) {
                    delFile(path);
                }
                databaseHelper.updateTask(task);
                Toast.makeText(this, "Zadanie zaktualizowane", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Błąd zapisu pliku", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Calendar parseEndDate(String dateString) {
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

    private boolean validate() {
        String title = modifyTitleEditText.getText().toString();
        if (title.isEmpty()) {
            modifyTitleEditText.setError("Pole nie może być puste");
            return false;
        }

        String description = modifyDescriptionEditText.getText().toString();
        if (description.isEmpty()) {
            modifyDescriptionEditText.setError("Pole nie może być puste");
            return false;
        }

        String endDateString = modifyEndDateEditText.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date endDate = sdf.parse(endDateString);
            Date currentDate = new Date();
            if (endDate.before(currentDate)) {
                modifyEndDateEditText.setError("Data nie może być wcześniejsza niż obecna data");
                return false;
            }
        } catch (java.text.ParseException e) {
            modifyEndDateEditText.setError("Nieprawidłowy format daty");
            return false;
        }

        String attachmentPath = modifyAttachmentTextView.getText().toString();
        if (!attachmentPath.isEmpty()) {
            if (!isValidAttachmentPath(attachmentPath)) {
                modifyAttachmentTextView.setError("Nieprawidłowa ścieżka załącznika");
                return false;
            }
        }

        return true;
    }

    private boolean isValidAttachmentPath(String attachmentPath) {
        try {
            Uri uri = Uri.parse(attachmentPath);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return inputStream != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
