package com.simplistic.dannyboi.creditrecharge.Services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.PermissionChecker;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.simplistic.dannyboi.creditrecharge.MainActivity;
import com.simplistic.dannyboi.creditrecharge.R;

public class  CreditRequestService extends IntentService {
    static String recipient;
    static String amount;
    static Context context;
    Handler ui;
    static int USSD_PERMISSION;
    static int SMS_PERMISSION;

    public CreditRequestService(){
        super("CreditRequest");
        ui = new Handler(Looper.getMainLooper());
    }

    class CreditRequestThread extends Thread{
        @Override
        public void run() {
            Looper.prepare();
            Log.d("CreditRequestService", "Starting Service");
            String carrier = determineCarrier(context);
            String sendReq;
            switch(carrier){
                case "DIGICEL":
                    sendReq = buildDigicelRechargeString(recipient, amount);
                    Intent sendCreditIntent = new Intent(Intent.ACTION_CALL)
                            .setData(Uri.parse("tel:" + sendReq));
                    sendCreditIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (USSD_PERMISSION == PermissionChecker.PERMISSION_GRANTED){
                        startActivity(sendCreditIntent);
                    }
                    else{
                        showPermissionDeniedToast(context);
                    }
                    break;
                case "LIME":
                case "FLOW":
                    sendReq = buildLimeRechargeString(recipient, amount);
                    SmsManager smsManager = SmsManager.getDefault();
                    if (SMS_PERMISSION == PermissionChecker.PERMISSION_GRANTED) {
                        smsManager.sendTextMessage("+124",
                                null, sendReq, null, null);
                    }
                    else{
                        showPermissionDeniedToast(context);
                    }
                    break;
                default:
                    showNoSupportCarrierToast(context);
                    break;
            }
            stopSelf();

            super.run();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onHandleIntent(Intent intent) {
        recipient = (String) intent.getCharSequenceExtra("recipient");
        amount = (String) intent.getCharSequenceExtra("amount");
        context = getApplicationContext();

        USSD_PERMISSION = context.checkCallingOrSelfPermission("android.permission.CALL_PHONE");
        SMS_PERMISSION = context.checkCallingOrSelfPermission("android.permission.SEND_SMS");

        new CreditRequestThread().start();
    }

    private String buildDigicelRechargeString(String recipient, String amount){
        StringBuilder creditRechargeUSSD = new StringBuilder();
        creditRechargeUSSD.append("*165*970*0506*876");
        creditRechargeUSSD.append(recipient);
        creditRechargeUSSD.append('*');
        creditRechargeUSSD.append(amount);
        creditRechargeUSSD.append(Uri.encode("#"));

        return creditRechargeUSSD.toString();
    }

    private String buildLimeRechargeString(String recipient, String amount){
        StringBuilder creditRechargeSMS = new StringBuilder();
        creditRechargeSMS.append("TOPUP 5163 876");
        creditRechargeSMS.append(recipient);
        creditRechargeSMS.append(' ');
        creditRechargeSMS.append(amount);

        return creditRechargeSMS.toString();
    }

    private String determineCarrier(Context context){
        // Determine Carrier and return
        // the string which represents
        // that carrier

        String carrierName;

        try {
            TelephonyManager telephonyInfo =
                    (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = telephonyInfo.getSimOperatorName();
            Log.i("CarrierDetermination", carrierName);
            return carrierName;
        }
        catch (NullPointerException error){
            showNoSupportCarrierToast(context);
        }

        return "null";
    }

    private void showPermissionDeniedToast(final Context context){
        ui.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        getResources().getText(R.string.check_permission),
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showNoSupportCarrierToast(final Context context){
        ui.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        getResources().getText(R.string.carrier_no_support),
                        Toast.LENGTH_LONG).show();

            }
        });

    }
}
