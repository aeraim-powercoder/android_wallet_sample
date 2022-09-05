package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    protected final static String TAG = "SamsungWalletSample";
    protected static final String HOST = "https://api-us3.mpay.samsung.com";
    protected static final String PATH = "wallet/cmn/v2.0/device/available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Executors.newSingleThreadExecutor().submit(() -> {
            final String modelName = Build.MODEL;
            final String countryCode = "US"; // (optional) country code (ISO_3166-2)
            final String serviceType = "WALLET"; // (mandatory, fixed) for Samsung Wallet
            final String partnerCode = "[PARTNER ID]"; // (mandatory)
            try {
                boolean isWalletSupported = checkWalletSupported(
                        modelName, countryCode, serviceType, partnerCode);
                String msg = String.format(
                        "query for model(%s), countryCode(%s), serviceType(%s), partnerCode(%s) / wallet supported? (%s)",
                        modelName,
                        countryCode,
                        serviceType,
                        partnerCode,
                        isWalletSupported);
                Log.d(TAG, msg);
            } catch (Exception e) {
                // failed to check due to some reasons
                Log.e(TAG, e.getMessage(), e);
            }
        });


        URL impression_url = null;
        try {
            impression_url = new URL("'IMPRESSION URL'");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpsURLConnection myConnection =
                    (HttpsURLConnection) impression_url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ButtonClick1(View v){
        URL click_url = null;
        try {
            click_url = new URL("'CLICK URL'");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpsURLConnection myConnection =
                    (HttpsURLConnection) click_url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent urlintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://a.swallet.link/atw/v1/[Partner Code]]#Clip?cdata=CDATA"));
        startActivity(urlintent);
    }

    public boolean checkWalletSupported(@NonNull String modelName, @Nullable String countryCode,
                                        @NonNull String serviceType, @NonNull String partnerCode) throws Exception {
        if (modelName == null || modelName.isEmpty()) {
            Log.e(TAG, "model name is mandatory parameter");
            throw new Exception("something went wrong (failed to get device model name)");
        }
        if (serviceType == null || serviceType.isEmpty()) {
            Log.e(TAG, "serviceType is mandatory parameter");
            throw new Exception("something went wrong (failed to get device serviceType)");
        }
        if (partnerCode == null || partnerCode.isEmpty()) {
            Log.e(TAG, "partnerCode is mandatory parameter");
            throw new Exception("something went wrong (failed to get device partnerCode)");
        }
        String urlString = makeUrl(modelName, countryCode, serviceType);
        Log.i(TAG, "urlString: " + urlString);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("partnerCode", partnerCode);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            Log.i(TAG, "responseCode: " + responseCode);
            BufferedReader bufferedReader;
            if (responseCode == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String inputline;
            while ((inputline = bufferedReader.readLine()) != null) {
                Log.i(TAG, inputline);
                sb.append(inputline);
            }
            connection.disconnect();
            bufferedReader.close();
            // parse result
            JSONObject jsonObject = new JSONObject(sb.toString());
            String resultCode = jsonObject.getString("resultCode");
            String resultMessage = jsonObject.getString("resultMessage");
            if ("0".equals(resultCode) && "SUCCESS".equals(resultMessage)) {
                return jsonObject.getBoolean("available");
            } else {
                throw new Exception("something went wrong, resultCode(" + resultCode + "), resultMessage(" + resultMessage + ")");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new Exception("something went wrong (IOException), " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new Exception("something went wrong, receive wrong formatted response, " + e.getMessage());
        }
    }
    protected String makeUrl(@NonNull String modelName, @Nullable String countryCode,
                             @NonNull String serviceType) {
        StringBuilder sb = new StringBuilder();
        sb.append(HOST).append('/');
        sb.append(PATH);
        sb.append('?').append("serviceType").append('=').append(serviceType);
        sb.append('&').append("modelName").append('=').append(modelName);
        if (countryCode != null && !countryCode.isEmpty()) {
            sb.append('&').append("countryCode").append('=').append(countryCode);
        }
        return sb.toString();
    }
}