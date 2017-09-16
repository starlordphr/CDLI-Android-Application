package com.assolutions.altaf.cdlitablet;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


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
    ViewPager mViewPager;
    RequestQueue requestQueue ;
    JsonArrayRequest RequestOfJSonArray ;

    //`data` is used to set adapter for the viewpager for the first time. It's size will be 0 always.
    // It prevents app crash becuase we are fetching data from network
    ArrayList<ImageData> data=null;

    //`new_data` is fetched from network and it will be used to Update ViewPager's data after volley return Json.
    ArrayList<ImageData> new_data=null;

    String HTTP_JSON_URL = "http://cdli.ucla.edu/cdlitablet_android/fetchdata.php";
    String DATE_JSON = "date";
    String IMAGE_URL_JSON = "url";
    String BLURB_JSON = "blurb";
    String FULL_INFO_JSON = "full-info";
    String TITLE_JSON = "blurb-title";

    String date;
    String imagePath;
    String shortInfo;
    String fullInfo;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        data=new ArrayList<>();
        new_data=new ArrayList<>();


        // Create the adapter that will return a fragment for each Image
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),data);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        JSON_HTTP_CALL();

    }

    public void JSON_HTTP_CALL(){
        RequestOfJSonArray = new JsonArrayRequest(HTTP_JSON_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        ParseJSonResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(RequestOfJSonArray);
    }

    public void ParseJSonResponse(JSONArray array){
        for(int i = 0; i<array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                date = json.getString(DATE_JSON);
                imagePath = json.getString(IMAGE_URL_JSON);
                shortInfo = json.getString(BLURB_JSON);
                fullInfo = json.getString(FULL_INFO_JSON);
                title = json.getString(TITLE_JSON);

                Log.d("Date: ", date);
                Log.d("Image Url: ", imagePath);
                Log.d("Short Info: ", shortInfo);
                Log.d("Full Info: ", fullInfo);
                Log.d("Title: ", title);


                //Create and add new Image Object to arrayList
                ImageData imageData=new ImageData(title,date,imagePath,shortInfo,fullInfo);
                new_data.add(imageData);


                //TODO:Remove This. It is used to restrict app to 10 fragments
                if(i==10){
                    break;
                }
                


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Update viewpager data with data fetched from volley
        mSectionsPagerAdapter.UpdateData(new_data);


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

        //Expand Flag
        boolean isDescExpanded=false;

        //This value is used to collapse the expanded description till appropriate height,
        //so that short text will fit properly
        int desc_collapsed_height=0;

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

            //ImageData is implemented with serializable interface so that it can be passed to fragment.
            //passing ImageData object to fragment. So that fragment can populate data.
            args.putSerializable(DATA, data);
            fragment.setArguments(args);
            return fragment;
        }



        //This function will get called every time a new fragment is created
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //Inflate XML layout of fragment.
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            //Get UI element references
            final ImageData thisImage= (ImageData) getArguments().getSerializable(DATA);
            final TextView desc = (TextView) rootView.findViewById(R.id.desc_text);
            TextView title = (TextView) rootView.findViewById(R.id.desc_title);
            final TextView more_less=(TextView)rootView.findViewById(R.id.more_less);
            final TouchImageView imageView=(TouchImageView)rootView.findViewById(R.id.image_view);

            final LinearLayout descLayout=(LinearLayout) rootView.findViewById(R.id.desc_layout);
            final ContentLoadingProgressBar progressBar=(ContentLoadingProgressBar)rootView.findViewById(R.id.prog_bar);



            desc.setText(setHtmlText(thisImage.mShortInfo));
            //To handle HTML links present in the description
            desc. setMovementMethod(LinkMovementMethod.getInstance());
            title.setText(thisImage.mTitle);

            //You can use methods of this library as per your convenience i.e. cache strategy stc
            //Picasso
            Picasso.with(getContext()).
                    load(thisImage.mImageURL).
                    into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            //Set ProgressBar INVISIBLE after image is loaded in imageView
                            progressBar.setVisibility(View.INVISIBLE);
                            //Set default zoom to 1
                            imageView.setZoom(1);

                        }

                        @Override
                        public void onError() {
                            //Set Error Image here
                        }
                    });


            //Handle expand and collapse from textview click
            more_less.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(isDescExpanded){
                        CollapseView(descLayout,imageView);
                        //Set desc as shortText
                        desc.setText(setHtmlText(thisImage.mShortInfo));
                        more_less.setText(getResources().getString(R.string.more));

                        //***************************************************************************************
                        //         ENABLE IMAGE ZOOM ON COLLAPSE_VIEW(SHORT TEXT)
                        //
                        //*****************************************************************************************
                        //Enable image zooming
                        imageView.setZoom(1);
                        imageView.setEnabled(true);
                        //*************************************************************
                        //
                        //***************************************************************

                    }
                    else{
                        //This value is set here, so that it can be used while collapsing view
                        desc_collapsed_height=descLayout.getHeight();
                        ExpandView(descLayout,imageView);
                        //Set desc as shortText
                        desc.setText(setHtmlText(thisImage.mFullInfo));
                        desc.scrollTo(0,0);
                        more_less.setText(getResources().getString(R.string.less));

                        //***************************************************************************************
                        //          DISABLE IMAGE ZOOM WHILE SHOWING FULL DESC
                        //
                        //Handle background ZOOMING. Remove this block if you want image to ZOOM when showing full text
                        //*****************************************************************************************

                        //disable image zooming

                        //Reset animation
                        ValueAnimator imageZoomReset=ValueAnimator.ofFloat(imageView.getCurrentZoom(),1);
                        imageZoomReset.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float val= (float) valueAnimator.getAnimatedValue();
                                imageView.setZoom(val);
                            }
                        });

                        imageZoomReset.setDuration(1*500);
                        imageZoomReset.start();

                        imageView.setEnabled(false);
                        //**********************************************************************************************
                        //                      END
                        //*******************************************************************************************


                    }
                }
            });


            return rootView;
        }

        //This function will return text that can handle html formatting
        //if condition is for API changes
        private Spanned setHtmlText(String text){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
            }
            else{
               return Html.fromHtml(text);
            }
        }


        //Desc is expanded till it matches with rootView i.e. FrameLayout
        private void ExpandView(final View v, final View rootView) {


            //expand height from current height to FrameLayout's height
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
            //run anim1 for 1/2 seconds
            anim1.setDuration(1*500);
            anim1.start();


            //Set flag to true
            isDescExpanded=true;
        }

        //Desc is collapsed till it matches the initial height and width
        private void CollapseView(final View v, View rootView) {
            //Default width in 200dp
            int width=(int) getResources().getDimension(R.dimen.desc_width);

            //desc_collapsed_height : this is default height of the collapsed desc view after wrapping short text.
            //it can vary according to text content, hence it is given hardcoded value like `width`

            ValueAnimator anim1 = ValueAnimator.ofInt(rootView.getMeasuredHeight(),desc_collapsed_height);
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


                    //if text is scrolled in expanded view, reset it on collapse animation completion
                    if(val==desc_collapsed_height){
                        v.findViewById(R.id.desc_text).scrollTo(0,0);
                    }
                }
            });
            anim1.setDuration(1*500);
            anim1.start();

            isDescExpanded=false;
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        //pageImageData contains fragment data
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
            // Show total pages.
            return pageImageData.size();
        }

        //Update viewpager after getting response from volley
        public void UpdateData(ArrayList<ImageData> new_data){
            pageImageData.clear();
            pageImageData.addAll(new_data);
            notifyDataSetChanged();
        }
    }
}
