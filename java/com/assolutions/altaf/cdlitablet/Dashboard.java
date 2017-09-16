package com.assolutions.altaf.cdlitablet;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Dashboard extends AppCompatActivity {

    String HTTP_JSON_URL = "http://cdli.ucla.edu/cdlitablet_android/fetchdata.php";

    String DATE = "date";
    String IMAGE_THUMBNAIL_URL = "thumbnail-url";

    String mDate;
    String imageThumbnail;

    ArrayList<ThumbnailData> initialData=new ArrayList<>();
    ArrayList<ThumbnailData> thumbnailData=new ArrayList<>();

    MyRecyclerViewAdapter myRecyclerViewAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        JSON_HTTP_CALL(null,"thumbnail_fetch");


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.dashboard_recView);
        int numberOfColumns = 2;
        GridLayoutManager.LayoutParams params=new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,numberOfColumns);
        gridLayoutManager.generateLayoutParams(params);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, initialData);
        recyclerView.setAdapter(myRecyclerViewAdapter);

    }

    public void JSON_HTTP_CALL(String todaysdate,String type){
        String MODIFIED_JSON_URL = HTTP_JSON_URL;
        JsonArrayRequest RequestOfJSonArray ;
        RequestQueue requestQueue ;

        if(type == "specific_fetch")
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

        requestQueue = Volley.newRequestQueue(Dashboard.this);
        requestQueue.add(RequestOfJSonArray);
    }

    public void ParseJSonResponse(JSONArray array){
        for(int i = 0; i<array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                mDate=json.getString(DATE);
                imageThumbnail = json.getString(IMAGE_THUMBNAIL_URL);

                Log.d("Date: ", mDate);
                Log.d("Image Thumbnail: ", imageThumbnail);

                ThumbnailData tData=new ThumbnailData();
                tData.setmDate(mDate);
                tData.setmURL(imageThumbnail);

                thumbnailData.add(tData);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        myRecyclerViewAdapter.updateData(thumbnailData);

    }

    class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        Context context;
        ArrayList<ThumbnailData> data=null;

        public MyRecyclerViewAdapter(Context context, ArrayList<ThumbnailData> initialData) {
            this.context=context;
            this.data=initialData;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(context).inflate(R.layout.dashboard_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            ThumbnailData thisData=data.get(position);
            Picasso.with(context).load(thisData.getmURL()).placeholder(R.drawable.loader).into(holder.mImageView);

            holder.mYear.setText(getTextYear(thisData.getmDate()));
            holder.mDay.setText(getTextDay(thisData.getmDate()));
            holder.mMonth.setText(getTextMonth(thisData.getmDate()));


        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void updateData(ArrayList<ThumbnailData> new_data){
            data.clear();
            data.addAll(new_data);
            notifyDataSetChanged();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView mImageView;
            private LinearLayout dateLayout;
            private TextView mYear;
            private TextView mDay;
            private TextView mMonth;

           // private CircularTextView dateTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                mImageView=itemView.findViewById(R.id.thumb_imageview);
                dateLayout=itemView.findViewById(R.id.thumb_date_layout);
                mYear=itemView.findViewById(R.id.thumb_year);
                mDay=itemView.findViewById(R.id.thumb_day);
                mMonth=itemView.findViewById(R.id.thumb_month);
               // dateTextView=itemView.findViewById(R.id.thumb_dateview);
            }

            @Override
            public void onClick(View view) {
                onItemClick(view,getAdapterPosition());
            }
        }

        private void onItemClick(View view, int adapterPosition) {

            Intent intent=new Intent(Dashboard.this,SpecificImage.class);
            intent.putExtra("thumbDataDate",thumbnailData.get(adapterPosition).getmDate());


            if(Build.VERSION.SDK_INT>=21) {
                showCircularReveal(view, intent);
            }
            else{
                startActivity(intent);
            }




            //Toast.makeText(context,"Clicked :"+adapterPosition,Toast.LENGTH_LONG).show();
        }

        private void showCircularReveal(View view, final Intent thisIntent) {
            // get the center for the clipping circle
            int cx = view.getWidth() / 2;
            int cy = view.getHeight() / 2;

// get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);

// create the animator for this view (the start radius is zero)
            @SuppressLint({"NewApi", "LocalSuppress"})
            Animator anim =ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);


// make the view visible and start the animation

            anim.start();
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    startActivity(thisIntent);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }


        private String getTextYear(String date){
            String arr[]=date.split("-");
            return arr[0];
        }
        private String getTextDay(String date){
            String arr[]=date.split("-");
            return arr[2];
        }
        private String getTextMonth(String date){
            String arr[]=date.split("-");
            int month=Integer.parseInt(arr[1]);

            String monthStr="";

            switch (month){
                case 1: monthStr="Jan"; break;
                case 2: monthStr="Feb"; break;
                case 3: monthStr="Mar"; break;
                case 4: monthStr="Apr"; break;
                case 5: monthStr="May"; break;
                case 6: monthStr="Jun"; break;
                case 7: monthStr="Jul"; break;
                case 8: monthStr="Aug"; break;
                case 9: monthStr="Sep"; break;
                case 10: monthStr="Oct"; break;
                case 11: monthStr="Nov"; break;
                case 12: monthStr="Dec"; break;
                default:    monthStr="Jan";

            }
            return monthStr;
        }


    }
}


