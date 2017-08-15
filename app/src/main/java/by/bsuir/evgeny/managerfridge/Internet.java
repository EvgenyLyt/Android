package by.bsuir.evgeny.managerfridge;

import android.content.Context;

import java.io.IOException;

public class Internet {
    public static boolean isAvailable(Context context){
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            return (mExitValue==0);
        }
        catch (InterruptedException|IOException ex)
        {
            MessageBox.Show(context,ex.toString(),ex.getMessage());
        }
        return false;
    }
}
