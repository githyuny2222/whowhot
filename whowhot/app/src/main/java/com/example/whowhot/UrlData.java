package com.example.whowhot;

public class UrlData {
    private String url;
    private int danger;

    public UrlData() {
        // default
    }

    public UrlData(int danger, String url) {
        this.danger = danger;
        this.url = url;
    }

    public String getURL(){
        return url;
    }
    public int getDanger(){
        return danger;
    }

    public void setURL(String url){
        this.url = url;
    }
    public void setDanger(int danger){
        this.danger = danger;
    }
}
