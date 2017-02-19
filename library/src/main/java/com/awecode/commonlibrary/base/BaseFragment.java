package com.awecode.commonlibrary.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.awecode.commonlibrary.retrofit.ApiInterface;
import com.awecode.commonlibrary.retrofit.ServiceGenerator;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by munnadroid on 16/02/17.
 */
public abstract class BaseFragment extends Fragment implements Validator.ValidationListener {
    protected Context mContext;
    protected ProgressDialog mProgressDialog;
    protected ApiInterface mApiInterface = ServiceGenerator.createService(ApiInterface.class);
    protected Validator mValidator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    public abstract int getLayoutId();


    public void openFragment(Fragment fragment) {
        ((BaseActivity) getActivity()).openFragment(fragment);
    }

    public void openFragmentNoHistory(Fragment fragment) {
        ((BaseActivity) getActivity()).openFragmentNoHistory(fragment);
    }

    public void show_progress_dialog(String message) {
        mProgressDialog = new ProgressDialog(mContext);
        if (TextUtils.isEmpty(message))
            message = "Please Wait...";
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

    }

    public void dissmiss_dialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    public void toast(String message) {
        try {
            ((BaseActivity) mContext).toast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void noInternetDialog() {
        ((BaseActivity) mContext).noInternetDialog();
    }

    public boolean internetAvailable() {
        return ((BaseActivity) mContext).internetAvailable();
    }

    public Spanned fromHtml(String str) {
        return ((BaseActivity) getActivity()).fromHtml(str);
    }

    @Override
    public void onValidationSucceeded() {

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getActivity());
            // Display error messages ;)
            if (view instanceof EditText)
                ((EditText) view).setError(message);
            else
                toast(message);

        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }

        //INSERT CUSTOM CODE HERE
        onFragmentVisible();
    }

    public void onFragmentVisible() {

    }


    public int get_color(int colorCode) {
        return ContextCompat.getColor(mContext, colorCode);
    }
}
