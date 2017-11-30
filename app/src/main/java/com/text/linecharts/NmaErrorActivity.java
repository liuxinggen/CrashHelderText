package com.text.linecharts;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 程序崩溃用户提示界面
 *
 * @author ht
 */
public class NmaErrorActivity extends BaseActivity {
    /**
     * 消息
     */
    private String mMessage;
    /**
     * 提示语
     */
    private String mIdea;
    /**
     * 选择按钮
     */
    private CheckBox mRreportCB;
    /**
     * 描述输入框
     */
    private EditText mUserIdeaET;
    /**
     * 提交进度条
     */
    private ProgressDialog mProgressDialog;
    /**
     * 消息提示语
     */
    private Message mMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_error);
        mMessage = this.getIntent().getStringExtra("msg");
        mUserIdeaET = (EditText) this.findViewById(R.id.et_user_idea);
        mRreportCB = (CheckBox) this.findViewById(R.id.ckb_report);
    }

    public void forClick(View v) {
        if (v.getId() == R.id.btn_report) {
            report(1);
        } else if (v.getId() == R.id.btn_close) {
            report(-1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.quit();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
            switch (msg.what) {
                case 1:
                    restart();
                    break;
                case -1:
                    quit();
                    break;
            }
            if (msg.obj != null && (Boolean) msg.obj) {
                Toast.makeText(NmaErrorActivity.this, "错误报告提交成功!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 发送错误报告
     */
    private void report(final int code) {
        mMsg = new Message();
        mMsg.what = code;
        mIdea = this.mUserIdeaET.getText().toString();
        if (mRreportCB.isChecked()) {
            if (mUserIdeaET.getText() == null
                    || mUserIdeaET.getText().toString().equals("")) {
                mUserIdeaET.requestFocus();
                mUserIdeaET.setError("请您输入问题描述!");
            } else {
                if (isNetConected()) {
                    mProgressDialog = ProgressDialog.show(NmaErrorActivity.this,
                            "", "正在提交数据,请稍候...", true);
                    //TODO:向服务器提交奔溃日志
                    mMsg.obj = true;
                    handler.sendMessage(mMsg);

                } else {
                    Toast.makeText(NmaErrorActivity.this,
                            "网络连接错误，请检查您的手机是否联网!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (isNetConected()) {
                //用户点击关闭应用按钮后，如果它处于联网状态，也应该把错误日志上传
                //TODO:向服务器提交奔溃日志
                handler.sendMessage(mMsg);
            } else {
                handler.sendMessage(mMsg);
            }
        }

    }

    /**
     * 重启
     */
    private void restart() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Intent mIntent = new Intent();
            ComponentName comp = new ComponentName(
                    "com.text.linecharts",
                    "com.text.linecharts.MainActivity");
            mIntent.setComponent(comp);
            mIntent.setAction("android.intent.action.MAIN");
            this.startActivity(mIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.finish();
    }

    /**
     * 退出
     */
    protected void quit() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.restartPackage(getPackageName());
        finish();
        CompleteQuit.getInstance().exitAll(true);
    }

    /**
     * 判断手机是否联网
     *
     * @return
     */
    private boolean isNetConected() {
        try {
            ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}

