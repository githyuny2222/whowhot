package com.example.whowhot;

public class UrlData {
    private String url;
    private String type;
    private int danger;

    public UrlData() {
        // default
    }

    public UrlData(int danger, String url, String type) {
        this.danger = danger;
        this.url = url;
        this.type = type;
    }

    public String getURL(){
        return url;
    }
    public String getType(){
        return type;
    }
    public int getDanger(){
        return danger;
    }

    public void setType(String type){
        this.type = type;
    }
    public void setURL(String url){
        this.url = url;
    }
    public void setDanger(int danger){
        this.danger = danger;
    }
}
