package com.example.whowhot;

public class UrlData {
    private String url;
    private int type;
    private int danger;

    public UrlData() {
        // default
    }

    public UrlData(String url) {
        this.url = url;
    }

    // url과 위험도
    public UrlData(int danger, String url) {
        this.danger = danger;
        this.url = url;
    }

    // url과 위험도와 type
    public UrlData(int danger, String url, int type) {
        this.danger = danger;
        this.url = url;
        this.type = type;
    }

    public String getURL(){
        return url;
    }
    public int getType(){
        return type;
    }
    public int getDanger(){
        return danger;
    }

    public void setType(int type){
        this.type = type;
    }
    public void setURL(String url){
        this.url = url;
    }
    public void setDanger(int danger){
        this.danger = danger;
    }
}
