package com.example.namedmemories.adapters;

import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.namedmemories.R;
import com.example.namedmemories.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public EventAdapter(List<Event> eventList, OnEventActionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, timeView;
        ImageButton editBtn, deleteBtn;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.eventTitle);
            timeView = itemView.findViewById(R.id.eventTime);
            editBtn = itemView.findViewById(R.id.btnEdit);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleView.setText(event.title);
        holder.timeView.setText(event.time);

        holder.editBtn.setOnClickListener(v -> listener.onEdit(event));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
