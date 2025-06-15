package com.example.namedmemories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.namedmemories.R;

import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private final List<String> personList;
    private final OnPersonClickListener listener;

    public interface OnPersonClickListener {
        void onPersonClick(String person);
    }

    public PersonAdapter(List<String> personList, OnPersonClickListener listener) {
        this.personList = personList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView personName;

        public ViewHolder(View view) {
            super(view);
            personName = view.findViewById(R.id.personName);
        }
    }

    @Override
    public PersonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String person = personList.get(position);
        holder.personName.setText(person);
        holder.itemView.setOnClickListener(v -> listener.onPersonClick(person));
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }
}
