package com.qiscus.sdk.ui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.qiscus.sdk.ui.fragment.BaseFragment;

import java.util.List;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class BasePagerAdapter<Fragment extends BaseFragment> extends
        FragmentStatePagerAdapter {
    protected List<Fragment> fragments;
    protected List<String> titles;

    public BasePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public BasePagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public abstract Fragment getItem(int position);

    @Override
    public int getCount() {
        return fragments.size();
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.size() == fragments.size() ? titles.get(position) : super.getPageTitle(position);
    }
}
