package cn.nubia.fulleclientdemo.base;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

import cn.nubia.fulleclientdemo.FullApplication;

/**
 * Created by gbq on 2017-4-20.
 */

public class PackageUtil {

    public static int getPid(String packageName) {
        ActivityManager am = (ActivityManager) FullApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();
        int pid = -1;
        for (
                ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
            if (amProcess.processName.equals(packageName)) {
                pid = amProcess.pid;
                break;
            }
        }
        return pid;
    }
}
