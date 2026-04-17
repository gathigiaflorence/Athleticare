package com.example.athleticare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    Context context;
    List<Player> playerList;

    public PlayerAdapter(Context context, List<Player> playerList) {
        this.context = context;
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {

        Player player = playerList.get(position);

        holder.txtName.setText("Name: " + player.getName());
        holder.txtSchoolId.setText("ID: " + player.getSchoolId());

        String ageText = (player.getAge() != null) ? String.valueOf(player.getAge()) : "-";
        holder.txtAge.setText("Age: " + ageText);
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtSchoolId, txtAge;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtSchoolId = itemView.findViewById(R.id.txtSchoolId);
            txtAge = itemView.findViewById(R.id.txtAge);
        }
    }
}