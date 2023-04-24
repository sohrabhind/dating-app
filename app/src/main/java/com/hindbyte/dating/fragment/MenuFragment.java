package com.hindbyte.dating.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.balysv.materialripple.MaterialRippleLayout;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.BalanceActivity;
import com.hindbyte.dating.activity.FriendsActivity;
import com.hindbyte.dating.activity.LikedActivity;
import com.hindbyte.dating.activity.LikesActivity;
import com.hindbyte.dating.activity.PeopleNearbyActivity;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.activity.SearchActivity;
import com.hindbyte.dating.activity.SettingsActivity;
import com.hindbyte.dating.activity.UpgradesActivity;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.squareup.picasso.Picasso;


public class MenuFragment extends Fragment implements Constants {


    CircularImageView mProfilePhoto;
    ImageView mFriendsIcon, mProfileLevelIcon;
    private TextView mNavProfileFullname, mNavProfileSubhead;

    private MaterialRippleLayout mNavProfile, mNavSearch, mNavFriends, mNavLikes, mNavLiked, mNavUpgrades, mNavSettings;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        getActivity().setTitle(R.string.nav_menu);

        mNavProfile = rootView.findViewById(R.id.nav_profile);
        mNavProfileFullname = rootView.findViewById(R.id.nav_profile_fullname);
        mNavProfileSubhead = rootView.findViewById(R.id.nav_profile_subhead);

        mNavSearch = rootView.findViewById(R.id.nav_search);

        mNavFriends = rootView.findViewById(R.id.nav_friends);
        mNavLikes = rootView.findViewById(R.id.nav_likes);
        mNavLiked = rootView.findViewById(R.id.nav_liked);
        mNavUpgrades = rootView.findViewById(R.id.nav_upgrades);
        mNavSettings = rootView.findViewById(R.id.nav_settings);

        // Counters

        mFriendsIcon = rootView.findViewById(R.id.friendsIcon);

        //

        mProfilePhoto = rootView.findViewById(R.id.profilePhoto);
        mProfileLevelIcon = rootView.findViewById(R.id.profileLevelIcon);

        mNavProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), ProfileActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                getActivity().startActivity(i);
            }
        });

        mNavSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        mNavFriends.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), FriendsActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavLikes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), LikesActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavLiked.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), LikedActivity.class);
                i.putExtra("itemId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavUpgrades.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), BalanceActivity.class);
                startActivity(i);
            }
        });

        mNavSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
            }
        });

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateView() {

        // Counters

        mFriendsIcon.setVisibility(View.GONE);

        if (App.getInstance().getNewFriendsCount() != 0) {

            mFriendsIcon.setVisibility(View.VISIBLE);
        }

        // Photo

        if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {
            Picasso.get()
            .load(App.getInstance().getPhotoUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(mProfilePhoto);
        } else {
            mProfilePhoto.setImageResource(R.drawable.profile_default_photo);
        }

        // Level

        switch (App.getInstance().getLevelMode()) {
            case 1:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_silver);
                break;
            case 2:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_gold);
                break;
            case 3:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_diamond);
                break;
            default:
                mProfileLevelIcon.setVisibility(View.GONE);
                break;
        }

        // Fullname

        mNavProfileFullname.setText(App.getInstance().getFullname());
        mNavProfileSubhead.setText("@" + App.getInstance().getUsername());
    }

    @Override
    public void onResume() {

        super.onResume();

        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_filters);
        item.setVisible(false);

        item = menu.findItem(R.id.action_remove_all);
        item.setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}