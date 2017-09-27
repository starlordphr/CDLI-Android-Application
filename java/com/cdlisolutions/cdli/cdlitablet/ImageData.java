package com.cdlisolutions.cdli.cdlitablet;



import java.io.Serializable;

/**
 * Created by CDLI on 20-Aug-17.
 */

public class ImageData implements Serializable {
    String mTitle;
    String mDate;
    String mImageURL;
    String mShortInfo;
    String mFullInfo;
    String mImagethumbnail;

    public ImageData(String mTitle, String mDate, String mImageURL, String mShortInfo, String mFullInfo, String mImagethumbnail) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mImageURL = mImageURL;
        this.mShortInfo = mShortInfo;
        this.mFullInfo = mFullInfo;
        this.mImagethumbnail = mImagethumbnail;
    }



}
