package ch.zhaw.init.touchexplore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ch.zhaw.init.touchexplore.utils.AppPref;


public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    public Context context;

    public AppPref appPref;

    Dialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPref=AppPref.getInstance(context);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context =context;
    }
    public boolean isOnline() {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public void hideKeyboard() {
        View view = ((Activity) context).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    public void gotoActivity(Class className, Bundle bundle, boolean isClearStack)
    {
        Intent intent=new Intent(context,className);

        if(bundle!=null)
            intent.putExtras(bundle);

        if(isClearStack)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }
    public boolean hasPermission(String[] permissions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }


    public void showToast(String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }



    public  void showLoading()
    {
        if(dialog!=null)
            hideLoading();

        if(dialog==null)
        {
            dialog=new Dialog(context);
            if(dialog.getWindow()!=null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_bar);
        }
        if(!dialog.isShowing())
            dialog.show();
    }
    public  void hideLoading()
    {
        if(dialog!=null && dialog.isShowing())
        {
            dialog.dismiss();
        }
    }


}
