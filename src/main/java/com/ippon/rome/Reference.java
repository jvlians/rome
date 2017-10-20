package com.ippon.rome;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public class Reference {

    private String hash;
    private String key;

    public Reference(String hash, String key){
        this.hash = hash;
        this.key = key;
    }

    public Reference(BufferedInputStream file){

    }

    public String getHash() {
        return hash;
    }

    public String getKey() {
        return key;
    }

    public BufferedOutputStream getData(){
        return null;
    }

    public void updateData(BufferedInputStream file){

    }

    public Reference getPreviousVersion(){
        //Return null if does not exist
        return null;
    }
}
