package com.awecode.commonlibrary.base;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.awecode.commonlibrary.retrofit.ApiInterface;
import com.awecode.commonlibrary.retrofit.ServiceGenerator;
import com.awecode.commonlibrary.utils.CommonUtils;
import com.awecode.commonlibrary.utils.Connectivity;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by munnadroid on 16/02/17.
 */

public abstract class BaseActivity extends AppCompatActivity implements Validator.ValidationListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public Context mContext;
    public Validator mValidator;
    public Bundle mBundle;
    protected ApiInterface mApiInterface = ServiceGenerator.createService(ApiInterface.class);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initOncreate();
    }

    private void initOncreate() {
        setContentView(getLayoutResourceId());
        ButterKnife.bind(this);
        mBundle = getIntent().getExtras();
        mContext = this;
        initializeValidator();
    }


    private void initializeValidator() {
        if (mValidator == null) {
            mValidator = new Validator(this);
            mValidator.setValidationListener(this);
        }
    }

    public void validate() {
        initializeValidator();
        mValidator.validate();
    }

    protected boolean containsBundleKey(String KEY) {

        if (getIntent() != null
                && mBundle != null)
            return mBundle.containsKey(KEY);
        else
            return false;
    }


    protected abstract int getLayoutResourceId();

    public void toast(String message) {
        CommonUtils.toast(mContext, message);
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        //TODO replace 0 by container id
        ft.replace(0,
                fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    public void openFragmentNoHistory(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        //TODO replace 0 by container id
        ft.replace(0,
                fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    public void makeFullScreenActivity() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public Spanned fromHtml(String str) {
        return CommonUtils.fromHtml(str);
    }

    public int get_color(int colorCode) {
        return ContextCompat.getColor(mContext, colorCode);
    }


    @Override
    public void onValidationSucceeded() {

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages ;)
            if (view instanceof EditText)
                ((EditText) view).setError(message);
            else
                toast(message);

        }
    }


    public void show_Dialog(String title, String message) {
        new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText("Ok")
                .show();
    }

    public boolean internetAvailable() {
        return Connectivity.isConnected(mContext);
    }

    public void noInternetDialog() {
        show_Dialog("Oops!", "Please connect to internet.");
    }


    public boolean check_intent_key(String KEY) {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEY))
            return true;

        return false;
    }
}
