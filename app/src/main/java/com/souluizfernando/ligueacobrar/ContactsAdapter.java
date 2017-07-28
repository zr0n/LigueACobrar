package com.souluizfernando.ligueacobrar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lgbt on 28/07/2017.
 */

public class ContactsAdapter extends SimpleCursorAdapter{
    MainActivity mContext;
    String operadora;
    public ContactsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        mContext = (MainActivity) context;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(view != null){
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
        super.bindView(view, context, cursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return super.getView(position, convertView, parent);
    }
    private String getLocalCode(String phoneNumber){
        String patternString = "^55([0-9]{2})[0-9]{8,9}$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(phoneNumber);
        if(matcher.find()){
            String localCode = matcher.group(1);
            return localCode;
        }
        return "";
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
}
