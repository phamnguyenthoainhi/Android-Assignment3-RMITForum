package android.rmit.assignment3;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerController extends FragmentPagerAdapter {
    int tabCounts;

    public PagerController(FragmentManager fm, int tabCounts) {
        super(fm, tabCounts);
        this.tabCounts = tabCounts;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SubscribedCourseUser();
            case 2:
                return new CreatedPosts();
            case 1:
                return new FollowedPost();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return tabCounts;
    }
}