package com.modcom.otptry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        OtpReceivedInterface{
    private static final int CREDENTIAL_PICKER_REQUEST = 1;
    EditText txt;
    TextView resend;
    GoogleApiClient mGoogleApiClient;
    MySMSBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    EditText inputMobileNumber;
    TextView txt1,txt2,txt3,txt4;
    OtpEditText inputOtp;
    Button btnGetOtp, btnVerifyOtp;
    ConstraintLayout layoutInput, layoutVerify;
    CardView card1,card2,card3,card4;
    ConstraintLayout otpsmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resend=findViewById(R.id.textView6);

        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        appSignatureHelper.getAppSignatures();

        initViews();
        // init broadcast receiver
          mSmsBroadcastReceiver = new MySMSBroadcastReceiver();
        //set google api client for hint request

        mSmsBroadcastReceiver.setOnOtpListeners(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(mSmsBroadcastReceiver, intentFilter);
        // get mobile number from phone
        getHintPhoneNumber();
        btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Call server API for requesting OTP and when you got success start
                // SMS Listener for listing auto read message lsitner
                fetchContacts();
                startSMSListener();

            }
        });




    }
    private void initViews() {
        inputMobileNumber = findViewById(R.id.editTextInputMobile);
        btnGetOtp = findViewById(R.id.buttonGetOTP);
        btnVerifyOtp = findViewById(R.id.buttonVerify);
        layoutInput = findViewById(R.id.getOTPLayout);
        layoutVerify = findViewById(R.id.verifyOTPLayout);
        txt1=findViewById(R.id.textView1);
        txt2=findViewById(R.id.textView2);
        txt3=findViewById(R.id.textView3);
        txt4=findViewById(R.id.textView4);
        card1=findViewById(R.id.cardView);
        card2=findViewById(R.id.cardView2);
        card3=findViewById(R.id.cardView3);
        card4=findViewById(R.id.cardView4);
        otpsmall=findViewById(R.id.constraintLayout);

    }

    @Override public void onOtpReceived(String otp) {
        Toast.makeText(this, "Otp Received " + otp.substring(otp.indexOf(":") +1).substring(0, 7).charAt(1), Toast.LENGTH_LONG).show();
//        inputOtp.setText(otp);
//        inputOtp.setText((otp.substring(otp.indexOf(":") +1)).substring(0, 7));
        resend.setVisibility(View.GONE);


        txt1.setText(String.valueOf(otp.substring(otp.indexOf(":") +1).substring(0, 7).charAt(1)));
        card1.setCardElevation(10.0f);
        txt2.setText(String.valueOf(otp.substring(otp.indexOf(":") +1).substring(0, 7).charAt(2)));
        card2.setCardElevation(10.0f);
      txt3.setText(String.valueOf(otp.substring(otp.indexOf(":") +1).substring(0, 7).charAt(3)));
        card3.setCardElevation(10.0f);
        txt4.setText(String.valueOf(otp.substring(otp.indexOf(":") +1).substring(0, 7).charAt(4)));
        card4.setCardElevation(10.0f);
    }
    @Override public void onOtpTimeout() {
        Toast.makeText(this, "Time out, please resend", Toast.LENGTH_LONG).show();
    }

    public void startSMSListener() {
        countdown();
        SmsRetrieverClient mClient = SmsRetriever.getClient(this);
        Task<Void> mTask = mClient.startSmsRetriever();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {

                Toast.makeText(MainActivity.this, "SMS Retriever starts", Toast.LENGTH_LONG).show();
            }
        });
        mTask.addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void getHintPhoneNumber() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();


        PendingIntent intent = Credentials.getClient(getApplicationContext()).getHintPickerIntent(hintRequest);
        try
        {
            startIntentSenderForResult(intent.getIntentSender(), CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0,new Bundle());
        }
        catch (IntentSender.SendIntentException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK)
        {
            // Obtain the phone number from the result
            Credential credentials = data.getParcelableExtra(Credential.EXTRA_KEY);
            inputMobileNumber.setText(credentials.getId().substring(1));
            countdown();
            //get the selected phone number
//Do what ever you want to do with your selected phone number here


        }
        else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE)
        {
            // *** No phone numbers available ***
            Toast.makeText(getApplicationContext(), "No phone numbers found", Toast.LENGTH_LONG).show();
        }


    }








    private void fetchContacts() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.url),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println("maxi"+response);



                        try {
                            Log.d("JSON", response);



                            JSONObject eventObject = new JSONObject(response);
                            JSONArray projectNameArray = eventObject.getJSONArray("data");
                            String error_status=eventObject.getString("status");
                            System.out.println(error_status);


                        } catch (Exception e) {
                            Log.d("Tag",e.getMessage());


                        }




                        // adding contacts to contacts list


                        // refreshing recycler view

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json

                Toast.makeText(getApplicationContext(), "check connection"+ error , Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();


                String sql = "SELECT `services`.`amount`,`services`.`serviceid` as `id`,`services`.`name`," +
                        "`services`.`category`,`mainservices`.`name` as `catname` from `services` inner join " +
                        "`mainservices` on `mainservices`.`id`=`services`.`category` where `services`.`type`='single' and `mainservices`.`name`='servicing'";

                params.put("action", "get_data");

                params.put("number",inputMobileNumber.getText().toString());


                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public void countdown(){

        new CountDownTimer(50000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only
                NumberFormat f = new DecimalFormat("00");
                long hour = (millisUntilFinished / 3600000) % 24;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                resend.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                resend.setText("Resend");
                resend.setTextColor(getResources().getColor(R.color.link));
                resend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fetchContacts();
                        startSMSListener();

                    }
                });

            }
        }.start();

    }





}