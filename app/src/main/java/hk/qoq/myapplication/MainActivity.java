package hk.qoq.myapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static hk.qoq.myapplication.R.drawable.noti_icon;

public class MainActivity extends Activity {
    Button btnHit;
    Button btnStop;
    TextView resultText;
    ProgressDialog pd;
    JSONObject avilJson;
    Map<String, List<String>> avilMap;
    Switch switch1;
    final int requestCode = 1;
    Intent intent;
    final int flags = PendingIntent.FLAG_UPDATE_CURRENT;
    PendingIntent pendingIntent ; // 取得PendingIntent
    Timer timer;
    TimerTask timerTask;
    //String jsonUrl = "http://192.168.0.2:8080/resources/json/avail2.json";
    String jsonUrl = "https://reserve.cdn-apple.com/HK/zh_HK/reserve/iPhone/availability.json";
    // Sets an ID for the notification
    int mNotificationId = 999;

    Notification.Builder mBuilder ;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private void parseAvailJson() {
        StoreCode[] storeArr = StoreCode.values();
        ModelCode[] modelArr = ModelCode.values();
        avilMap = new HashMap<String, List<String>>();
        for (StoreCode storeCode : storeArr) {
            if (avilMap.get(storeCode.getDesc()) == null) {
                avilMap.put(storeCode.getDesc(), new ArrayList<String>());
            }
            try {
                JSONObject storeObj = avilJson.getJSONObject(storeCode.name());
                if (storeObj != null) {
                    for (ModelCode mc : modelArr) {
                        if (storeObj.getString(mc.getModel()).equals("ALL")) {
                            avilMap.get(storeCode.getDesc()).add(mc.name());
                        }
                    }
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        resultText.setText("No Response");
                    }
                });
            }
        }
        boolean available = false;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,List<String>> entry : avilMap.entrySet()) {
            List<String> list = entry.getValue();
            if(list.size()==0) {continue;}
            available = true;
            for(String model :list) {
                sb.append(model).append(" is available in ").append(entry.getKey()).append(System.getProperty("line.separator"));
            }
        }
        if(!available) {return;}
        runOnUiThread(new Runnable(){public void run(){resultText.setText("Have response");}});
        mBuilder.setContentText(sb.append("end").toString());
        //mBuilder.setStyle(inboxStyle);
        intent.putExtra("avil status",sb.toString());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags);
        mBuilder.setStyle(new Notification.BigTextStyle().bigText(sb.toString())).setContentIntent(pendingIntent);
        Notification notif = mBuilder.build();
        notif.defaults |= (Notification.DEFAULT_SOUND |  Notification.DEFAULT_VIBRATE);
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);
        btnHit = (Button) findViewById(R.id.button);
        btnStop = (Button) findViewById(R.id.button2);
        switch1= (Switch) findViewById(R.id.switch1);
        resultText = (TextView) findViewById(R.id.textView3);
        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                timerTask = new myTimerTask();
                timer.schedule(timerTask,0,3000);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                resultText.setText("Not running");
                //if(timerTask.cancel()==false) timerTask=new myTimerTask();

            }
        });
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    jsonUrl = "http://192.168.0.2:8080/resources/json/avail2.json";
                }
                else {
                    jsonUrl =  "https://reserve.cdn-apple.com/HK/zh_HK/reserve/iPhone/availability.json";
                }
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        intent = new Intent(this,NotifyActivity.class);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hk.qoq.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hk.qoq.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        timer.cancel();
        client.disconnect();
    }

    private class myTimerTask extends TimerTask{
        @Override
        public void run()
        {
            //new JsonTask().execute("https://reserve.cdn-apple.com/HK/zh_HK/reserve/iPhone/availability.json");}},0,3000);
            mBuilder = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.noti_icon)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setContentIntent(pendingIntent);
            new JsonTask().execute(jsonUrl);}

    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            //pd = new ProgressDialog(MainActivity.this);
            //pd.setMessage("Please wait");
            //pd.setCancelable(false);
            //pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                JSONObject jObject = new JSONObject(buffer.toString());
                avilJson = jObject;
                parseAvailJson();
                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
                updateResultText(e.getClass().getName());
            } catch (IOException e) {
                e.printStackTrace();
                updateResultText(e.getClass().getName());
            } catch (JSONException e) {
                e.printStackTrace();
                updateResultText(e.getClass().getName());
            } catch (Exception e) {
                e.printStackTrace();
                updateResultText(e.getClass().getName());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void updateResultText(final String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    resultText.setText(result);
                }
            });
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
               /* if (pd.isShowing()) {
                    pd.dismiss();
                }*/
            //Log.i("Json received", result);
        }
    }

}


