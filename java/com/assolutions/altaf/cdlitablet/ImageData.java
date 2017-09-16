package com.assolutions.altaf.cdlitablet;



import java.io.Serializable;

/**
 * Created by HAWONG on 20-Aug-17.
 */

public class ImageData implements Serializable {
    String mTitle;
    String mDate;
    String mImageURL;
    String mShortInfo;
    String mFullInfo;

    public ImageData(String mTitle, String mDate, String mImageURL, String mShortInfo, String mFullInfo) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mImageURL = mImageURL;
        this.mShortInfo = mShortInfo;
        this.mFullInfo = mFullInfo;
    }



}
