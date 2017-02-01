package cl.telios.parkea.Helpers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by rubro on 01-02-2017.
 */

public class WebRequest {
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    //Constructor with no parameter
    public WebRequest() {
    }
    /**
     * Making web service call
     *
     * @url - url to make request
     * @requestmethod - http request method
     */
    /*public String makeWebServiceCall(String url, int requestmethod) {
        return this.makeWebServiceCall(url, requestmethod);
    }*/
    /**
     * Making service call
     *
     * @url - url to make request
     * @requestmethod - http request method
     * @params - http request params
     */
    public String makeWebServiceCall(String urladdress, int requestmethod) {
        URL url;
        String response = "";
        Log.d("Develop", "URL: "+urladdress);
        try {
            url = new URL(urladdress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            /*if (requestmethod == POST) {
                conn.setRequestMethod("POST");
            } else if (requestmethod == GET) {
                conn.setRequestMethod("GET");
            }*/
            //conn.setRequestMethod("GET"); // OJO CON ESTA LINEA, CAMBIAR MAS ADELANTE
            /*if (params != null) {
                //Log.d("Develop", "Params NOT NULL");
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first)
                        first = false;
                    else
                        result.append("&");
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                writer.write(result.toString());
                writer.flush();
                writer.close();
                os.close();
            }
            else{
                //Log.d("Develop", "Params NULL");
            }*/
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}