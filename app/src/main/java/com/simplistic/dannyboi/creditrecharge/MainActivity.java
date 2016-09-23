package com.simplistic.dannyboi.creditrecharge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.text.InputFilter;
import android.telephony.TelephonyManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simplistic.dannyboi.creditrecharge.Services.CreditRequestService;

import java.util.concurrent.Executor;
import java.util.jar.Attributes;


public class MainActivity extends AppCompatActivity {

    static private String NUMBERS_FILE = "numbers";
    private String to_send;
    private EditText recipientInput, amountInput;
    private InputFilter recipientMask;
    private TextView serviceProvider;
    private Button sendCredit;

    class eraseEditTexts extends AsyncTask<View, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceProvider = (TextView) findViewById(R.id.credit_vendor);
        serviceProvider.setText(determineCarrier(this));

        recipientMask = new InputFilter.LengthFilter(7);

        recipientInput = (EditText) findViewById(R.id.credit_recipient_input);
        recipientInput.setFilters(new InputFilter[]{recipientMask});
        amountInput = (EditText) findViewById(R.id.credit_amount_input);

        sendCredit = (Button) findViewById(R.id.send_credit);

        View.OnClickListener clickListener = new CreditRechargeOnClick();

        sendCredit.setOnClickListener(clickListener);
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
            ProgressBar pBar = (ProgressBar)((ViewGroup)v.getParent()).findViewById(R.id.progress_bar);
            pBar.setVisibility(View.VISIBLE);
            sendCredit.setEnabled(false);
            Intent serviceableIntent = new Intent(v.getContext(),
                                                  CreditRequestService.class);

            serviceableIntent.putExtra("recipient",
                    ((EditText)findViewById(R.id.credit_recipient_input))
                            .getText()
                            .toString());

            serviceableIntent.putExtra("amount",
                                       ((EditText)findViewById(R.id.credit_amount_input))
                                               .getText()
                                               .toString());
            startService(serviceableIntent);

            new eraseEditTexts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                                   recipientInput, amountInput);
            pBar.setVisibility(View.GONE);
            sendCredit.setEnabled(true);
        }
    }

    private String determineCarrier(Context context){
        // Determine Carrier and return
        // the string which represents
        // that carrier

        String carrierName;

        try{
            TelephonyManager telephonyInfo =
                    (TelephonyManager)context
                            .getSystemService(Context.TELEPHONY_SERVICE);

            carrierName = telephonyInfo.getSimOperatorName();
            Log.i("CarrierDetermination", carrierName);

            if (carrierName.equals("")){
                carrierName = "Network Not Found";
            }

            return carrierName;}
        catch(NullPointerException error){
            showNoSupportCarrierToast();
        }

        return "null";
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
}

