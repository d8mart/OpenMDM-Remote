package com.openmdmremote.ui.registration.fragments;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.openmdmremote.R;
import com.openmdmremote.ui.registration.tourcards.Welcome;

public class WelcomeTourFragment extends Fragment {
    public static final String FRAGMENT_TAG = "welcomefragment";

    private ViewPager viewPager;
    private FragmentPagerAdapter adapter;
    private ImageView[] dots;
    private LinearLayout pagerIndicator;

    public WelcomeTourFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcometour, container, false);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) view.findViewById(R.id.tour_pager);
        adapter = new TourAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        pagerIndicator = (LinearLayout) view.findViewById(R.id.pager_indicator);
        setDots();

        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / +12);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        ((Button) view.findViewById(R.id.button_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ((Button) view.findViewById(R.id.button_signup)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSignUp();
            }
        });

        ((Button) view.findViewById(R.id.button_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLogIn();
            }
        });


        return view;
    }

    private void switchToSignUp() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SignUpFragment(), SignUpFragment.FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void switchToLogIn() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new LogInFragment(), LogInFragment.FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setDots() {

        int dotsCount = adapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_notselected));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            params.setMargins(4, 4, 4, 4);
            pagerIndicator.addView(dots[i], params);
        }
        selectDot(0);
    }

    private void selectDot(int position) {
        Resources res = getResources();
        for (int i = 0; i < adapter.getCount(); i++) {
            int drawableId = (i == position) ? (R.drawable.dot_selected) : (R.drawable.dot_notselected);
            Drawable drawable = res.getDrawable(drawableId);
            dots[i].setImageDrawable(drawable);
        }
    }

    public class TourAdapter extends FragmentPagerAdapter {
        private final int NUMS_OF_FRAGMENTS = 2;


        private final FragmentManager mFragmentManager;

        public TourAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mFragmentManager = fragmentManager;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Welcome();
                case 1:
                    return new Welcome();
                default:
                    return new Welcome();
            }
        }

        @Override
        public int getCount() {
            return NUMS_OF_FRAGMENTS;
        }

        @Override
        public float getPageWidth(int position) {
            return 0.87f;
        }

        /* workaround for fragments disappear problem */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            destroyItem(container, position, fragment);
            return super.instantiateItem(container, position);
        }
    }
}