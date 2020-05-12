package com.example.smsexcelapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<String> phoneNumbers, contents;
    private LayoutInflater inflater;

    Adapter(Context context, List<String> phoneNumbers, List<String> contents){
        this.phoneNumbers = phoneNumbers;
        this.contents = contents;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String phone_number = phoneNumbers.get(position);
        String content = contents.get(position);
        holder.phone_number.setText(phone_number);
        holder.content.setText(content);

    }

    @Override
    public int getItemCount() {
        return phoneNumbers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView phone_number, content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            phone_number = itemView.findViewById(R.id.txt_phone_number);
            content = itemView.findViewById(R.id.txt_content);
        }
    }
}
