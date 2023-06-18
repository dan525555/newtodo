package com.example.newtodo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Task> itemList;
    private List<Task> filteredItemList; // Lista po filtrowaniu

    public MyAdapter(List<Task> itemList) {
        this.itemList = itemList;
        this.filteredItemList = itemList; // Początkowo przypisujemy całą listę
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = filteredItemList.get(position); // Używamy przefiltrowanej listy
        holder.titleTextView.setText(task.getTitle());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        holder.startDateTextView.setText("rozpoczęcie: "+dateFormat.format(task.getCreationTime().getTime()));
        holder.endDateTextView.setText("zakończenie: "+dateFormat.format(task.getExecutionTime().getTime()));

        if (task.getAttachmentPath().isEmpty()) {
            holder.tagImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.tagImageView.setVisibility(View.VISIBLE);
        }

        if (!task.isNotificationEnabled()) {
            holder.notificationImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.notificationImageView.setVisibility(View.VISIBLE);
        }

        if (!task.isCompleted()) {
            holder.completedImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.completedImageView.setVisibility(View.VISIBLE);
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MoreDetails.class);
            intent.putExtra("taskId", task.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.single_task;
    }

    @Override
    public int getItemCount() {
        return filteredItemList.size(); // Zwracamy rozmiar przefiltrowanej listy
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(String query) {
        filteredItemList.clear();
        if (query.isEmpty()) {
            filteredItemList.addAll(itemList);
        } else {
            for (Task task : itemList) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredItemList.add(task);
                }
            }
        }
        Collections.sort(filteredItemList, (task1, task2) -> {
            long executionTime1 = task1.getExecutionTime().getTimeInMillis();
            long executionTime2 = task2.getExecutionTime().getTimeInMillis();
            return Long.compare(executionTime1, executionTime2);
        });

        notifyDataSetChanged();
    }
    public void setTaskList(List<Task> taskList) {
        this.itemList = taskList;
        filterList("");
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView startDateTextView;
        TextView endDateTextView;
        ImageView tagImageView;
        ImageView notificationImageView;
        ImageView completedImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            startDateTextView = itemView.findViewById(R.id.startDateTextView);
            endDateTextView = itemView.findViewById(R.id.endDateTextView);
            tagImageView = itemView.findViewById(R.id.tagImageView);
            notificationImageView = itemView.findViewById(R.id.notificationImageView);
            completedImageView = itemView.findViewById(R.id.completedImageView);


        }
    }





}
