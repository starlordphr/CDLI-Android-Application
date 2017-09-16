package com.assolutions.altaf.cdlitablet;



import java.io.Serializable;

/**
 * Created by HAWONG on 20-Aug-17.
 */

public class ImageData implements Serializable {
    String name;
    int localResource;
    String desc;

    public ImageData(String name,int localResource,String desc){
        this.name=name;
        this.localResource=localResource;
        this.desc=desc;
    }
}
