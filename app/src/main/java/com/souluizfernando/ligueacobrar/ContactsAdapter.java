package com.souluizfernando.ligueacobrar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lgbt on 28/07/2017.
 */

public class ContactsAdapter extends ArrayAdapter<String[]>{
    MainActivity mContext;
    String operadora;
    ArrayList<String[]> data;
    Context main;
    int resourceId;

    public ContactsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<String[]> objects, Activity activity) {
        super(context, resource, objects);
        data = objects;
        main = context;
        resourceId = resource;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup element;
        LayoutInflater inflater = LayoutInflater.from(main);
        if(convertView == null){
            element = (LinearLayout) inflater.inflate(R.layout.contact_list_item, parent, false);
        }
        else{
            element = (LinearLayout) convertView;
        }
        if(element == null){
            return super.getView(position, element, parent);
        }
        element.removeAllViews();
        setOnClickListener(element);
        inflater.inflate(R.layout.list_text_1, element);
        inflater.inflate(R.layout.list_text_2, element);
        TextView nameView = element.findViewById(R.id.name);
        TextView numberView = element.findViewById(R.id.number);
        String[] rowData = data.get(position);
        nameView.setText(rowData[0]);
        numberView.setText(rowData[1]);
        return element;
    }
    private String getLocalCode(String phoneNumber){
        return ((MainActivity) main).getLocalCode(phoneNumber);
    }
    //remove space or simbols
    private String parseNumber(String number){
        return number.replaceAll("\\W", "");
    }
    private String insertACobrarWithDDD(String number){
        String localCode = getLocalCode(number);
        return number.replaceAll("^[0-9]{4}", "90"+operadora+localCode);
    }
    private String insertLocalACobrar(String number){
        return number.replaceAll("^[0-9]{4}", "9090");
    }

    private void setOnClickListener(View view) {
        final MainActivity mContext = (MainActivity) main;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOperadora(mContext.operadoraCode);
                TextView name = (TextView) view.findViewById(R.id.name);
                TextView number = (TextView) view.findViewById(R.id.number);
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(
                        Context.TELEPHONY_SERVICE);
                String myPhoneNumber = tm.getLine1Number();
                myPhoneNumber = parseNumber(myPhoneNumber);
                String myLocalCode = getLocalCode(myPhoneNumber);

                String contactPhoneNumber = number.getText().toString();
                contactPhoneNumber = parseNumber(contactPhoneNumber);
                String contactLocalCode = getLocalCode(contactPhoneNumber);
                if(contactLocalCode.length() == 0){
                    if(contactPhoneNumber.length() < 10){
                        contactPhoneNumber = "55"+myLocalCode+contactPhoneNumber;
                    }
                    else if(contactPhoneNumber.length() < 12){
                        contactPhoneNumber = "55"+contactPhoneNumber;
                    }
                    contactLocalCode = getLocalCode(contactPhoneNumber);

                }
                String callNumber;
                if(contactLocalCode.equals(myLocalCode)){
                    callNumber = insertLocalACobrar(contactPhoneNumber);
                }
                else{
                    Log.d("Not match: ", contactLocalCode + " == " + myLocalCode);
                    callNumber = insertACobrarWithDDD(contactPhoneNumber);
                }
                Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + callNumber));
                try{
                    mContext.startActivity(intent);
                }
                catch(SecurityException e){
                    Log.d("Perm Missing Call phone", e.getMessage().toString());
                }
            }
        });
    }
}
