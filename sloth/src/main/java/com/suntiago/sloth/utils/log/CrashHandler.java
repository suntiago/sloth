package com.suntiago.sloth.utils.log;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.suntiago.sloth.utils.file.StorageHelper;
import com.suntiago.sloth.utils.file.StorageManagerHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author Jeremy
 */
public class CrashHandler implements UncaughtExceptionHandler {

  public static final String TAG = "CrashHandler";
  public static final String BROADCAST_ACTION = "com.viroyal.permission.crash_log_update";
  //private LocalBroadcastManager mLocalBroadcastManager;

  //系统默认的UncaughtException处理类
  private UncaughtExceptionHandler mDefaultHandler;
  //CrashHandler实例
  private static CrashHandler INSTANCE = new CrashHandler();
  //程序的Context对象
  private Context mContext;
  //用来存储设备信息和异常信息
  private Map<String, String> infos = new HashMap<String, String>();

  //用于格式化日期,作为日志文件名的一部分
  private DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

  private String packageName = "com.viroyal.base";

  /**
   * 保证只有一个CrashHandler实例
   */
  private CrashHandler() {
  }

  StorageHelper mStorageHelper = StorageManagerHelper.getStorageHelper();

  /**
   * 获取CrashHandler实例 ,单例模式
   */
  public static CrashHandler getInstance() {
    return INSTANCE;
  }

  public String getCrashLogPath() {
    return getSavePath();
  }


  private String getSavePath() {
    return mStorageHelper.getSDCardPath() + "log/crash/";
  }


  /**
   * 初始化
   * use init(Context context)  instead
   *
   * @param context
   */
  @Deprecated
  public void init(Context context, String com, String pkgId) {
    mContext = context;
    //获取系统默认的UncaughtException处理器
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    //mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    //设置该CrashHandler为程序的默认处理器
    Thread.setDefaultUncaughtExceptionHandler(this);
    if (!TextUtils.isEmpty(pkgId)) {
      this.packageName = pkgId;
    }
  }

  public void init(Context context) {
    mContext = context;
    //获取系统默认的UncaughtException处理器
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    //mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    //设置该CrashHandler为程序的默认处理器
    Thread.setDefaultUncaughtExceptionHandler(this);
    String pkgId = context.getPackageName().replace(".", "_") + "_";
    if (!TextUtils.isEmpty(pkgId)) {
      this.packageName = pkgId;
    }

  }

  /**
   * 当UncaughtException发生时会转入该函数来处理
   */
  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    if (!handleException(ex) && mDefaultHandler != null) {
      //如果用户没有处理则让系统默认的异常处理器来处理
      mDefaultHandler.uncaughtException(thread, ex);
    } else {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        Log.e(TAG, "error : ", e);
      }
      //退出程序
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(1);
    }
  }

  /**
   * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
   *
   * @param ex
   * @return true:如果处理了该异常信息;否则返回false.
   */
  private boolean handleException(Throwable ex) {
    if (ex == null) {
      return false;
    }
    //使用Toast来显示异常信息
    new Thread() {
      @Override
      public void run() {
        Looper.prepare();
        Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
        Looper.loop();
      }
    }.start();
    //收集设备参数信息
    collectDeviceInfo(mContext);

    //打印异常log,以供分析调试
    ex.printStackTrace();
    //保存日志文件
    String path = saveCrashInfo2File(ex);

    Slog.d(TAG, BROADCAST_ACTION);
    Intent intent = new Intent();
    intent.setAction(BROADCAST_ACTION);
    intent.putExtra("path", path);
    intent.putExtra("pkgName", mContext.getPackageName());
    mContext.sendBroadcast(intent);

    return true;
  }

  /**
   * 收集设备参数信息
   *
   * @param ctx
   */
  public void collectDeviceInfo(Context ctx) {
    try {
      PackageManager pm = ctx.getPackageManager();
      PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
      if (pi != null) {
        String versionName = pi.versionName == null ? "null" : pi.versionName;
        String versionCode = pi.versionCode + "";
        infos.put("versionName", versionName);
        infos.put("versionCode", versionCode);
      }
    } catch (NameNotFoundException e) {
      Log.e(TAG, "an error occured when collect package info", e);
    }
    Field[] fields = Build.class.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        infos.put(field.getName(), field.get(null).toString());
        Log.d(TAG, field.getName() + " : " + field.get(null));
      } catch (Exception e) {
        Log.e(TAG, "an error occured when collect crash info", e);
      }
    }
  }

  /**
   * 保存错误信息到文件中
   *
   * @param ex
   * @return 返回文件名称, 便于将文件传送到服务器
   */
  private String saveCrashInfo2File(Throwable ex) {

    StringBuffer sb = new StringBuffer();
    for (Map.Entry<String, String> entry : infos.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      sb.append(key + "=" + value + "\n");
    }

    Writer writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    ex.printStackTrace(printWriter);
    Throwable cause = ex.getCause();
    while (cause != null) {
      cause.printStackTrace(printWriter);
      cause = cause.getCause();
    }
    printWriter.close();
    String result = writer.toString();
    sb.append(result);
    try {
      long timestamp = System.currentTimeMillis();
      String time = formatter.format(new Date());
      String pkgNamepath = packageName.replace(".", "_");
      String fileName = "crash_" + pkgNamepath + "_" + time + "_" + timestamp + ".log";
      String path = getCrashLogPath();
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        File dir = new File(path);
        if (!dir.exists()) {
          dir.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(path + fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
      }
      return path + fileName;
    } catch (Exception e) {
      Log.e(TAG, "an error occured while writing file...", e);
    }
    return null;
  }
}