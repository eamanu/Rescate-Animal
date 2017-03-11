package com.eamanu.rescateanimal;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by eamanu on 2/20/17.
 */

public class BaseActivity extends AppCompatActivity{
    private static final String TAG_DIALOG_FRAGMENT = "tagDialogFragment";

    protected void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev == null) {
            ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(message);
            fragment.show(ft, TAG_DIALOG_FRAGMENT);
        }

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                Toast.makeText(BaseActivity.this, "Error en la descarga de información", Toast.LENGTH_SHORT).show();
            }
        };

        Handler pdCancel = new Handler();
        pdCancel.postDelayed(progressRunnable, 10000);
    }

    protected void dismissProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev != null) {
            ft.remove(prev).commit();
        }
    }

    private Fragment getExistingDialogFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENT);
    }
}
