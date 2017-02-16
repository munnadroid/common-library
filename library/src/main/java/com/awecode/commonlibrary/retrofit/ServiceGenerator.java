package com.awecode.commonlibrary.retrofit;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by munnadroid on 10/23/16.
 */
public class ServiceGenerator {
    private static final String TAG = ServiceGenerator.class.getSimpleName();

    private static final int TIME_OUT = 20;

    public static final String API_BASE_URL = "";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    public static Retrofit retrofit;
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());


    public static <S> S createService(Class<S> serviceClass) {
        setTimeouts();
        addCustomHeader();
        // add logging as last interceptor
//        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));  // <-- this is the important line!
        retrofit = builder.
                client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    private static void setTimeouts() {
        httpClient.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
    }

    private static void addCustomHeader() {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request original = chain.request();

                //TODO - pass login token here
                String token = "";
                Request.Builder requestBuilder;
                if (TextUtils.isEmpty(token)) {
                    requestBuilder = original.newBuilder()
                            .header("Content-Type", "application/json");
                } else {
                    requestBuilder = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Token " + token);
                }

                Request request = requestBuilder.build();
                Response response = chain.proceed(request);
                return response;

            }
        });
    }

}
