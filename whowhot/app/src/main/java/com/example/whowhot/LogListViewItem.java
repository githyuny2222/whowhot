package com.example.whowhot;

public class LogListViewItem {
    private String logText;

    public LogListViewItem(){
        // default
    }

    public LogListViewItem(String logText){
        this.logText = logText;
    }

    public String getLogText() { return logText; }

    public void setLogText(String logText) { this.logText = logText; }
}
