package com.hindbyte.dating.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.dating.R;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.BalanceItem;
import com.hindbyte.dating.model.Chat;

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
        public TextView credits, time;
        public LinearLayout parent;
        public TextView message;

        public ViewHolder(View view) {
            super(view);
            credits = view.findViewById(R.id.credits);
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
        if (item.getPaymentAction() == PA_BUY_CREDITS || item.getPaymentAction() == PA_BUY_REGISTRATION_BONUS) {
            holder.credits.setText("+" + item.getCreditsCount() + " " + ctx.getString(R.string.label_credits));
        } else {
            holder.credits.setText("-" + item.getCreditsCount() + " " + ctx.getString(R.string.label_credits));
        }

        switch (item.getPaymentAction()) {
            case PA_BUY_CREDITS: {
                switch (item.getPaymentType()) {
                    case PT_CARD: {
                        holder.message.setText(ctx.getString(R.string.label_payments_credits_stripe));
                        break;
                    }
                    case PT_GOOGLE_PURCHASE: {
                        holder.message.setText(ctx.getString(R.string.label_payments_credits_android));
                        break;
                    }
                    case PT_APPLE_PURCHASE: {
                        holder.message.setText(ctx.getString(R.string.label_payments_credits_ios));
                        break;
                    }
                }
                break;
            }

            case PA_BUY_GIFT: {
                holder.message.setText(ctx.getString(R.string.label_payments_send_gift));
                break;
            }
            case PA_BUY_PRO_MODE: {
                holder.message.setText(ctx.getString(R.string.label_payments_pro_mode));
                break;
            }
            case PA_BUY_MESSAGE_PACKAGE: {
                holder.message.setText(ctx.getString(R.string.label_payments_message_package));
                break;
            }
            case PA_BUY_REGISTRATION_BONUS: {
                holder.message.setText(ctx.getString(R.string.label_payments_registration_bonus));
                break;
            }
        }
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