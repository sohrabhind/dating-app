package com.investokar.poppi.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.investokar.poppi.app.App;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.investokar.poppi.activity.ProfileActivity;
import com.investokar.poppi.R;
import com.investokar.poppi.model.Chat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;



public class DialogsListAdapter extends RecyclerView.Adapter<DialogsListAdapter.ViewHolder> {

    private Context ctx;
    private List<Chat> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Chat item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subtitle, count, time;
        public CircularImageView image, online;
        public ImageView profileLevelIcon;
        public LinearLayout parent;
        public TextView message;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            subtitle = view.findViewById(R.id.subtitle);
            message = view.findViewById(R.id.message);
            count = view.findViewById(R.id.count);
            time = view.findViewById(R.id.time);
            image = view.findViewById(R.id.image);
            parent = view.findViewById(R.id.parent);

            online = view.findViewById(R.id.online);
            profileLevelIcon = view.findViewById(R.id.profileLevelIcon);
        }
    }

    public DialogsListAdapter(Context mContext, List<Chat> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final Chat item = items.get(position);
        holder.online.setVisibility(View.GONE);

        switch (item.getLevelMode()) {
            case 1:
                holder.profileLevelIcon.setVisibility(View.VISIBLE);
                holder.profileLevelIcon.setImageResource(R.drawable.level_silver);
                break;
            case 2:
                holder.profileLevelIcon.setVisibility(View.VISIBLE);
                holder.profileLevelIcon.setImageResource(R.drawable.level_gold);
                break;
            case 3:
                holder.profileLevelIcon.setVisibility(View.VISIBLE);
                holder.profileLevelIcon.setImageResource(R.drawable.level_diamond);
                break;
            default:
                holder.profileLevelIcon.setVisibility(View.GONE);
                holder.profileLevelIcon.setImageResource(R.color.transparent);
                break;
        }

        String imageUrl = item.getWithUserPhotoUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = String.valueOf(R.drawable.profile_default_photo);
        }

        final ImageView img = holder.image;
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.profile_default_photo)
                .error(R.drawable.profile_default_photo)
                .into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        img.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        img.setImageResource(R.drawable.profile_default_photo);
                        img.setVisibility(View.VISIBLE);
                    }
                });

        if (item.getId() == App.getInstance().getId()) {
            holder.title.setText(item.getWithUserFullname());
        } else {
            String fullname = item.getWithUserFullname();
            if(fullname.split("\\w+").length>1){
                fullname = fullname.substring(0, fullname.lastIndexOf(' '));
            }
            holder.title.setText(fullname);
        }
        holder.subtitle.setVisibility(View.GONE);

        if (!item.getLastMessage().isEmpty()) {

            holder.message.setText(item.getLastMessage().replaceAll("<br>", " "));

        } else {

            holder.message.setText(ctx.getString(R.string.label_last_message_image));
        }

        if (!item.getLastMessageAgo().isEmpty()) {

            holder.time.setText(item.getLastMessageAgo());

        } else {

            holder.time.setText("");
        }

        if (item.getNewMessagesCount() == 0) {
            holder.count.setVisibility(View.GONE);
            holder.count.setText(String.valueOf(item.getNewMessagesCount()));
        } else {
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(String.valueOf(item.getNewMessagesCount()));
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, items.get(position), position);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Chat chat = items.get(position);
                Intent intent = new Intent(ctx, ProfileActivity.class);
                intent.putExtra("profileId", chat.getWithUserId());
                ctx.startActivity(intent);
            }
        });
    }

    public Chat getItem(int position) {

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