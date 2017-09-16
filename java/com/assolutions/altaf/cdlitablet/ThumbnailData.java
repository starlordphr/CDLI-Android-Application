package com.assolutions.altaf.cdlitablet;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HAWONG on 07-Sep-17.
 */

public class ThumbnailData implements Serializable {

    private String mDate;
    private String mURL;


    public String getmDate() {
        return mDate;
    }

    public String getmURL() {
        return mURL;
    }

    public void setmDate(String mDate) { this.mDate = mDate;  }

    public void setmURL(String mURL) {
        this.mURL = mURL;
    }



}
