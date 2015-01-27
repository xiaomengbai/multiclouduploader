package edu.gmu.xiaomengbai.multiclouduploader;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class FileBrowseActivity extends ActionBarActivity {

    final static String TAG = "FileBrowseActivity";

    final private static String[] errorInfo = {"No files found!"};
    final private static String rootDir = "/sdcard";

    private File root;
    private ArrayList<File> fileList;
    private FileAdapter mFileAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browse);

        final ListView lv = (ListView)findViewById(R.id.files_listview);

        root = new File(rootDir);
        fileList = getFiles(root);
        Log.d(TAG, "Initial files: " + fileList);
        mFileAdapter = new FileAdapter(this, fileList);

        lv.setAdapter(mFileAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File fileSelected = fileList.get(position);
                Log.d(TAG, "File selected: " + fileSelected.getAbsolutePath());

                if(fileSelected.isDirectory()){
                    fileList = getFiles(fileSelected);
                    mFileAdapter.clear();
                    mFileAdapter.addAll(fileList);
                }else{
                    Intent intent = new Intent(FileBrowseActivity.this, MainActivity.class);
                    intent.setData(Uri.fromFile(fileSelected));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_browse, menu);
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


    private ArrayList<File> getFiles(File dir){
        ArrayList<File> files = new ArrayList<File>();
        File listFiles[] = dir.listFiles();

        files.add(dir);
        if(dir.compareTo(root) != 0)
            files.add(dir.getParentFile());

        if(listFiles != null)
            files.addAll(Arrays.asList(listFiles));

        return files;
    }

}
