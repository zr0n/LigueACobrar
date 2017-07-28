package com.souluizfernando.ligueacobrar;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by lgbt on 28/07/2017.
 */

public class ContactsAdapter extends SimpleCursorAdapter{
    public ContactsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(view != null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name = (TextView) view.findViewById(R.id.name);
                    TextView number = (TextView) view.findViewById(R.id.number);
                    String aCobrator = parseNumber(number.getText().toString());
                    Log.d(name.getText().toString(), aCobrator);
                }
            });
        }
        super.bindView(view, context, cursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return super.getView(position, convertView, parent);
    }
    private String parseNumber(String number){
        return number.replaceAll("^(\\+[0-9]{1,3}\\s[0-9]{2,3})", "9090");
    }
}
