package com.example.athleticare;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;
import java.util.Objects;

public class AppointmentDiffCallback extends DiffUtil.Callback {

    private final List<AppointmentModel> oldList;
    private final List<AppointmentModel> newList;

    public AppointmentDiffCallback(List<AppointmentModel> oldList, List<AppointmentModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldPos, int newPos) {

        AppointmentModel oldItem = oldList.get(oldPos);
        AppointmentModel newItem = newList.get(newPos);

       
        return Objects.equals(oldItem.getSchoolId(), newItem.getSchoolId()) &&
                Objects.equals(oldItem.getDate(), newItem.getDate()) &&
                Objects.equals(oldItem.getTime(), newItem.getTime());
    }

    @Override
    public boolean areContentsTheSame(int oldPos, int newPos) {

        AppointmentModel oldItem = oldList.get(oldPos);
        AppointmentModel newItem = newList.get(newPos);

        return Objects.equals(oldItem.getSchoolId(), newItem.getSchoolId()) &&
                Objects.equals(oldItem.getDate(), newItem.getDate()) &&
                Objects.equals(oldItem.getTime(), newItem.getTime()) &&
                Objects.equals(oldItem.getAppointmentType(), newItem.getAppointmentType()) &&
                Objects.equals(oldItem.getInjuryType(), newItem.getInjuryType()) &&
                Objects.equals(oldItem.getStatus(), newItem.getStatus());
    }
}
