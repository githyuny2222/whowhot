package com.example.whowhot;

import android.util.Log;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VirustotalThread extends Thread{
    private static final String BASE_URL = "https://www.virustotal.com/vtapi/v2/";
    private static final String API_KEY = "a0e8c394a951c2010e28b8b78cf955727aeff325dc995c657a0a63ad23eb3f1d";

    private Boolean isRunning = false;
    private int result_positive = -1;
    private VirusTotalApi virusTotalApiService;
    private String target_url;

    static final String TAG = "TEST_VT";   // Log.d용 태그

    public VirustotalThread() {
    }

    public VirustotalThread(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        super.run();

        VirusTotalApiClient();

        while (isRunning){
            try {
                // Create a Retrofit Call object for the API request
                Call<JsonObject> call = virusTotalApiService.scanUrl(API_KEY, target_url);
                // Execute the request and get the response
                Response<JsonObject> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response
                    JsonObject result = response.body();

                    String result_str = result.toString();
                    //Log.d(TAG, result_str);

                    int response_index_begin = result_str.indexOf("\"response_code\":");
                    int sizeof_reponse_code = "\"response_code\":".length();
                    int response_index_end = response_index_begin + sizeof_reponse_code;
                    int result_int;

                    String response_code_str = result_str.substring(response_index_end, response_index_end+1);
                    Log.d(TAG, "response : " + response_code_str);

                    // 응답 없는 URL
                    if(response_code_str.equals("0")){
                        Log.d(TAG, "응답 없는 URL");
                        result_int = 0;
                    }
                    else {
                        int positive_index = result.toString().indexOf("\"positives\":");

                        String result_str2 = result_str.substring(positive_index + 12, positive_index + 18);
                        String result_str3 = result_str2.substring(0, result_str2.indexOf(","));
                        //Log.d(TAG, "result_str1 : " + result_str2);
                        //Log.d(TAG, "result_str2 : " + result_str2);
                        //Log.d(TAG, "result_str3: " + result_str3); // 감지한 숫자

                        result_int = Integer.parseInt(result_str3);
                    }
                    setResult(result_int);
                } else {
                    // Handle the error response
                    Log.d(TAG, "Error: " + response.message());
                }
                isRunning = false;
            }
            catch (Exception e){
                e.printStackTrace();
                isRunning = false;
                Log.d(TAG, "Error: " + e.getMessage(), e);
            }
        }
    }

    public void setIsRunning(Boolean bool){ this.isRunning = bool; }
    public void setResult(int result){ this.result_positive = result; }
    public void setTargetUrl(String target_url){ this.target_url = target_url; }

    public int getResult(){
        return result_positive;
    }

    public void VirusTotalApiClient() {
        // Create an OkHttpClient with logging interceptor
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);
        // Build Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        // Create the API service
        virusTotalApiService = retrofit.create(VirusTotalApi.class);
    }
}
