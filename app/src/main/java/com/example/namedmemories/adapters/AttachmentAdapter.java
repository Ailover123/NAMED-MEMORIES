package com.example.namedmemories.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.namedmemories.R;
import com.example.namedmemories.activities.AddMemoryActivity;

import java.util.Collections;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private final List<Uri> attachments;

    public AttachmentAdapter(List<Uri> attachments) {
        this.attachments = attachments;
    }

    public AttachmentAdapter(List<AddMemoryActivity.Attachment> attachments, Object onAttachmentRemoved, List<Uri> attachments1) {
        this.attachments = attachments1;
    }

    public AttachmentAdapter(List<AddMemoryActivity.Attachment> attachments, Object onAttachmentRemoved) {
        this.attachments = Collections.emptyList();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;

        public ViewHolder(View view) {
            super(view);
            previewImage = view.findViewById(R.id.previewImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri uri = attachments.get(position);
        Glide.with(holder.previewImage.getContext())
                .load(uri)
                .into(holder.previewImage);
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }
}
