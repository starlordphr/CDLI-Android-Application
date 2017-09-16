package com.assolutions.altaf.cdlitablet;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
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
    ViewPager mViewPager;

    //`data` is used to set adapter for the viewpager for the first time. It's size will be 0 always.
    // It prevents app crash becuase we are fetching data from network
    ArrayList<ImageData> data=null;

    //`new_data` is fetched from network and it will be used to Update ViewPager's data after volley return Json.
    ArrayList<ImageData> new_data=null;
    ArrayList<String> fetched_dates = null;

    String HTTP_JSON_URL = "http://cdli.ucla.edu/cdlitablet_android/fetchdata.php";
    String WEB_VERSION = "http://cdli.ucla.edu/cdlitablet/showcase?date=";
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

    String downloadFolder = "cdli_tablet";
    String PATH = Environment.getExternalStorageDirectory()+ "/"+downloadFolder+"/";
    public static final int REQUEST_CODE = 1;
    public int CHECK_COUNTER = 8;

    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.app.ActionBar actionBar = getActionBar();

        //permissionManager = new PermissionManager() {};
        permissionManager = new PermissionManager() {
            @Override
            public void ifCancelledAndCanRequest(Activity activity) {
                // Do Customized operation if permission is cancelled without checking "Don't ask again"
                // Use super.ifCancelledAndCanRequest(activity); or Don't override this method if not in use
                Toast.makeText(getApplicationContext(), "Permissions not granted.\nApplication won't function properly.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void ifCancelledAndCannotRequest(Activity activity) {
                // Do Customized operation if permission is cancelled with checking "Don't ask again"
                // Use super.ifCancelledAndCannotRequest(activity); or Don't override this method if not in use
                Toast.makeText(getApplicationContext(), "Permissions not granted.\nApplication won't function properly.",Toast.LENGTH_SHORT).show();
            }
        };
        permissionManager.checkAndRequestPermissions(this);

        data=new ArrayList<>();
        new_data=new ArrayList<>();
        fetched_dates=new ArrayList<>();


        // Create the adapter that will return a fragment for each Image
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),data);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);



        checkConnection();

        // listening for page changes
        ViewPager.SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("Page: ",Integer.toString(mViewPager.getCurrentItem()));
                if(position !=0 && position % CHECK_COUNTER == 0)
                {
                    //Toast.makeText(getApplicationContext(), "Fetch Date: "+getDate(mViewPager.getCurrentItem()),Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), "Fetch Date: "+incrementDate(getLastDate()),Toast.LENGTH_SHORT).show();
                    if(!ifPageExists(getDate(mViewPager.getCurrentItem())))
                    {
                        JSON_HTTP_CALL(incrementDate(getLastDate(), -1), "batch_fetch");
                        CHECK_COUNTER += 10;
                    }
                }
            }
        };
        mViewPager.addOnPageChangeListener(listener);
        listener.onPageSelected(0); // due to a bug in listener implementation
    }

    private void checkConnection(){

        CoordinatorLayout coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        if(ConnectivityChecker.isConnected(getApplicationContext())) {
            JSON_HTTP_CALL(null, "initial_fetch");
        }
        else{
            Snackbar.make(coordinatorLayout,"No network available!",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkConnection();
                        }
                    })
                    .show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
       // Toast.makeText(getApplicationContext(), "Permissions Granted.\nEnjoy CDLI Tablet :) ",Toast.LENGTH_SHORT).show();
    }

    public boolean ifPageExists(String date)
    {
        String checkDate = incrementDate(date, -2);
        Log.d("Sent Date: ",date);
        Log.d("Check Date: ",checkDate);
        if(fetched_dates.contains(checkDate))
        {
            Log.d("Page Flag: ","True ... dont fetch");
            return true;
        }
        Log.d("Page Flag: ","False .... do fetch");
        return false;
    }

    public String incrementDate(String date, int counter)
    {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat("yyyy-MM-dd");

        DateTime dtOrg = new DateTime(date);
        DateTime dtPlusOne = dtOrg.plusDays(counter);
        return sdfr.format(dtPlusOne.toDate());
    }

    public void JSON_HTTP_CALL(String todaysdate,String type){
        String MODIFIED_JSON_URL = HTTP_JSON_URL;
        JsonArrayRequest RequestOfJSonArray ;
        RequestQueue requestQueue ;

        if(new_data.size() != 0 && todaysdate!=null && type=="batch_fetch")
        {
            MODIFIED_JSON_URL = HTTP_JSON_URL+"?todaysdate="+todaysdate;
            Log.d("URL: ",MODIFIED_JSON_URL);
        }
        else if(type == "initial_fetch"){
            MODIFIED_JSON_URL = HTTP_JSON_URL;
        }
        else if(type == "specific_fetch")
        {
            MODIFIED_JSON_URL = HTTP_JSON_URL+"?specificdate="+todaysdate;
        }
        else if(type == "thumbnail_fetch")
        {
            MODIFIED_JSON_URL = HTTP_JSON_URL+"?thumbnaildate=null";
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
                imageThumbnail = json.getString(IMAGE_THUMBNAIL_URL);

                Log.d("Date: ", date);
                Log.d("Image Url: ", imagePath);
                Log.d("Short Info: ", shortInfo);
                Log.d("Full Info: ", fullInfo);
                Log.d("Title: ", title);
                Log.d("Image Thumbnail: ", imageThumbnail);


                //Create and add new Image Object to arrayList
                fetched_dates.add(date);
                ImageData imageData=new ImageData(title,date,imagePath,shortInfo,fullInfo, imageThumbnail);
                new_data.add(imageData);


                //TODO:Remove This. It is used to restrict app to 10 fragments
                /*if(i==10){
                    break;
                }*/
                


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
        getMenuInflater().inflate(R.menu.activity_share_action, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shareId) {
            new DownloadFile().execute(getImageThumbnail(mViewPager.getCurrentItem()));
            //Toast.makeText(getApplicationContext(), "Page: "+Integer.toString(mViewPager.getCurrentItem()),Toast.LENGTH_LONG).show();

            return true;
        }
        else if(id==R.id.dashboard){
            Intent intent=new Intent(MainActivity.this,Dashboard.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        String targetFileName=getDate(mViewPager.getCurrentItem())+".jpg";
        String tempFilePath =    PATH+targetFileName;
        File tempFile = new File(tempFilePath);
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user pressed ok
                //Toast.makeText(getApplicationContext(), "Intent Closed OK",Toast.LENGTH_LONG).show();
                tempFile.delete();
            }else{
                // The user pressed cancel
                //Toast.makeText(getApplicationContext(), "Intent Canceled",Toast.LENGTH_LONG).show();
                tempFile.delete();
            }
        }
    }

    public String getDate(int counter)
    {
        return new_data.get(counter).mDate;
    }

    public String getLastDate()
    {
        return new_data.get(new_data.size()-1).mDate;
    }

    public String getShortInfo(int counter)
    {
        return new_data.get(counter).mShortInfo;
    }

    public String getImageURL(int counter)
    {
        return new_data.get(counter).mImageURL;
    }

    public String getTitle(int counter)
    {
        return new_data.get(counter).mTitle;
    }

    public String getImageThumbnail(int counter)
    {
        return new_data.get(counter).mImagethumbnail;
    }

    public String getEmailBody(int counter)
    {
        String temp = "I saw this entry on the Android app \"cdli tablet\" and wanted to share it with you:\n\n";
        temp += "\""+getShortInfo(counter)+"\"\n\n";
        return temp;
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
                    layoutParams.setMargins(0,24,0,0);
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

        private int convertToDP(int pixels){
             return (int) (pixels / Resources.getSystem().getDisplayMetrics().density);
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




    class DownloadFile extends AsyncTask<String,Integer,Long> {
        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
        String strFolderName;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* mProgressDialog.setMessage("Downloading Attachment");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
            */
        }
        @Override
        protected Long doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL((String) aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                String targetFileName=getDate(mViewPager.getCurrentItem())+".jpg";
                String tempFilePath =    PATH+targetFileName;
                int lengthOfFile = conexion.getContentLength();

                File folder = new File(PATH);
                File thumbnailfile = new File(tempFilePath);
                if(!folder.exists()){
                    folder.mkdir();//If there is no folder it will be created.
                }
                if(thumbnailfile.exists())
                {
                   // mProgressDialog.dismiss();
                    return null;
                }
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(tempFilePath);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress ((int)(total*100/lengthOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
           /* mProgressDialog.setProgress(progress[0]);
            if(mProgressDialog.getProgress()==mProgressDialog.getMax()){
                mProgressDialog.dismiss();
            }
            */
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            //Toast.makeText(getApplicationContext(), "Attachment Downloaded", Toast.LENGTH_SHORT).show();

            //Intent for email
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT,getTitle(mViewPager.getCurrentItem())+" - cdli tablet");
            intent.putExtra(Intent.EXTRA_TEXT,getEmailBody(mViewPager.getCurrentItem()));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getImageThumbnail(mViewPager.getCurrentItem())));

            //Attachment
            String targetFileName=getDate(mViewPager.getCurrentItem())+".jpg";
            String tempFilePath =    PATH+targetFileName;
            Uri uriPath = Uri.fromFile(new File(tempFilePath));
            intent.putExtra(Intent.EXTRA_STREAM, uriPath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //intent.setType("message/rfc822");
            //intent.setType("text/plain");
            //intent.setType("text/html");
            intent.setType("image/jpeg");
            Intent chooser = Intent.createChooser(intent,"Share");

            //Start activity
            // Verify that the intent will resolve to an activity
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooser, REQUEST_CODE);
                //startActivity(chooser);
            }
        }


    }
}
