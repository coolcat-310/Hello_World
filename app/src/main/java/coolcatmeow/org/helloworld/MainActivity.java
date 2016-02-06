package coolcatmeow.org.helloworld;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private Button ourButton;
    private TextView ourMessage;
    private int numTimesClicked =0;

    private final String MEETUP_API =
            "54.173.94.86";

    private TextView  statusView;

    private JSONObject loadUrl(String location)
    {
        LinkedList<String> lines = new LinkedList<String>();

        // Download URL to array of lines
        try {
            // Open URL
            URL url = new URL(location);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            // Open buffered reader
            InputStream is  = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buf = new BufferedReader(isr);

            // Read data into lines array
            String line;
            while ((line = buf.readLine()) != null)
                lines.add(line);

            // Close keep-alive connections
            conn.disconnect();
        }
        catch (MalformedURLException e) {
            Log.d("SfvLug", "Invalid URL: " + location);
        }
        catch (IOException e) {
            Log.d("SfvLug", "Download failed for: " + location);
        }

        // Load text into JSON Object
        String text = TextUtils.join("", lines);
        try {
            return new JSONObject(text);
        } catch (JSONException e) {
            Log.d("SfvLug", "Invalid JSON Object: " + text);
            return null;
        }
    }


    /* Display the meeting status to the user */
    private void setStatus(JSONObject status)
    {

        String text = null;

        // Extract info from JSON object and format output messages
        try {
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-DD'T'hh:mm:ss");

            JSONObject event = status.getJSONArray("results").getJSONObject(0);
            JSONObject venue = event.getJSONObject("venue");

            String name  = event.getString("name");
            String where = venue.getString("name") + " " + venue.getString("city");
            long   stamp = event.getLong("time") + event.getLong("utc_offset");

            String when  = fmt.format(new Date(stamp));

            text = "What:  " + name  + "\n" +
                    "When:  " + when  + "\n" +
                    "Where: " + where + "\n";
        }
        catch (NullPointerException e) {
            text = "Error downloading meeting status";
        }
        catch (JSONException e) {
            text = "Error parsing meeting status: " + e.toString();
        }
        Log.d("SfvLug", "Next meeting: " + this.toString());
    }


    /* Trigger a refresh of the meeting status */
    private void loadStatus()
    {
        // Set URI location
        String location = this.MEETUP_API;

        // Notify the user that we're loading
        statusView.setText("Loading..");

        // Update status in a background thread
        //
        // In Android, we normally cannot access the network from the main
        // thread; doing so would cause the user interface to freeze during
        // data transfer.
        //
        // Again, android provides several ways around this, here we use an
        // AsyncTask which lets us run some code in a background thread and
        // then update the user interface once the background code has
        // finished.
        //
        // The first Java Generic parameter are:
        //   1. String     - argument for doInBackground, from .execute()
        //   2. Void       - not used here, normally used for progress bars
        //   3. JSONObject - the return type from doInBackground which is
        //                   passed to onPostExecute function.
        new AsyncTask<String, Void, JSONObject>() {

            // Called from a background thread, so we don't block the user
            // interface. Using AsyncTask synchronization is handled for us.
            protected JSONObject doInBackground(String... args) {
                // Java passes this as a variable argument array,
                // but we only use the first entry.
                return MainActivity.this.loadUrl(args[0]);
            }

            // Called once in the main thread once doInBackground finishes.
            // This is executed in the Main thread once again so that we can
            // update the user interface.
            protected void onPostExecute(JSONObject status) {
                MainActivity.this.setStatus(status);
                Toast.makeText(MainActivity.this, "Load complete", Toast.LENGTH_SHORT).show();
            }

        }.execute(location);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This is content on the screen
        setContentView(R.layout.activity_main);
        //Link Button to Textview
        ourButton = (Button) findViewById(R.id.button);
        ourMessage = (TextView) findViewById(R.id.textView);


        //Listen on the button to be click
        View.OnClickListener ourOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numTimesClicked++;
                String result = "Then button got tapped " + numTimesClicked + " time";
                if(numTimesClicked != 1){
                    result += "s";
                }
                ourMessage.setText(result);
            }
        };

            ourButton.setOnClickListener(ourOnClickListener);

    }

        


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Add menu_main on screen
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
            Toast toastMessage = Toast.makeText(this, "Text value is now " + ourMessage.getText(), Toast.LENGTH_LONG);
            toastMessage.show();
            numTimesClicked = 0;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
