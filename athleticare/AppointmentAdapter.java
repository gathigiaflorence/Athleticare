package com.example.athleticare;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentModel> list = new ArrayList<>();

    // STATUS CHANGE
    public interface OnStatusChangeListener {
        void onStatusChange(AppointmentModel appointment, String newStatus);
    }

    // DELETE
    public interface OnDeleteListener {
        void onDelete(AppointmentModel appointment);
    }

    private OnStatusChangeListener statusListener;
    private OnDeleteListener deleteListener;

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusListener = listener;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void updateList(List<AppointmentModel> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        AppointmentModel a = list.get(pos);

        // 🔥 NAME instead of ID (your request)
        h.id.setText("Name: " + safe(a.getAthleteName()));

        h.injury.setText("Injury: " + safe(a.getInjuryType()));
        h.date.setText("Date: " + safe(a.getDate()));
        h.time.setText("Time: " + safe(a.getTime()));
        h.type.setText("Type: " + safe(a.getAppointmentType()));

        String status = a.getStatus() != null ? a.getStatus() : "PENDING";
        h.status.setText(status);

        // COLOR STATUS
        switch (status) {
            case "COMPLETED":
                h.status.setBackgroundColor(Color.parseColor("#16A34A"));
                break;
            case "MISSED":
                h.status.setBackgroundColor(Color.parseColor("#DC2626"));
                break;
            default:
                h.status.setBackgroundColor(Color.parseColor("#F59E0B"));
                break;
        }

        // STATUS BUTTONS
        h.btnPending.setOnClickListener(v -> {
            if (statusListener != null) {
                statusListener.onStatusChange(a, "PENDING");
            }
        });

        h.btnCompleted.setOnClickListener(v -> {
            if (statusListener != null) {
                statusListener.onStatusChange(a, "COMPLETED");
            }
        });

        h.btnMissed.setOnClickListener(v -> {
            if (statusListener != null) {
                statusListener.onStatusChange(a, "MISSED");
            }
        });

        // 🔥 DELETE BUTTON (new)
        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(a);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String safe(String v) {
        return v != null ? v : "-";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView id, injury, date, time, type, status;
        Button btnPending, btnCompleted, btnMissed, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            id = itemView.findViewById(R.id.textViewAthleteId);
            injury = itemView.findViewById(R.id.textViewInjuryType);
            date = itemView.findViewById(R.id.textViewDate);
            time = itemView.findViewById(R.id.textViewTime);
            type = itemView.findViewById(R.id.textViewType);
            status = itemView.findViewById(R.id.textViewStatus);

            btnPending = itemView.findViewById(R.id.buttonPending);
            btnCompleted = itemView.findViewById(R.id.buttonCompleted);
            btnMissed = itemView.findViewById(R.id.buttonMissed);

            btnDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}