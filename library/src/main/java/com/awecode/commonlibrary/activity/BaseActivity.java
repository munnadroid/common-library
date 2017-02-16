package com.awecode.commonlibrary.activity;

/**
 * Created by munnadroid on 2/16/17.
 */


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.awecode.commonlibrary.R;
import com.awecode.squaddraft.MyApplication;
import com.awecode.squaddraft.http.FcmDeviceIdSendHttpService;
import com.awecode.squaddraft.model.other.ContestListType;
import com.awecode.squaddraft.model.other.TapTargetData;
import com.awecode.squaddraft.model.pojo.Download;
import com.awecode.squaddraft.model.pojo.apk_download.ApkDownloadResponse;
import com.awecode.squaddraft.util.Connectivity;
import com.awecode.squaddraft.util.Constants;
import com.awecode.squaddraft.util.DownloadService;
import com.awecode.squaddraft.util.PrefsHelper;
import com.awecode.squaddraft.util.Util;
import com.awecode.squaddraft.util.VersionManager;
import com.awecode.squaddraft.util.prefs.Prefs;
import com.awecode.squaddraft.util.retrofit.ApiInterface;
import com.awecode.squaddraft.util.retrofit.ServiceGenerator;
import com.awecode.squaddraft.util.stateLayout.StateLayout;
import com.awecode.squaddraft.views.aboutus.AboutusActivity;
import com.awecode.squaddraft.views.contest.ContestFragment;
import com.awecode.squaddraft.views.home.HomeActivity;
import com.awecode.squaddraft.views.my_squads.MySquadsActivity;
import com.awecode.squaddraft.views.nav.NavigationDrawerFragment;
import com.awecode.squaddraft.views.nav.NavigationItem;
import com.awecode.squaddraft.views.profile.ProfileActivity;
import com.awecode.squaddraft.views.refer_friend.ReferFriendActivity;
import com.awecode.squaddraft.views.setting.SettingActivity;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;


public abstract class BaseActivity extends AppCompatActivity implements Validator.ValidationListener, {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public Context mContext;
    public Validator mValidator;
    public Bundle mBundle;
    protected ApiInterface mApiInterface = ServiceGenerator.createService(ApiInterface.class);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        ButterKnife.bind(this);
        mBundle = getIntent().getExtras();
        mContext = this;
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
        registerReceiver();
    }

    protected boolean containsBundleKey(String KEY) {

        if (getIntent() != null
                && mBundle != null)
            return mBundle.containsKey(KEY);
        else
            return false;
    }

    /**
     * if fcm device id not sent yet,
     * get fcm token and send it to server
     */
    public void handleFcmDeviceRegistration() {

        try {
            if (!PrefsHelper.getFcmDeviceIdSendStatus()
                    && FirebaseInstanceId.getInstance() != null
                    && FirebaseInstanceId.getInstance().getToken() != null) {
                PrefsHelper.saveFcmDeviceId(FirebaseInstanceId.getInstance().getToken());
                PrefsHelper.createFcmDeviceIdSendStatus(true);

            }

            //send refresh fcm token
            FcmDeviceIdSendHttpService.sendFcmDeviceId(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * setup toolbar and collapsing toolbar
     */
    public void setupToolbar_navigation(View toolbarLayout,
                                        Toolbar toolbar,
                                        AppBarLayout appBarLayout,
                                        CollapsingToolbarLayout collapsingToolbarLayout,
                                        DrawerLayout drawerLayout,
                                        NavigationDrawerFragment navigationDrawerFragment,
                                        CoordinatorLayout container) {

        this.mToolbarLayout = toolbarLayout;
        this.mToolbar = toolbar;
        this.mAppbar = appBarLayout;
        this.mDrawerLayout = drawerLayout;
        this.mNavigationDrawerFragment = navigationDrawerFragment;
        this.mContainer = container;

        if (mAppbar != null)
            mAppbar.addOnOffsetChangedListener(this);
        if (mToolbarLayout != null)
            mToolbarLayout.setVisibility(View.GONE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mNavigationDrawerFragment.setup(R.id.fragment_drawer, mDrawerLayout, mToolbar, mContainer);
        mDrawerLayout.closeDrawer(Gravity.LEFT);

    }

    private void initializeGoogleAnalytics() {
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();

    }


    public void sendPageLoadEvent(String which) {
        if (mTracker == null)
            initializeGoogleAnalytics();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.page_load))
                .setAction(which)
                .build());
    }

    public void sendScreenName(String screenName) {
        if (mTracker == null)
            initializeGoogleAnalytics();
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    public void sendClickEvent(String which) {
        if (mTracker == null)
            initializeGoogleAnalytics();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.click))
                .setAction(which)
                .build());
    }

    public void sendBackEvent(String which) {
        if (mTracker == null)
            initializeGoogleAnalytics();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.back))
                .setAction(which)
                .build());
    }

    protected abstract int getLayoutResourceId();

    public void toast(String message) {
        Util.toast(mContext, message);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.container,
                fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    public void openFragmentNoHistory(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.container,
                fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    public void makeFullScreenActivity() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public Spanned fromHtml(String str) {
        return Util.fromHtml(str);
    }

    public int get_Color(int colorCode) {
        return ContextCompat.getColor(mContext, colorCode);
    }

    public Typeface getBoldFontFace() {
        return TypefaceUtils.load(mContext.getAssets(), "fonts/Montserrat-Bold.ttf");
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

    protected void showProgressView(String message) {
        try {
            mStateLayout.showProgressView(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showProgressView() {
        try {
            mStateLayout.showProgressView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showErrorView(String message) {
        try {
            mStateLayout.showErrorView(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEmptyView(String message) {
        mStateLayout.showEmptyView(message);
    }


    public void showContentView() {
        try {
            mStateLayout.showContentView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show_Dialog(String title, String message) {
        new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText(R.string.dismiss)
                .show();
    }

    public boolean internetAvailable() {
        return Util.internetConnectionAvailable(mContext);
    }

    public void noInternetDialog() {
        show_Dialog(getString(R.string.oops_string), getString(R.string.please_connect_internet));
    }

    public StateLayout getStateLayout() {
        return mStateLayout;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        this.mVerticalOffset = verticalOffset;
    }

    public int getVerticalOffset() {
        return mVerticalOffset;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            return mNavigationDrawerFragment.mActionBarDrawerToggle.onOptionsItemSelected(menuItem);
        }
        return super.onOptionsItemSelected(menuItem);
    }


    public void simpleSnackBar(String message) {
        Snackbar.make(getStateLayout(), message, Snackbar.LENGTH_SHORT)
                .setDuration(2000).show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, NavigationItem navigationItem) {
        String savedViewName = PrefsHelper.getNavDrawerViewName();
        if (!navigationItem.getMenuType().toString().equalsIgnoreCase(savedViewName)) {
            PrefsHelper.saveNavDrawerViewName(navigationItem.getMenuType().toString());
            Intent intent = new Intent();

            switch (navigationItem.getMenuType()) {
                case HOME:
                    sendClickEvent("side_menu_home");
                    intent.setClass(mContext, HomeActivity.class);
                    intent.putExtra(HomeActivity.INTENT_LIST_TYPE, ContestListType.GAMES);
                    break;
                case HISTORY:
                    sendClickEvent("side_menu_history");
                    intent.setClass(mContext, HomeActivity.class);
                    intent.putExtra(HomeActivity.INTENT_LIST_TYPE, ContestListType.HISTORY);
                    break;
                case MY_SQUAD:
                    sendClickEvent("side_menu_my_squad");
                    intent.setClass(mContext, MySquadsActivity.class);
                    break;

                case PROFILE:
                    sendClickEvent("side_menu_profile");
                    intent.setClass(mContext, ProfileActivity.class);
                    break;
                case REFER_FRIEND:
                    sendClickEvent("side_menu_refer_a_friend");
                    intent.setClass(mContext, ReferFriendActivity.class);
                    break;
                case SETTING:
                    sendClickEvent("side_menu_setting");
                    intent.setClass(mContext, SettingActivity.class);
                    break;
                case ABOUTUS:
                    sendClickEvent("side_menu_aboutus");
                    intent.setClass(mContext, AboutusActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }

    public boolean check_intent_key(String KEY) {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEY))
            return true;

        return false;
    }

    public void setRefreshingDone(PtrClassicFrameLayout ptrClassicFrameLayout) {
        if (ptrClassicFrameLayout != null && ptrClassicFrameLayout.isShown())
            ptrClassicFrameLayout.refreshComplete();
    }

    public void set_check_for_update(boolean checkForUpdate) {
        this.mCheckForUdpate = checkForUpdate;
    }

    /**
     * check for storage permission and
     * download apk service is running or not
     */
    public void setup_latest_apk_download() {
        if (!Constants.IS_UPDATE_CANCEL_CLICKED
                || mCheckForUdpate)
            if (isStoragePermissionGranted()) //check for storage permission
                checkApkDownloadServiceRunning();


    }

    private void checkApkDownloadServiceRunning() {
        if (!Util.isMyServiceRunning(DownloadService.class, mContext)) //check if service is running or not
            request_latest_apk_version_check();
        else {
            if (mCheckForUdpate)
                simpleSnackBar("Download is already running.");
        }
    }

    /**
     * start apk download service
     */
    protected void start_apk_download_service(ApkDownloadResponse apkDownloadResponse) {
        DownloadService.shouldContinue = true;
        downloadIntentService = new Intent(mContext, DownloadService.class);
        downloadIntentService.putExtra(DownloadService.INTENT_APK_DOWNLOAD_URL, apkDownloadResponse);
        startService(downloadIntentService);
    }


    /**
     * apk download progress broadcast receiver
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(INTENT_MESSAGE_PROGRESS)) {
                Download download = intent.getParcelableExtra("download");
                if (download.getProgress() == 100) {
                    if (mApkDownloadProgressDialog != null) {
                        // When the loop exits, set the dialog content to a string that equals "Done"
                        if (mApkDownloadProgressDialog.isShowing())
                            mApkDownloadProgressDialog.setContent(getString(R.string.file_download_complete));

                        if (mApkDownloadProgressDialog.isShowing()) //dismiss dialog
                            mApkDownloadProgressDialog.dismiss();
                        mApkDownloadProgressDialog = null;
                    }

                } else
                    showApkDownloadProgressDialog(download);
            }
        }
    };

    /**
     * register apk download progress broadcast receiver
     */
    private void registerReceiver() {
        try {
            LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(INTENT_MESSAGE_PROGRESS);
            bManager.registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * show apk download progress dialog
     *
     * @param download
     */
    private void showApkDownloadProgressDialog(Download download) {
        Log.v(TAG, "testing download apk: " + download.getProgress() + "\n" +
                String.format("Downloaded (%d/%d) MB", download.getCurrentFileSize(), download.getTotalFileSize()));

        if (download.getProgress() == 0)
            mApkDownloadProgressDialog = null;
        Log.v(TAG, "testing download ....000");
        //update progress if there is progress dialog
        if (mApkDownloadProgressDialog != null) {
            Log.v(TAG, "testing download ....1111");
            if (mApkDownloadProgressDialog.isShowing())
                mApkDownloadProgressDialog.setProgress(download.getProgress());
            Log.v(TAG, "testing download ....2222");
            return;
        }

        Log.v(TAG, "testing download ....33333");
        //create new progress dialog
        if (((Activity) mContext).hasWindowFocus()//important line
                && download.getProgress() == 0) {

            // If the showMinMax parameter is true, a min/max ratio will be shown to the left of the seek bar.
            boolean showMinMax = false;

            mApkDownloadProgressDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.apk_download_title)
                    .content(R.string.please_wait)
                    .negativeText("Cancel")
                    .cancelable(false)
                    .positiveText("Download in Background")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            //null, so that dialog wont appear on revisit of actiivty
                            mApkDownloadProgressDialog = null;
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            DownloadService.shouldContinue = false;
                            Constants.IS_UPDATE_CANCEL_CLICKED = true;

                        }
                    })
                    .progress(false, 100, showMinMax)
                    .show();
        }
    }


    /**
     * check latest apk version name
     */
    private void request_latest_apk_version_check() {
        if (mCheckForUdpate)
            show_progress_dialog("Checking update...");
        Observable<Response<ApkDownloadResponse>> call = mApiInterface.check_latest_apk_version();
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<ApkDownloadResponse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {


                    }

                    @Override
                    public void onNext(Response<ApkDownloadResponse> response) {
                        try {
                            dissmiss_dialog();
                            if (!response.isSuccessful())
                                return;

                            ApkDownloadResponse apkDownloadResponse = response.body();
                            if (VersionManager.checkVersion(mContext, apkDownloadResponse.getVersion().trim()))
                                if (mCheckForUdpate)
                                    start_apk_download_service(apkDownloadResponse);
                                else {
                                    if (!apkDownloadResponse.getVersion().equalsIgnoreCase(VersionManager.getSkipVersion()))
                                        show_install_apk_dialog_for_wifi(apkDownloadResponse);
                                }
                            else {
                                if (mCheckForUdpate)//show no new update dialog
                                    new MaterialDialog.Builder(mContext)
                                            .title("Update!")
                                            .content("New update not available. Thank you.")
                                            .positiveText("Dismiss")
                                            .cancelable(true)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();


                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * download apk if wifi
     * show confirm dialog, if mobile data
     */
    private void show_install_apk_dialog_for_wifi(ApkDownloadResponse apkDownloadResponse) {
        if (Connectivity.isConnectedWifi(mContext) && PrefsHelper.get_autoupdate_status())
            //if wifi, download the apk
            start_apk_download_service(apkDownloadResponse);
        else
            //if data, show confirm dialog
            new MaterialDialog.Builder(mContext)
                    .title("New Update Available!")
                    .content("Do you want to update now ?")
                    .positiveText("Update Now")
                    .negativeText("Skip This Version")
                    .neutralText("Not Now")
                    .btnStackedGravity(GravityEnum.START)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            start_apk_download_service(apkDownloadResponse);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            VersionManager.saveSkipVersion(apkDownloadResponse.getVersion());
                            dialog.dismiss();
                        }
                    }).onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            }).show();

    }


    /**
     * check for storage permission
     *
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            checkApkDownloadServiceRunning();
            //resume tasks needing this permission
        }
    }


    /**
     * check if taptarget view is already shown or not
     * <p>
     * true- first time
     */
    public boolean is_view_first_time(String className) {
        String data = Prefs.getString(Constants.PREFS.PREFS_TAP_TARGET_VIEWS, "");

        try {
            if (!TextUtils.isEmpty(data)) {
                TypeToken<List<TapTargetData>> token = new TypeToken<List<TapTargetData>>() {
                };
                List<TapTargetData> tapTargetViews = new Gson().fromJson(data, token.getType());
                for (TapTargetData tapTargetData : tapTargetViews)
                    if (tapTargetData.getClassName().equalsIgnoreCase(className))
                        return false;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        try {
            save_taptargetview_class_name(className, data);
        } finally {
            return true;
        }


    }

    private void save_taptargetview_class_name(String className, String data) {
        List<TapTargetData> tapTargetDatas = new ArrayList<>();
        if (!TextUtils.isEmpty(data)) {
            TypeToken<List<TapTargetData>> token = new TypeToken<List<TapTargetData>>() {
            };
            tapTargetDatas = new Gson().fromJson(data, token.getType());
            tapTargetDatas.add(new TapTargetData(className));
        } else {
            TapTargetData tapTargetData = new TapTargetData(className);
            tapTargetDatas.add(tapTargetData);
        }

        Prefs.putString(Constants.PREFS.PREFS_TAP_TARGET_VIEWS, new Gson().toJson(tapTargetDatas).toString());

    }

    /**
     * show tap targetview on multiple views
     *
     * @param tapTargetDatas
     */
    public void showTapTargetView(List<TapTargetData> tapTargetDatas, String className) {

        if (is_view_first_time(className)
                && tapTargetDatas != null
                && tapTargetDatas.size() > 0) {
            List<TapTarget> tapTargets = new ArrayList<>();
            for (TapTargetData data : tapTargetDatas)
                tapTargets.add(TapTarget.forView(findViewById(data.getViewId()), data.getTitle())
                        .dimColor(android.R.color.holo_red_dark)
                        .outerCircleColor(R.color.yellow)
                        .targetCircleColor(R.color.colorPrimary)
                        .cancelable(false)
                        .drawShadow(true)
                        .textColor(android.R.color.black));

            final TapTargetSequence sequence = new TapTargetSequence(this)
                    .targets(tapTargets)
                    .listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            // Yay
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            // Boo
                        }

                    });

            sequence.start();
        }
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

    /**
     * add backbutton in toolbar
     *
     * @param toolbar
     */
    public void setNavBackButton(Toolbar toolbar) {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        try {
            //change back arrow color
            final Drawable upArrow = ContextCompat.getDrawable(mContext, R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(get_Color(R.color.white), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //set back arrow click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getSupportFragmentManager().popBackStack();
                else
                    finish();
            }
        });

    }
}
