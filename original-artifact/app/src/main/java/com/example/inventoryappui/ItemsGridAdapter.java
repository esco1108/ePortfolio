package com.example.inventoryappui; // <-- change to your package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemsGridAdapter extends RecyclerView.Adapter<ItemsGridAdapter.VH> {

    private List<ItemRepository.ItemRow> data;

    public ItemsGridAdapter(List<ItemRepository.ItemRow> data) {
        this.data = data;
    }

    public void setData(List<ItemRepository.ItemRow> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_cell, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemRepository.ItemRow row = data.get(position);
        holder.txtName.setText(row.name);
        holder.txtQty.setText("Qty: " + row.quantity);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtQty;

        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtQty = itemView.findViewById(R.id.txtQty);
        }
    }
}