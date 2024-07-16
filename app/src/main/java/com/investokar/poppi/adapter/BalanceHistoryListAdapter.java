package com.investokar.poppi.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investokar.poppi.R;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.model.BalanceItem;
import com.investokar.poppi.model.Chat;

import java.util.List;


public class BalanceHistoryListAdapter extends RecyclerView.Adapter<BalanceHistoryListAdapter.ViewHolder> implements Constants {

    private Context ctx;
    private List<BalanceItem> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, BalanceItem item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView level, time;
        public LinearLayout parent;
        public TextView message;

        public ViewHolder(View view) {
            super(view);
            level = view.findViewById(R.id.level);
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
            parent = view.findViewById(R.id.parent);
        }
    }

    public BalanceHistoryListAdapter(Context mContext, List<BalanceItem> items) {
        this.ctx = mContext;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_balance_history_list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final BalanceItem item = items.get(position);
        holder.level.setText(item.getLevelCount() + " " + ctx.getString(R.string.label_level));
        holder.message.setText(item.getAmount()+ " " + item.getCurrency());
        holder.time.setText(item.getDate());
    }

    public BalanceItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnClickListener {
        void onItemClick(View view, Chat item, int pos);
    }

}