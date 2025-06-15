package com.example.namedmemories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.namedmemories.R;
import com.example.namedmemories.activities.AddMemoryActivity;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<AddMemoryActivity.Category> categoryList;
    private final OnCategoryClickListener listener;
    private String selectedCategoryId = "";

    public interface OnCategoryClickListener {
        void onCategoryClick(AddMemoryActivity.Category category);
    }

    public CategoryAdapter(List<AddMemoryActivity.Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public void setSelectedCategory(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;
        ImageView categoryIcon;
        View cardView;

        public ViewHolder(View view) {
            super(view);
            categoryText = view.findViewById(R.id.categoryText);
            categoryIcon = view.findViewById(R.id.categoryIcon);
            cardView = view.findViewById(R.id.categoryCard); // Assuming you use a CardView or container
        }
    }

    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AddMemoryActivity.Category category = categoryList.get(position);

        holder.categoryText.setText(category.getName());
        holder.categoryIcon.setImageResource(category.getIconRes());

        // Change background if selected
        if (category.getId().equals(selectedCategoryId)) {
            holder.cardView.setBackgroundResource(R.drawable.selected_category_background); // Use a selected state background
        } else {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), category.getColorRes()));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onCategoryClick(category);
            setSelectedCategory(category.getId());
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
