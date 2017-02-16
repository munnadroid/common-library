package com.awecode.commonlibrary.retrofit;

import android.content.Context;

import retrofit2.Response;
import rx.Observer;

/**
 * Created by munnadroid on 12/18/16.
 */
public abstract class MyResponseHandler implements Observer<Response<?>> {

    private Context context;

    public MyResponseHandler(Context context) {
        this.context = context;
    }

    private MyResponseHandler() {
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Response<?> response) {
        int response_code = response.code();
        if (response_code == 401
                || response_code == 403)
            startLoginActivity();
    }


    private void startLoginActivity() {
        //TODO do logout here
    }


}
