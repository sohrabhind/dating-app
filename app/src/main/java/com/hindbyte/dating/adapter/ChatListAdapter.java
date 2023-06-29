package com.hindbyte.dating.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.hindbyte.dating.activity.PhotoViewActivity;
import com.hindbyte.dating.util.ToastWindow;
import com.pkmmte.view.CircularImageView;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.ChatItem;
import com.hindbyte.dating.view.ResizableImageView;
import com.squareup.picasso.Picasso;

import java.util.List;



public class ChatListAdapter extends BaseAdapter implements Constants {

	private FragmentActivity activity;
	private LayoutInflater inflater;
	private List<ChatItem> dialogList;

    ToastWindow toastWindow = new ToastWindow();

	public ChatListAdapter(FragmentActivity activity, List<ChatItem> dialogList) {

		this.activity = activity;
		this.dialogList = dialogList;
	}

	@Override
	public int getCount() {

		return dialogList.size();
	}

	@Override
	public Object getItem(int location) {

		return dialogList.get(location);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	static class ViewHolder {

        public ResizableImageView mLeft_Img;
        public ResizableImageView mRight_Img;

        public TextView mLeft_TimeAgo;
        public TextView mLeft_Message;
        public TextView mLeftReport;
		public CircularImageView mLeft_FromUser;

        public TextView mRight_TimeAgo;
        public TextView mRight_Message;
        public CircularImageView mRight_FromUser;

        public LinearLayout mLeftItem;
        public LinearLayout mRightItem;

        public ImageView mSeenIcon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;

		final ChatItem chatItem = dialogList.get(position);

		if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

		if (convertView == null) {

            convertView = inflater.inflate(R.layout.chat_left_item_list_row, null);
			
			viewHolder = new ViewHolder();

            viewHolder.mLeft_Img = convertView.findViewById(R.id.left_img);
            viewHolder.mRight_Img = convertView.findViewById(R.id.right_img);

            viewHolder.mLeftItem = convertView.findViewById(R.id.leftItem);
            viewHolder.mRightItem = convertView.findViewById(R.id.rightItem);

            viewHolder.mLeft_FromUser = convertView.findViewById(R.id.left_fromUser);
            viewHolder.mLeft_Message = convertView.findViewById(R.id.left_message);
			viewHolder.mLeft_TimeAgo = convertView.findViewById(R.id.left_timeAgo);
            viewHolder.mLeftReport = convertView.findViewById(R.id.left_report);

            viewHolder.mRight_FromUser = convertView.findViewById(R.id.right_fromUser);
            viewHolder.mRight_Message = convertView.findViewById(R.id.right_message);
            viewHolder.mRight_TimeAgo = convertView.findViewById(R.id.right_timeAgo);

            viewHolder.mSeenIcon = convertView.findViewById(R.id.seenIcon);

            convertView.setTag(viewHolder);

            viewHolder.mLeft_FromUser.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int getPosition = (Integer) v.getTag();

                    ChatItem chatItem = dialogList.get(getPosition);

                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra("profileId", chatItem.getFromUserId());
                    activity.startActivity(intent);
                }
            });

		} else {
			
			viewHolder = (ViewHolder) convertView.getTag();
		}


        viewHolder.mRight_Img.setTag(position);
        viewHolder.mLeft_Img.setTag(position);

        viewHolder.mLeft_TimeAgo.setTag(position);
        viewHolder.mLeft_Message.setTag(position);
        viewHolder.mLeft_FromUser.setTag(position);

        viewHolder.mRight_TimeAgo.setTag(position);
        viewHolder.mRight_Message.setTag(position);
        viewHolder.mRight_FromUser.setTag(position);

        viewHolder.mLeftItem.setTag(position);
        viewHolder.mRightItem.setTag(position);

        viewHolder.mSeenIcon.setTag(position);

        if (App.getInstance().getId() == chatItem.getFromUserId()) {

            viewHolder.mLeftItem.setVisibility(View.GONE);

            viewHolder.mRightItem.setVisibility(View.VISIBLE);

            if (chatItem.getFromUserPhotoUrl().length() > 0) {

                
            Picasso.get()
            .load(chatItem.getFromUserPhotoUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(viewHolder.mRight_FromUser);

            } else {

                viewHolder.mRight_FromUser.setImageResource(R.drawable.profile_default_photo);
            }

            if (chatItem.getImgUrl().length() != 0) {

                    viewHolder.mRight_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewHolder.mRight_Img.requestLayout();

                    viewHolder.mRight_Img.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(activity, PhotoViewActivity.class);
                            i.putExtra("imgUrl", chatItem.getImgUrl());
                            activity.startActivity(i);
                        }
                    });

                                    
            Picasso.get()
            .load(chatItem.getImgUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(viewHolder.mRight_Img);

                

                    viewHolder.mRight_Img.setVisibility(View.VISIBLE);

                } else {

                    viewHolder.mRight_Img.setVisibility(View.GONE);
                }

            viewHolder.mRight_FromUser.setVisibility(View.GONE);

            if (chatItem.getMessage().length() > 0) {

                viewHolder.mRight_Message.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mRight_Message.setVisibility(View.GONE);
            }

            viewHolder.mRight_Message.setText(chatItem.getMessage().replaceAll("<br>", "\n"));

            if (chatItem.getSeenAt() > 0) {

                viewHolder.mSeenIcon.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mSeenIcon.setVisibility(View.GONE);
            }

            viewHolder.mRight_TimeAgo.setText(chatItem.getTimeAgo());

            viewHolder.mRight_Message.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("msg", chatItem.getMessage().replaceAll("<br>", "\n"));
                    clipboard.setPrimaryClip(clip);

                    toastWindow.makeText(activity.getString(R.string.msg_copied_to_clipboard), 2000);

                    return false;
                }
            });

        } else {

            viewHolder.mRightItem.setVisibility(View.GONE);

            viewHolder.mLeftItem.setVisibility(View.VISIBLE);

            if (chatItem.getFromUserPhotoUrl().length() > 0) {

                                
            Picasso.get()
            .load(chatItem.getFromUserPhotoUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(viewHolder.mLeft_FromUser);

            } else {

                viewHolder.mLeft_FromUser.setImageResource(R.drawable.profile_default_photo);
            }

            if (chatItem.getImgUrl().length() != 0) {

                viewHolder.mRight_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewHolder.mRight_Img.requestLayout();

                    viewHolder.mLeft_Img.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(activity, PhotoViewActivity.class);
                            i.putExtra("imgUrl", chatItem.getImgUrl());
                            activity.startActivity(i);
                        }
                    });

                                                
            Picasso.get()
            .load(chatItem.getImgUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(viewHolder.mLeft_Img);


            viewHolder.mLeft_Img.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mLeft_Img.setVisibility(View.GONE);
            }

            if (chatItem.getMessage().length() > 0) {

                viewHolder.mLeft_Message.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mLeft_Message.setVisibility(View.GONE);
            }

            viewHolder.mLeft_Message.setText(chatItem.getMessage().replaceAll("<br>", "\n"));
            viewHolder.mLeft_TimeAgo.setText(chatItem.getTimeAgo());

            viewHolder.mLeft_Message.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("msg", chatItem.getMessage().replaceAll("<br>", "\n"));
                    clipboard.setPrimaryClip(clip);

                    toastWindow.makeText(activity.getString(R.string.msg_copied_to_clipboard), 2000);

                    return false;
                }
            });

            //

            viewHolder.mLeftReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            viewHolder.mLeftReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    report();
                }
            });
        }

		return convertView;
	}

    public void report() {

        String[] profile_report_categories = new String[] {

                activity.getText(R.string.label_profile_report_0).toString(),
                activity.getText(R.string.label_profile_report_1).toString(),
                activity.getText(R.string.label_profile_report_2).toString(),
                activity.getText(R.string.label_profile_report_3).toString(),

        };

        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(activity);
        alertDialog.setTitle(activity.getText(R.string.label_item_report_title));

        alertDialog.setSingleChoiceItems(profile_report_categories, 0, null);
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(activity.getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(activity.getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                androidx.appcompat.app.AlertDialog alert = (androidx.appcompat.app.AlertDialog) dialog;
                int reason = alert.getListView().getCheckedItemPosition();

                toastWindow.makeText(activity.getString(R.string.label_item_report_sent), 2000);
            }
        });

        alertDialog.show();
    }
}