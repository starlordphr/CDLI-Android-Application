package com.assolutions.altaf.cdlitablet;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ArrayList<ImageData> data=new ArrayList<>();
        data.add(new ImageData("Taj Mahal",R.drawable.tajmahal,getResources().getString(R.string.tajmahal)));
        data.add(new ImageData("Eiffel Tower",R.drawable.eiffeltower,getResources().getString(R.string.eiffeltower)));
        data.add(new ImageData("Tower of Pisa",R.drawable.pisatower,getResources().getString(R.string.pisatower)));
        data.add(new ImageData("Pyramids",R.drawable.pyramids,getResources().getString(R.string.pyramids)));
        data.add(new ImageData("The Great Wall of China",R.drawable.wallofchina,getResources().getString(R.string.wallofchina)));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),data);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the data for this
         * fragment.
         */
        private static final String DATA = "DATA";

        boolean isDescExpanded=false;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         *
         */
        public static PlaceholderFragment newInstance(ImageData data) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putSerializable(DATA, data);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ImageData thisImage= (ImageData) getArguments().getSerializable(DATA);

            TextView desc = (TextView) rootView.findViewById(R.id.desc_text);
            TextView title = (TextView) rootView.findViewById(R.id.desc_title);
            final ImageView imageView=(ImageView)rootView.findViewById(R.id.image_view);
            final LinearLayout descLayout=(LinearLayout)rootView.findViewById(R.id.desc_layout);

            desc.setText(thisImage.desc);
            title.setText(thisImage.name);
            imageView.setImageResource(thisImage.localResource);

            descLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isDescExpanded){
                        CollapseView(descLayout,imageView);
                    }
                    else{
                        ExpandView(descLayout,imageView);
                    }
                }
            });


            return rootView;
        }

        private void ExpandView(final View v, final View rootView) {

           ValueAnimator anim1 = ValueAnimator.ofInt(v.getMeasuredHeight(),rootView.getMeasuredHeight());
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.height = val;
                    v.setLayoutParams(layoutParams);
                }
            });
            anim1.setDuration(1*500);

            ValueAnimator anim2 = ValueAnimator.ofInt(v.getMeasuredWidth(),rootView.getMeasuredWidth());
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.width = val;
                    v.setLayoutParams(layoutParams);
                }
            });
            anim2.setDuration(1*500);

            //Run height and width animations together
            AnimatorSet animatorSet=new AnimatorSet();
            animatorSet.playTogether(anim1,anim2);
            animatorSet.start();


            isDescExpanded=true;
        }
        private void CollapseView(final View v, View rootView) {
            int width=(int) getResources().getDimension(R.dimen.desc_width);
            int height=(int) getResources().getDimension(R.dimen.desc_height);
            /*FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(width, height);
            params.gravity=Gravity.BOTTOM|Gravity.END;
            params.bottomMargin= (int) getResources().getDimension(R.dimen.bottom_margin);
            params.rightMargin=(int) getResources().getDimension(R.dimen.fab_margin);
            v.setLayoutParams(params);
            */
            ValueAnimator anim1 = ValueAnimator.ofInt(rootView.getMeasuredHeight(),height);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.height = val;
                    layoutParams.gravity=Gravity.BOTTOM|Gravity.END;
                    layoutParams.bottomMargin= (int) getResources().getDimension(R.dimen.bottom_margin);
                    layoutParams.rightMargin=(int) getResources().getDimension(R.dimen.fab_margin);
                    v.setLayoutParams(layoutParams);
                }
            });
            anim1.setDuration(1*500);

            ValueAnimator anim2 = ValueAnimator.ofInt(rootView.getMeasuredWidth(),width);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.width = val;
                    layoutParams.gravity=Gravity.BOTTOM|Gravity.END;
                    layoutParams.bottomMargin= (int) getResources().getDimension(R.dimen.bottom_margin);
                    layoutParams.rightMargin=(int) getResources().getDimension(R.dimen.fab_margin);
                    v.setLayoutParams(layoutParams);
                }
            });
            anim2.setDuration(1*500);

            //Run height and width animations together
            AnimatorSet animatorSet=new AnimatorSet();
            animatorSet.playTogether(anim1,anim2);
            animatorSet.start();



            isDescExpanded=false;
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<ImageData> pageImageData=new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<ImageData> data) {
            super(fm);
            pageImageData=data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(pageImageData.get(position));
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }
    }
}
