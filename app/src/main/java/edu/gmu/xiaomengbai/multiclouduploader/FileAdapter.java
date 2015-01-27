package edu.gmu.xiaomengbai.multiclouduploader;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {
    private ArrayList<File> files;
    private Context context;

    public FileAdapter(Context context, ArrayList<File> items) {
        super(context, R.layout.file_item, items);
        this.context = context;
        files = items;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View row = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        TextView tv = (TextView) row.findViewById(R.id.filename);

        File file = files.get(position);
        String fileName;

        if (position == 0)
            fileName = ".";
        else if (position == 1) {
            File firstFile = files.get(0);
            if (file.compareTo(firstFile.getParentFile()) == 0)
                fileName = "..";
            else
                fileName = file.getName();
        } else
            fileName = file.getName();

        if (file.isDirectory()) {
            if (fileName.compareTo(".") != 0 &&
                    fileName.compareTo("..") != 0)
                fileName += "/";
            tv.setTextColor(Color.argb(0xff, 0, 0, 0xff));
        }
        tv.setText(fileName);

        return row;
    }
}
