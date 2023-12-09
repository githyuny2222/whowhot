package com.example.whowhot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final String urlREGEX ="(http(s)?:\\/\\/|www.)?(([a-z0-9\\w])(\\.*))+[a-z-]{2,4}([\\/a-z0-9-%#º@?&=+\\w])+(\\.[a-z\\/]{2,4}(\\?[\\/a-z0-9-@%#?&=\\w]+)*)?([가-힣])*";
    private static final String phoneREGEX = "\\b(\\+?[0-9]+[-.\\s]?\\(?[0-9]+\\)?[-.\\s]?[0-9]+[-.\\s]?[0-9]+[-.\\s]?[0-9]+)\\b";

    // default constructor
    public Parser(){}

    /* 메시지에서 URL 파싱 */
    public String parseURL(String content){
        Pattern pattern = Pattern.compile(urlREGEX, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);
        String url = "";
        while (urlMatcher.find()) {
            String urls= urlMatcher.group();
            if (urls.contains(".")) {
                url = urls;
            }
        }
        return url;
    }

    /* 메시지에서 전화번호 파싱 */
    public String parsePhone(String content){
        Pattern pattern = Pattern.compile(phoneREGEX);
        Matcher phoneMatcher = pattern.matcher(content);
        if (phoneMatcher.find()){
            return phoneMatcher.group();
        }
        else
            return "";
    }
}
