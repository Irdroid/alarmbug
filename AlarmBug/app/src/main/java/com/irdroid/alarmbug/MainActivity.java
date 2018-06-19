package com.irdroid.alarmbug;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends Activity implements HeadsetActionButtonReceiver.Delegate {
private static final String TAG = "MainActivity";
    private Handler mHandler = new Handler();
    private static boolean security=false;
    private static boolean runned = false;
    private static final String MSG_KEY = "yo";

    static View root;
    static TextView tv;
    static String email;
@Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.activity_main);
    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    email = SP.getString("email", "info@irdroid.com");
    root = findViewById(R.id.root);
    tv = (TextView) findViewById(R.id.textView);
    tv.setText("Open/Close to Arm!");
    blink();


}

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainactivity, menu);

        return true;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public boolean onOptionsItemSelected(MenuItem item) {



        int id = item.getItemId();


        // Handle item selection
        switch (id) {



            case R.id.settings:

                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);

            default:
                return true;
        }
    }

 public void onResume() {
    HeadsetActionButtonReceiver.delegate = this;
    HeadsetActionButtonReceiver.register(this);
     SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     email = SP.getString("email", "info@irdroid.com");
    super.onResume();
}
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_POWER )

        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                event.startTracking();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void blink(){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                int timeToBlink = 700;    //in milissegunds
                try{Thread.sleep(timeToBlink);}catch (Exception e) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        TextView txt = (TextView) findViewById(R.id.textView);
                        if(txt.getVisibility() == View.VISIBLE){
                            txt.setVisibility(View.INVISIBLE);
                        }else{
                            txt.setVisibility(View.VISIBLE);
                        }
                        blink();
                    }
                });
            }
        }).start();
    }
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
          //  toast ("long press volup");
            mHandler.postAtTime(volup,
                    SystemClock.uptimeMillis() + 250);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            mHandler.postAtTime(voldown,
                    SystemClock.uptimeMillis() + 250);
         //   toast ("long press vol down");

            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }
    private Runnable voldown= new Runnable()
    {
        @TargetApi(Build.VERSION_CODES.CUPCAKE)
        public void run()
        {

            security=true;
        //    System.out.println("KeyDOWN");
            root.setBackgroundColor(Color.GREEN);
            tv.setText("SYSTEM ARMED!");
    if(!runned){
        new LongOperation().execute("");
    }
            runned =true;



            try {


            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mHandler.postAtTime(this, SystemClock.uptimeMillis() +
                    10);

        }
    };
    private Runnable volup = new Runnable()
    {
        public void run()
        {


            System.out.println("KeyUP");



            try {


            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mHandler.postAtTime(this, SystemClock.uptimeMillis() +
                    10);

        }
    };
    private final Handler comdata = new Handler() {

        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString(MSG_KEY);
            System.out.println(string);
            root.setBackgroundColor(Color.RED);
            tv.setText("Please setup email!");
        }
    };
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        mHandler.removeCallbacks(volup);
        mHandler.removeCallbacks(voldown);
        security=false;
        root.setBackgroundColor(Color.RED);
        tv.setText("SECURITY BREACH");
        runned=false;

        new LongOperation().execute("");

        if((event.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
             //   myVib.vibrate(20);
             //   Code = Slide_up;
                System.out.println("KeyUP");
            //    Transmit();
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
              //  myVib.vibrate(20);
                System.out.println("KeyDOWN");

              //  Code = Slide_down;
               // Transmit();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    private void sendmail(){
        final String username = "";
        final String password = "";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "irdroid.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            javax.mail.Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("alarmbug@irdroid.com"));
            message.setRecipients(javax.mail.Message.RecipientType.TO,
                    InternetAddress.parse(email));

            if (security){
                message.setSubject("Message from the Irdroid Door Bug! > Door closed");
                message.setText("Dear owner,"
                        +"\r\n\nThe Door was closed!");
            }else{
                message.setSubject("Message from the Irdroid Door Bug! > Door opened");
                message.setText("Dear Owner,"
                        +"\r\n\nDoor was opened!");
            }




            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("NewApi")
    private class LongOperation extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            if(email.equals("info@irdroid.com")){
                android.os.Message msg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(MSG_KEY, "Please setup email first!");
                msg.setData(bundle);
                comdata.sendMessage(msg);
            }else{
                sendmail();
            }

            return "Executed";
        }


        protected void onPostExecute(String result) {
          //  TextView txt = (TextView) findViewById(R.id.output);
          //  txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }


        protected void onPreExecute() {}


        protected void onProgressUpdate(Void... values) {}
    }

@Override public void onPause() {
    HeadsetActionButtonReceiver.unregister(this);
    super.onPause();
}

    @Override
    public void onMediaButtonSingleClick() {
System.out.println("Single");


    }

    @Override
    public void onMediaButtonDoubleClick() {
        System.out.println("Double");
    }
}

