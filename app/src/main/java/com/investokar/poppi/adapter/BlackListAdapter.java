package com.investokar.poppi.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.investokar.poppi.util.ToastWindow;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.investokar.poppi.activity.ProfileActivity;
import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.model.BlacklistItem;
import com.investokar.poppi.util.BlacklistItemInterface;
import com.investokar.poppi.util.CustomRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlackListAdapter extends BaseAdapter implements Constants {

	private FragmentActivity activity;
	private LayoutInflater inflater;
	private List<BlacklistItem> blackList;

    ToastWindow toastWindow = new ToastWindow();
    private BlacklistItemInterface responder;


	public BlackListAdapter(FragmentActivity activity, List<BlacklistItem> blackList, BlacklistItemInterface responder) {

		this.activity = activity;
		this.blackList = blackList;
        this.responder = responder;
	}

	@Override
	public int getCount() {

		return blackList.size();
	}

	@Override
	public Object getItem(int location) {

		return blackList.get(location);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	static class ViewHolder {

        public TextView mBlockedUserFullname;
        public TextView mBlockedTimeAgo;
        public TextView mBlockedReason;
        public TextView mBlockedAction;
		public CircularImageView mBlockedUser;
	        
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;

		if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

		if (convertView == null) {
			
			convertView = inflater.inflate(R.layout.blacklist_row, null);
			
			viewHolder = new ViewHolder();

            viewHolder.mBlockedUser = convertView.findViewById(R.id.blockedUser);
            viewHolder.mBlockedUserFullname = convertView.findViewById(R.id.blockedUserFullname);
			viewHolder.mBlockedReason = convertView.findViewById(R.id.blockedReason);
            viewHolder.mBlockedTimeAgo = convertView.findViewById(R.id.blockedTimeAgo);
            viewHolder.mBlockedAction = convertView.findViewById(R.id.blockedAction);

            convertView.setTag(viewHolder);

            viewHolder.mBlockedUser.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int getPosition = (Integer) v.getTag();

                    BlacklistItem item = blackList.get(getPosition);

                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra("profileId", item.getBlockedUserId());
                    activity.startActivity(intent);
                }
            });

            viewHolder.mBlockedUserFullname.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int getPosition = (Integer) v.getTag();

                    BlacklistItem item = blackList.get(getPosition);

                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra("profileId", item.getBlockedUserId());
                    activity.startActivity(intent);
                }
            });

		} else {
			
			viewHolder = (ViewHolder) convertView.getTag();
		}


        viewHolder.mBlockedTimeAgo.setTag(position);
        viewHolder.mBlockedReason.setTag(position);
        viewHolder.mBlockedUserFullname.setTag(position);
        viewHolder.mBlockedAction.setTag(position);
        viewHolder.mBlockedUser.setTag(position);
        viewHolder.mBlockedUser.setTag(position);
		
		final BlacklistItem item = blackList.get(position);
        String fullname = item.getBlockedUserFullname();
		    if (item.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			    fullname = fullname.substring(0, fullname.lastIndexOf(' '));
            }
        viewHolder.mBlockedUserFullname.setText(fullname);
        viewHolder.mBlockedUserFullname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);



		String imageUrl = item.getBlockedUserPhotoUrl();
		if (imageUrl == null || imageUrl.isEmpty()) {
			imageUrl = String.valueOf(R.drawable.profile_default_photo);
		}

              Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(viewHolder.mBlockedUser);

        viewHolder.mBlockedReason.setVisibility(View.GONE);

        viewHolder.mBlockedTimeAgo.setText(item.getTimeAgo());

        viewHolder.mBlockedAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int getPosition = (Integer) view.getTag();

                if (App.getInstance().isConnected()) {

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_REMOVE, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {

                                        if (!response.getBoolean("error")) {


                                        }

                                    } catch (JSONException e) {

                                        e.printStackTrace();

                                    } finally {

                                        responder.remove(getPosition);
                                        notifyDataSetChanged();
                                    }
                                }
                            }, error -> toastWindow.makeText(error.getMessage(), 2000)) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("accountId", Long.toString(App.getInstance().getId()));
                            params.put("accessToken", App.getInstance().getAccessToken());
                            params.put("profileId", Long.toString(item.getBlockedUserId()));
                            return params;
                        }
                    };

                    App.getInstance().addToRequestQueue(jsonReq);
                } else {
                    toastWindow.makeText(activity.getText(R.string.msg_network_error), 2000);
                }
            }
        });

		return convertView;
	}
}