package cl.telios.parkea.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by rubro on 01-02-2017.
 */

public class CheckNetwork {
    private static final String TAG = "Develop";
    public static boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            //Log.d(TAG,"no internet connection");
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                //Log.d(TAG," internet connection available...");
                return true;
            }
            else
            {
                //Log.d(TAG," internet connection");
                return true;
            }

        }
    }
}