package com.splice.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Ksf on 3/13/2016.
 */
public class LoadingDialog extends ProgressDialog{

    public LoadingDialog(Context context) {
        super(context);
        setMessage("Please wait...");
        setCancelable(false);
    }
}
