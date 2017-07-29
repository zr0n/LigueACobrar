package com.souluizfernando.ligueacobrar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.getLong;

public class MainActivity extends FragmentActivity
    implements AdapterView.OnItemSelectedListener{
    final int MY_PERMISSION_TO_READ_CONTACTS = 666;
    final String[] PERMISSIONS_NEEDED = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE
    };
    final static String GET_ALL_CONTACTS = "1 = 1";
    static String myPhoneNumber;
    public String operadoraCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(VerifyPermissions()) {
            myPhoneNumber = getMyPhoneNumber();
            setupListeners();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
               String[] permissions, int[] grantResults){
        if(requestCode == MY_PERMISSION_TO_READ_CONTACTS){
            Boolean granted = false;
            if(grantResults.length > 0){
                granted = true;
                for(int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        granted = false;
                    }
                }
            }
            if(granted){
                setupListeners();
                return;
            }
        }
        if(VerifyPermissions()){
            setupListeners();
        }
    }
    private void setupListeners(){
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operadoras, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        final EditText search = (EditText) findViewById(R.id.editText);
        final ContactsFragment cf = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        cf.setSearchTerm(GET_ALL_CONTACTS); //get All Contacts
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchTerm = search.getText().toString();
                cf.setSearchTerm(searchTerm);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private Boolean VerifyPermissions(){
        for(int i = 0; i < PERMISSIONS_NEEDED.length; i++){
            String permission = PERMISSIONS_NEEDED[i];
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        PERMISSIONS_NEEDED,
                        MY_PERMISSION_TO_READ_CONTACTS);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String operadora = adapterView.getSelectedItem().toString();
        Pattern pattern = Pattern.compile("^\\(([0-9]+)\\)");
        Matcher matcher = pattern.matcher(operadora);
        if(matcher.find()){
            operadoraCode = matcher.group(1);
        }
        else{
            operadoraCode = "21";
        }
        Log.d("Operadora Selecionada: ", operadoraCode);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public static class ContactsFragment extends Fragment {
        private ListView mContactsList;

        protected static final String QUERY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?":
                ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
        private static String mSearchTerm;

        public ContactsFragment(){

        }

        @Override
        public View getView() {
            final View v  = super.getView();
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name = (TextView) v.findViewById(R.id.name);
                    TextView number = (TextView) v.findViewById(R.id.number);
                    Log.d(name.getText().toString(),number.getText().toString());
                }
            });

            return v;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstaceState){
            return inflater.inflate(R.layout.contact_list_view, container, false);
        }
        public void setSearchTerm(String term){
            mSearchTerm = term;
            if(term.length() == 0){
                return;
            }
            Thread searchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                        String cName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                                ContactsContract.Contacts.DISPLAY_NAME;
                        String[] projection = new String[]{
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone._ID,
                                cName

                        };
                        String selection;
                        if(mSearchTerm.equals(GET_ALL_CONTACTS) || mSearchTerm.length() == 0){
                            selection = GET_ALL_CONTACTS;
                        }
                        else{
                            selection = like(ContactsContract.Contacts.DISPLAY_NAME, mSearchTerm);
                        }
                        Log.d("Selection: ", selection);
                        ContentResolver cr = getContext().getContentResolver();
                        Cursor cursor = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                projection,
                                selection,
                                null,
                                orderBy(cName));
                        String cNumber = ContactsContract.CommonDataKinds.Phone.NUMBER;
                        mContactsList = (ListView) getActivity().findViewById(R.id.mainFragment);
                        String[] columns = new String[]{cName, cNumber};
                        ArrayAdapter mCursorAdapter = new ContactsAdapter(
                                getActivity(),
                                android.R.layout.simple_list_item_1,
                                filterDuplicates(cursor, columns),
                                getActivity());
                        mContactsList.setAdapter(mCursorAdapter);
                }
                private String orderBy(String name){
                    return name + " ASC";
                }
                private String like(String name, String term){
                    return name + " LIKE '%"+term+"%'";
                }
            });
            searchThread.run();
        }
    }
    protected static ArrayList<String[]> filterDuplicates(Cursor c, String[] columns){
        ArrayList<String> names = new ArrayList();
        ArrayList<String> numbers = new ArrayList();
        ArrayList<String[]> contactsArr = cursorToArrayList(c, columns);
        ArrayList<String[]> uniques = new ArrayList<>();
        for(int i = 0; i < contactsArr.size(); i++){
            String currName = contactsArr.get(i)[0];
            String currNumber = parseNumber(contactsArr.get(i)[1]);
            Boolean unique = true;
            for(int j = 0; j < names.size(); j++){
                if(names.get(j).equals(currName)&& numbers.get(j).equals(currNumber)){
                    unique = false;
                }
            }
            if(unique){
                names.add(currName);
                numbers.add(currNumber);
                uniques.add(new String[]{currName, contactsArr.get(i)[1]});
            }
        }
        return uniques;
    }
    private static ArrayList<String[]> cursorToArrayList(Cursor c, String[] columns){
        ArrayList<String[]> converted = new ArrayList<String[]>();
        while(c.moveToNext()){
            String[] row = new String[2];
            row[0] = c.getString(c.getColumnIndex(columns[0]));
            row[1] = c.getString(c.getColumnIndex(columns[1]));
            converted.add(row);
        }
        return converted;
    }
    private static String parseNumber(String number){
        String myPhoneNumber = MainActivity.myPhoneNumber;
        String myLocal = getLocalCode(myPhoneNumber);
        String parsed = number.replaceAll("\\W", ""); //only numbers or letters
        if(parsed.length() < 10){
            parsed = "55"+myLocal+parsed;
        }
        else if(parsed.length() < 12){
            parsed = "55"+parsed;
        }
        return parsed;
    }
    protected String getMyPhoneNumber(){
        TelephonyManager tm = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }
    protected static String getLocalCode(String phoneNumber){
        String patternString = "^55([0-9]{2})[0-9]{8,9}$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(phoneNumber);
        if(matcher.find()){
            String localCode = matcher.group(1);
            return localCode;
        }
        return "";
    }
}
