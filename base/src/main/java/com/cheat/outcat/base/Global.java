package com.cheat.outcat.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Global {

    private static volatile Context context;
    private static boolean isDebug = false;

    public final static void init(Context ctx) {
        setContext(ctx);
    }

    public final static Context getContext() {
        if (context == null) {
            throw new RuntimeException("Global's Context is NULL, have your Application in manifest "
                    + "subclasses BaseApplication or Call 'Global.init(this)' in your own Application ? ");
        }

        return context;
    }

    public final static void setContext(Context context) {
        Global.context = context;

        try {
            ApplicationInfo info = context.getApplicationInfo();
            isDebug = ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            if (isDebug) {
                android.util.Log.w("Wns.Global.Runtime", "DEBUG is ON");
            }
        } catch (Exception e) {
            isDebug = false;
        }
    }


    public static void setDebug(boolean _isDebug) {
        isDebug = _isDebug;
    }

    /**
     * @return
     * @Deprecated Use BuildConfig.DEBUG instead
     */
    public static boolean isDebug() {
        return isDebug;
    }

    /*
     * 下面为 Android.Context 的同名静态方法包装 ↓
     */
    public final static AssetManager getAssets() {
        return getContext().getAssets();
    }

    public final static Resources getResources() {
        return getContext().getResources();
    }

    public final static PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    public final static ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

    public final static Looper getMainLooper() {
        return getContext().getMainLooper();
    }

    public final static Context getApplicationContext() {
        return getContext().getApplicationContext();
    }

    public final static void setTheme(int resid) {
        getContext().setTheme(resid);
    }

    public final static Resources.Theme getTheme() {
        return getContext().getTheme();
    }

    public final static ClassLoader getClassLoader() {
        return getContext().getClassLoader();
    }

    public final static String getPackageName() {
        return getContext().getPackageName();
    }

    public final static ApplicationInfo getApplicationInfo() {
        return getContext().getApplicationInfo();
    }

    public final static String getPackageResourcePath() {
        return getContext().getPackageResourcePath();
    }

    public final static String getPackageCodePath() {
        return getContext().getPackageCodePath();
    }

    public final static SharedPreferences getSharedPreferences(String name, int mode) {
        return getContext().getSharedPreferences(name, mode);
    }

    public final static FileInputStream openFileInput(String name) throws FileNotFoundException {
        return getContext().openFileInput(name);
    }

    public final static Typeface getTypeFaceFromAssets(String fontName) {
        try {
            AssetManager mgr = getAssets();//得到AssetManager
            Typeface tf = Typeface.createFromAsset(mgr, fontName);//根据路径得到Typeface
            return tf;
        } catch (Exception e) {
            return null;
        }
    }

    public final static FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return getContext().openFileOutput(name, mode);
    }

    public final static boolean deleteFile(String name) {
        return getContext().deleteFile(name);
    }

    public final static File getFileStreamPath(String name) {
        return getContext().getFileStreamPath(name);
    }

    public final static String[] fileList() {
        return getContext().fileList();
    }

    public final static File getFilesDir() {
        return getContext().getFilesDir();
    }

    public final static File getExternalFilesDir(String type) {
        return getContext().getExternalFilesDir(type);
    }

    public final static File getObbDir() {
        return getContext().getObbDir();
    }

    public final static File getCacheDir() {
        return getContext().getCacheDir();
    }

    public final static File getExternalCacheDir() {
        return getContext().getExternalCacheDir();
    }

    public final static File getDir(String name, int mode) {
        return getContext().getDir(name, mode);
    }

    public final static SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return getContext().openOrCreateDatabase(name, mode, factory);
    }

    public final static SQLiteDatabase openOrCreateDatabase(String name,
                                                            int mode,
                                                            SQLiteDatabase.CursorFactory factory,
                                                            DatabaseErrorHandler errorHandler) {
        return getContext().openOrCreateDatabase(name, mode, factory, errorHandler);
    }

    public final static File getDatabasePath(String name) {
        return getContext().getDatabasePath(name);
    }

    public final static String[] databaseList() {
        return getContext().databaseList();
    }

    @Deprecated
    public final static Drawable getWallpaper() {
        return getContext().getWallpaper();
    }

    @Deprecated
    public final static Drawable peekWallpaper() {
        return getContext().peekWallpaper();
    }

    @Deprecated
    public final static int getWallpaperDesiredMinimumWidth() {
        return getContext().getWallpaperDesiredMinimumWidth();
    }

    @Deprecated
    public final static int getWallpaperDesiredMinimumHeight() {
        return getContext().getWallpaperDesiredMinimumHeight();
    }

    @Deprecated
    public final static void setWallpaper(Bitmap bitmap) throws IOException {
        getContext().setWallpaper(bitmap);
    }

    @Deprecated
    public final static void setWallpaper(InputStream data) throws IOException {
        getContext().setWallpaper(data);
    }

    @Deprecated
    public final static void clearWallpaper() throws IOException {
        getContext().clearWallpaper();
    }

    public final static void startActivity(Intent intent) {
        getContext().startActivity(intent);
    }

    public final static void startActivities(Intent[] intents) {
        getContext().startActivities(intents);
    }

    public final static boolean startApplicationMarket(Context context) {
        try {
            String packageName = getPackageName();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static void startIntentSender(IntentSender intent,
                                               Intent fillInIntent,
                                               int flagsMask,
                                               int flagsValues,
                                               int extraFlags) throws IntentSender.SendIntentException {
        getContext().startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    public final static void sendBroadcast(Intent intent) {
        getContext().sendBroadcast(intent);
    }

    public final static void sendBroadcast(Intent intent, String receiverPermission) {
        getContext().sendBroadcast(intent, receiverPermission);
    }

    public final static void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        getContext().sendOrderedBroadcast(intent, receiverPermission);
    }

    public final static void sendOrderedBroadcast(Intent intent,
                                                  String receiverPermission,
                                                  BroadcastReceiver resultReceiver,
                                                  Handler scheduler,
                                                  int initialCode,
                                                  String initialData,
                                                  Bundle initialExtras) {
        getContext().sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode,
                initialData, initialExtras);
    }



    public final static Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return getContext().registerReceiver(receiver, filter);
    }

    public final static Intent registerReceiver(BroadcastReceiver receiver,
                                                IntentFilter filter,
                                                String broadcastPermission,
                                                Handler scheduler) {
        return getContext().registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    public final static void unregisterReceiver(BroadcastReceiver receiver) {
        getContext().unregisterReceiver(receiver);
    }

    public final static ComponentName startService(Intent service) {
        return getContext().startService(service);
    }

    public final static boolean stopService(Intent name) {
        return getContext().stopService(name);
    }

    public final static boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return getContext().bindService(service, conn, flags);
    }

    public final static void unbindService(ServiceConnection conn) {
        getContext().unbindService(conn);
    }

    public final static boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        return getContext().startInstrumentation(className, profileFile, arguments);
    }

    public final static Object getSystemService(String name) {
        return getContext().getSystemService(name);
    }

    public final static int checkPermission(String permission, int pid, int uid) {
        return getContext().checkPermission(permission, pid, uid);
    }

    public final static int checkCallingPermission(String permission) {
        return getContext().checkCallingPermission(permission);
    }

    public final static int checkCallingOrSelfPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission);
    }

    public final static void enforcePermission(String permission, int pid, int uid, String message) {
        getContext().enforcePermission(permission, pid, uid, message);
    }

    public final static void enforceCallingPermission(String permission, String message) {
        getContext().enforceCallingPermission(permission, message);
    }

    public final static void enforceCallingOrSelfPermission(String permission, String message) {
        getContext().enforceCallingOrSelfPermission(permission, message);
    }

    public final static void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        getContext().grantUriPermission(toPackage, uri, modeFlags);
    }

    public final static void revokeUriPermission(Uri uri, int modeFlags) {
        getContext().revokeUriPermission(uri, modeFlags);
    }

    public final static int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return getContext().checkUriPermission(uri, pid, uid, modeFlags);
    }

    public final static int checkCallingUriPermission(Uri uri, int modeFlags) {
        return getContext().checkCallingUriPermission(uri, modeFlags);
    }

    public final static int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return getContext().checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    public final static int checkUriPermission(Uri uri,
                                               String readPermission,
                                               String writePermission,
                                               int pid,
                                               int uid,
                                               int modeFlags) {
        return getContext().checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags);
    }

    public final static void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        getContext().enforceUriPermission(uri, pid, uid, modeFlags, message);
    }

    public final static void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        getContext().enforceCallingUriPermission(uri, modeFlags, message);
    }

    public final static void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        getContext().enforceCallingOrSelfUriPermission(uri, modeFlags, message);
    }

    public final static void enforceUriPermission(Uri uri,
                                                  String readPermission,
                                                  String writePermission,
                                                  int pid,
                                                  int uid,
                                                  int modeFlags,
                                                  String message) {
        getContext().enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
    }

    public final static Context createPackageContext(String packageName, int flags)
            throws PackageManager.NameNotFoundException {
        return getContext().createPackageContext(packageName, flags);
    }

    public final static boolean isRestricted() {
        return getContext().isRestricted();
    }
}

