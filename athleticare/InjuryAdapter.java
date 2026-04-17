package com.example.athleticare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InjuryAdapter extends RecyclerView.Adapter<InjuryAdapter.ViewHolder> {

    private List<InjuryModel> injuryList;

    public InjuryAdapter(List<InjuryModel> injuryList) {
        this.injuryList = injuryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_injury, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        InjuryModel injury = injuryList.get(position);

        holder.text.setText(
                "Name: " + injury.getName() + "\n" +
                        "ID: " + injury.getSchoolId() + "\n" +
                        "Sport: " + injury.getSport() + "\n" +
                        "Injury: " + injury.getInjuryType() + "\n" +
                        "Area: " + injury.getInjuryArea() + "\n" +
                        "Date: " + injury.getInjuryDate()
        );
    }

    @Override
    public int getItemCount() {
        return injuryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.textInjury);
        }
    }
}