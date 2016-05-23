package com.simplistic.dannyboi.creditrecharge;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.text.InputFilter;


public class MainActivity extends AppCompatActivity {

    static private String NUMBERS_FILE = "numbers";
    private String to_send;
    private String[] previouslyRechargedNumbers;
    private EditText recipientInput, amountInput;
    private RadioButton sendToDigicel, sendToLime;
    private InputFilter recipientMask;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipientMask = new InputFilter.LengthFilter(7);

        recipientInput = (EditText) findViewById(R.id.credit_recipient_input);
        recipientInput.setFilters(new InputFilter[]{recipientMask});
        amountInput = (EditText) findViewById(R.id.credit_amount_input);

        sendToDigicel = (RadioButton)findViewById(R.id.vendor_choice1);
        sendToLime = (RadioButton)findViewById(R.id.vendor_choice2);

        View.OnClickListener sendCreditListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = (View) v.getParent();
                Boolean sendingViaDigicel = sendToDigicel.isChecked();
                Boolean sendingViaLime = sendToLime.isChecked();

                String recipient = recipientInput.getText().toString().trim();
                String amount = amountInput.getText().toString().trim();

                if (sendingViaDigicel) {
                    to_send = buildDigicelRechargeString(recipient, amount);
                    Log.d("sendCreditUSSD",
                            to_send);
                    Intent sendCreditIntent = new Intent(Intent.ACTION_CALL);
                    sendCreditIntent.setData(Uri.parse("tel:" + to_send));
                    startActivity(sendCreditIntent);
                }

                if (sendingViaLime) {
                    to_send = buildLimeRechargeString(recipient, amount);
                    Log.d("sendCreditSMS",
                            to_send);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("124", null, to_send, null, null);
                }

                new eraseEditTexts().execute(recipientInput, amountInput);

            }
        };

        Button sendCredit = (Button) findViewById(R.id.send_credit);

        sendCredit.setOnClickListener(sendCreditListener);

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

