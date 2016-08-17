package com.simplistic.dannyboi.creditrecharge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.InputFilter;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.jar.Attributes;


public class MainActivity extends AppCompatActivity {

    static private String NUMBERS_FILE = "numbers";
    private String to_send;
    private EditText recipientInput, amountInput;
    private InputFilter recipientMask;
    private TextView serviceProvider;

    class eraseEditTexts extends AsyncTask<View, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(View... params) {
            final View[] inputs = params;


            for (final View input: inputs) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((EditText)input).setText("");
                    }
                });
            }

            return true;
        }
    }

    class CreditRequestTask extends AsyncTask<Attributes, Void, Attributes>{
        @Override
        protected Attributes doInBackground(Attributes... params){
            params[0].putValue("carrier" , determineCarrier().toUpperCase());

            return params[0];
        }

        protected void onPostExecute(Attributes result){
            if (result.getValue("carrier").equals("DIGICEL")){
                to_send = buildDigicelRechargeString(result.getValue("recipient"),
                        result.getValue("amount"));
                Log.d("sendCreditUSSD", to_send);
                Intent sendCreditIntent = new Intent(Intent.ACTION_CALL);
                sendCreditIntent.setData(Uri.parse("tel:" + to_send));
                if (isCallPermitted()) {
                    startActivity(sendCreditIntent);
                }
                else{
                    findViewById(R.id.send_credit).setEnabled(false);
                    showPermissionDeniedToast();
                }

            }
            else if (result.getValue("carrier").equals("FLOW") ||
                    result.getValue("carrier").equals("LIME")){
                to_send = buildLimeRechargeString(result.getValue("recipient"),
                        result.getValue("amount"));
                Log.d("sendCreditSMS", to_send);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("+124",
                        null, to_send, null, null);
            }
            else {
                showNoSupportCarrierToast();
            }

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceProvider = (TextView) findViewById(R.id.credit_vendor);
        serviceProvider.setText(determineCarrier(this.getBaseContext()));

        recipientMask = new InputFilter.LengthFilter(7);

        recipientInput = (EditText) findViewById(R.id.credit_recipient_input);
        recipientInput.setFilters(new InputFilter[]{recipientMask});
        amountInput = (EditText) findViewById(R.id.credit_amount_input);

        Button sendCredit = (Button) findViewById(R.id.send_credit);

        sendCredit.setOnClickListener(new CreditRechargeOnClick());
    }

    private boolean isCallPermitted(){
        int callPermissionGranted = PermissionChecker
                .checkSelfPermission(getApplicationContext(),
                        Intent.ACTION_CALL);

        return callPermissionGranted == PermissionChecker.PERMISSION_GRANTED;

    }

    private void showPermissionDeniedToast(){
        final Context context = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        getResources().getText(R.string.check_permission),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showNoSupportCarrierToast(){
        final Context context = this;
        this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(context,
                        getResources().getText(R.string.carrier_no_support),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    class CreditRechargeOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Attributes parameters = new Attributes();


            parameters.putValue("recipient",
                                ((EditText)findViewById(R.id.credit_recipient_input))
                                        .getText()
                                        .toString());
            parameters.putValue("amount",
                    ((EditText)findViewById(R.id.credit_amount_input))
                            .getText()
                            .toString());
            new CreditRequestTask().execute(parameters);

            new eraseEditTexts().execute(recipientInput, amountInput);
        }
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

    private String determineCarrier(){
        Context context = this.getApplicationContext();
        return determineCarrier(context);
    }

    private String determineCarrier(Context context){
        // Determine Carrier and return
        // the string which represents
        // that carrier

        String carrierName;

        TelephonyManager telephonyInfo =
                (TelephonyManager)context
                        .getSystemService(Context.TELEPHONY_SERVICE);

        carrierName = telephonyInfo.getSimOperatorName();
        Log.i("CarrierDetermination", carrierName);

        return carrierName;
    }

    @Override
    protected void onPause(){
        super.onPause();
        //Clear the text fields whenever the activity is paused
        EditText toWhom = (EditText)this.findViewById(R.id.credit_recipient_input);
        EditText howMuch = (EditText)this.findViewById(R.id.credit_amount_input);

        toWhom.setText("");
        howMuch.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

