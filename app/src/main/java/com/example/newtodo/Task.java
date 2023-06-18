package com.example.newtodo;

import android.graphics.Bitmap;
import java.util.Calendar;

public class Task {
    private long id;
    private String title;
    private String description;
    private Calendar creationTime;
    private Calendar executionTime;
    private boolean completed;
    private boolean notificationEnabled;
    private String category;
    private String attachmentPath;
    private Bitmap attachmentImage;

    public Task(long id, String title, String description, Calendar creationTime, Calendar executionTime, boolean completed,
                boolean notificationEnabled, String category, String attachmentPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creationTime = creationTime;
        this.executionTime = executionTime;
        this.completed = completed;
        this.notificationEnabled = notificationEnabled;
        this.category = category;
        this.attachmentPath = attachmentPath;
        this.attachmentImage=null;
    }
    public Task( String title, String description, Calendar creationTime, Calendar executionTime, boolean completed,
                boolean notificationEnabled, String category, String attachmentPath) {
          this.title = title;
        this.description = description;
        this.creationTime = creationTime;
        this.executionTime = executionTime;
        this.completed = completed;
        this.notificationEnabled = notificationEnabled;
        this.category = category;
        this.attachmentPath = attachmentPath;
        this.attachmentImage=null;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public Calendar getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Calendar executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public Bitmap getAttachmentImage() {
        return attachmentImage;
    }

    public void setAttachmentImage(Bitmap attachmentImage) {
        this.attachmentImage = attachmentImage;
    }
}
