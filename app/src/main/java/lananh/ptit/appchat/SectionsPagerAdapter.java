package lananh.ptit.appchat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFragment requestsFragment = new RequestFragment();
                return requestsFragment;
            case 1:
                ChatFragment chatsFragment = new ChatFragment();
                return chatsFragment;
            case 2:
                FriendFragment friendsFragment = new FriendFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0: return "Thông báo";
            case 1: return "Chat";
            case 2: return "Bạn bè";
            default: return null;
        }
    }
}

