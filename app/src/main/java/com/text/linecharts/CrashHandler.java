package com.text.linecharts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 类名：com.junze.testapkupdate
 * 时间：2017/11/24 19:49
 * 描述：
 * 修改人：
 * 修改时间：
 * 修改备注：
 *
 * @author Liu_xg
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    private static final String PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + File.separator
            + File.separator;

    private static final String FILE_NAME = "crashText";
    private static final String FILE_NAME_SUFFIX = ".log";


    private static CrashHandler sInstance = new CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultCrasHandler;

    private Context mContext;

    public CrashHandler() {

    }

    public static CrashHandler getsInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mDefaultCrasHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    /**
     * 这是最关键的函数，当程序中有未捕获的异常，系统将会自动调用这个方法，
     * thread为出现未捕获异常的线程，ex为为未捕获的异常，有了这个ex，我们就可以得到
     * 异常信息了
     *
     * @param thread
     * @param ex
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        //导出异常信息到SD卡中
        try {
            dumpExceptionToSDCard(ex);
            //上传异常信息到服务器，便于开发人员分析日志从而解决bug
            uploadExceptionToServer();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        new Thread() {
            @Override
            public void run() {

                Looper.prepare();
                try {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction("com.error.text.NOTIFY_ERROR");
                    mContext.startActivity(intent);
                    CompleteQuit.getInstance().exitAll(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();

        ex.printStackTrace();
        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (mDefaultCrasHandler != null) {
            mDefaultCrasHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡中
        if (!Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.i(TAG, "dumpExceptionToSDCard: SD卡不存在");
                return;
            }
        }
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(current));
        File file = new File(PATH + FILE_NAME);
        //先删除之前的异常信息
        if (file.exists()) {
            DeleteFile(file);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        StackTraceElement[] stackTrace = ex.getStackTrace();

        String error_text = "错误：" + ex.toString() + "  \n  ";
        for (int i = 0; i < stackTrace.length; i++) {
            error_text += stackTrace[i].getFileName() + " class:"
                    + stackTrace[i].getClassName() + " method:"
                    + stackTrace[i].getMethodName() + " line:"
                    + stackTrace[i].getLineNumber() + "  \n  ";
        }
        try {
            PrintWriter pw = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(file + File.separator
                                    + time + FILE_NAME_SUFFIX)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "dumpExceptionToSDCard: " + e);
        }
    }

    /**
     * 写入手机的基本信息
     *
     * @param pw
     */
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //Android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print('_');
        pw.println(Build.VERSION.SDK_INT);

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //CUP架构
        pw.print("CUP ABI: ");
        pw.println(Build.CPU_ABI);

        //奔溃发生时间
        pw.print("CURRENT DATE: ");
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        pw.println(dateFormat.format(currentDate));

    }

    private void uploadExceptionToServer() {
        //TODO Upload Exception Message To Your Web Server


    }

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public void DeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                DeleteFile(f);
            }
            file.delete();
        }
    }
}
