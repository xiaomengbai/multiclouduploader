package edu.gmu.xiaomengbai.multiclouduploader;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    final static String TAG = "MainActivity";
    final static int REQUEST_GETFILE = 0;


    private TextView filename_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filename_tv = (TextView)findViewById(R.id.filename_textview);
        final Button browse_btn = (Button)findViewById(R.id.browse_button);
        final Button upload_btn = (Button)findViewById(R.id.upload_button);

        browse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "browse button clicked!");
                filename_tv.setText("browse button clicked!");

                startActivityForResult(
                        new Intent(MainActivity.this, FileBrowseActivity.class), REQUEST_GETFILE);
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Upload the seleted file to some cloud
                Log.d(TAG, "upload button clicked!");
                filename_tv.setText("upload button clicked!");
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_GETFILE:
                if(resultCode == RESULT_OK) {
                    File fileSelected = new File(data.getData().getPath());
                    filename_tv.setText(fileSelected.getAbsolutePath());
                }
                break;
        }
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

    private File parseFileFromIntent(Intent intent){
        return new File(intent.getData().getPath());
    }
}
