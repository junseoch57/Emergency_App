package com.example.theemergency_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PoliceStationAdapter extends RecyclerView.Adapter<PoliceStationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PoliceStation station);
    }

    private List<PoliceStation> policeStations;
    private OnItemClickListener listener;

    public PoliceStationAdapter(List<PoliceStation> policeStations, OnItemClickListener listener) {
        this.policeStations = policeStations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PoliceStationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_police_station, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PoliceStationAdapter.ViewHolder holder, int position) {
        PoliceStation station = policeStations.get(position);
        holder.bind(station, listener);
    }

    @Override
    public int getItemCount() {
        return policeStations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPoliceStationName);
            tvAddress = itemView.findViewById(R.id.tvPoliceStationAddress);
        }

        public void bind(final PoliceStation station, final OnItemClickListener listener) {
            tvName.setText(station.getName());
            tvAddress.setText(station.getAddress());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(station);
                }
            });
        }
    }
}
