package ch.zhaw.init.touchexplore;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import ch.zhaw.init.touchexplore.utils.AppPref;


public class Base extends AppCompatActivity {

    public Toolbar toolbar;

    protected AppPref appPref;

    Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPref=AppPref.getInstance(this);
    }

    public void setToolbar()
    {
        toolbar=findViewById(R.id.toolBar);
        if(toolbar!=null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
        }
    }

    public void enableBack(boolean isBack)
    {
        if(toolbar!=null)
        {
            if(isBack)
            {
                toolbar.setNavigationIcon(R.drawable.icon_back_arrow);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
            else
            {
                toolbar.setNavigationIcon(null);
            }

        }
    }
    public void setTitle(String title)
    {
        TextView textView=findViewById(R.id.tvTitle);
        if(textView!=null)
            textView.setText(title);
    }

    public boolean isOnline()
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                return cm.getActiveNetworkInfo().isConnectedOrConnecting();
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }
    public  boolean hasPermission( String[] permissions) {

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && permissions!=null)
        {
            for(String permission:permissions)
            {
                if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }
    public void gotoActivity(Class className, Bundle bundle, boolean isClearStack)
    {
        Intent intent=new Intent(this,className);

        if(bundle!=null)
            intent.putExtras(bundle);

        if(isClearStack)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }
    public void gotoActivity(@NonNull Intent intent, boolean isClearStack)
    {
        if(isClearStack)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

    public void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public  void showLoading()
    {
        if(dialog!=null)
            hideLoading();

        if(dialog==null)
        {
            dialog=new Dialog(this);
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
