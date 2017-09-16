package com.assolutions.altaf.cdlitablet;

import android.animation.ValueAnimator;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecificImage extends AppCompatActivity {

    final String THUMB_DATA_DATE="thumbDataDate";
    String HTTP_JSON_URL = "http://cdli.ucla.edu/cdlitablet_android/fetchdata.php";

    String DATE_JSON = "date";
    String IMAGE_URL_JSON = "url";
    String BLURB_JSON = "blurb";
    String FULL_INFO_JSON = "full-info";
    String TITLE_JSON = "blurb-title";
    String IMAGE_THUMBNAIL_URL = "thumbnail-url";

    String date;
    String imagePath;
    String shortInfo;
    String fullInfo;
    String title;
    String imageThumbnail;

    //Expand Flag
    boolean isDescExpanded=false;

    //This value is used to collapse the expanded description till appropriate height,
    //so that short text will fit properly
    int desc_collapsed_height=0;

    ImageData imageData=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_image);

        getSupportActionBar().setHomeButtonEnabled(true);


        String date=getIntent().getStringExtra(THUMB_DATA_DATE);

        JSON_HTTP_CALL(date,"specific_fetch");

    }


    public void SetData(){

        setTitle(imageData.mTitle);

        final TextView desc = (TextView)findViewById(R.id.spec_desc_text);
        TextView title = (TextView) findViewById(R.id.spec_desc_title);
        final TextView more_less=(TextView)findViewById(R.id.spec_more_less);
        final TouchImageView imageView=(TouchImageView)findViewById(R.id.spec_image_view);

        final LinearLayout descLayout=(LinearLayout)findViewById(R.id.spec_desc_layout);
        final ContentLoadingProgressBar progressBar=(ContentLoadingProgressBar)findViewById(R.id.spec_prog_bar);



        desc.setText(setHtmlText(imageData.mShortInfo));
        //To handle HTML links present in the description
        desc. setMovementMethod(LinkMovementMethod.getInstance());
        title.setText(imageData.mTitle);



        //You can use methods of this library as per your convenience i.e. cache strategy stc
        //Picasso
        Picasso.with(SpecificImage.this).
                load(imageData.mImageURL).
                into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //Set ProgressBar INVISIBLE after image is loaded in imageView
                        progressBar.setVisibility(View.INVISIBLE);
                        //Set default zoom to 1
                        imageView.setZoom(1);

                        descLayout.setVisibility(View.VISIBLE);
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
                    desc.setText(setHtmlText(imageData.mShortInfo));
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
                    desc.setText(setHtmlText(imageData.mFullInfo));
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
    }

    public void JSON_HTTP_CALL(String todaysdate,String type){
        String MODIFIED_JSON_URL = HTTP_JSON_URL;
        JsonArrayRequest RequestOfJSonArray ;
        RequestQueue requestQueue ;

        if(type == "specific_fetch")
        {
            MODIFIED_JSON_URL = HTTP_JSON_URL+"?specificdate="+todaysdate;
        }


        RequestOfJSonArray = new JsonArrayRequest(MODIFIED_JSON_URL,
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

        requestQueue = Volley.newRequestQueue(SpecificImage.this);
        requestQueue.add(RequestOfJSonArray);
    }

    public void ParseJSonResponse(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                date = json.getString(DATE_JSON);
                imagePath = json.getString(IMAGE_URL_JSON);
                shortInfo = json.getString(BLURB_JSON);
                fullInfo = json.getString(FULL_INFO_JSON);
                title = json.getString(TITLE_JSON);
                imageThumbnail = json.getString(IMAGE_THUMBNAIL_URL);

                Log.d("Date: ", date);
                Log.d("Image Url: ", imagePath);
                Log.d("Short Info: ", shortInfo);
                Log.d("Full Info: ", fullInfo);
                Log.d("Title: ", title);
                Log.d("Image Thumbnail: ", imageThumbnail);


                imageData = new ImageData(title, date, imagePath, shortInfo, fullInfo, imageThumbnail);
                SetData();


                //TODO:Remove This. It is used to restrict app to 10 fragments
                /*if(i==10){
                    break;
                }*/


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                layoutParams.setMargins(0,24,0,0);
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
                layoutParams.gravity= Gravity.BOTTOM|Gravity.END;
                layoutParams.bottomMargin= (int) getResources().getDimension(R.dimen.bottom_margin);
                layoutParams.rightMargin=(int) getResources().getDimension(R.dimen.fab_margin);
                v.setLayoutParams(layoutParams);


                //if text is scrolled in expanded view, reset it on collapse animation completion
                if(val==desc_collapsed_height){
                    v.findViewById(R.id.spec_desc_text).scrollTo(0,0);
                }
            }
        });
        anim1.setDuration(1*500);
        anim1.start();

        isDescExpanded=false;
    }
}
