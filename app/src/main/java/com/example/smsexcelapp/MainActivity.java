package com.example.smsexcelapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Workbook workbook;
    Adapter adapter;
    ProgressBar progressBar;
    TextView txt_filePicker;
    Button btn_filePicker, btnSend;
    Intent myFileIntent;
    TextView wait;
    RecyclerView recyclerView;
    List<String> storyPhoneNumber, storyContent, storyAddress, storyTime;
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    String jsonString = "[{'phoneNumber': '0888717267', 'content': 'Test'}, {'phoneNumber': '0888078898', 'content': 'Test'}]";
    JSONArray jsonArray = new JSONArray();
    String sendingSmsStatus = "SEND";
    String deliverySmsStatus = "DELIVERED";
    PendingIntent send, delivered;
    BroadcastReceiver sendReceiver, deliveredRecevier;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_filePicker = (TextView) findViewById(R.id.textView);
        btn_filePicker = (Button) findViewById(R.id.button);
        btnSend = (Button) findViewById(R.id.button2);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        wait = findViewById(R.id.wait);

        wait.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        storyPhoneNumber = new ArrayList<>();
        storyContent = new ArrayList<>();
        storyAddress = new ArrayList<>();
        storyTime = new ArrayList<>();
        send = PendingIntent.getBroadcast(this, 0,
                new Intent(sendingSmsStatus), 0);
        delivered = PendingIntent.getBroadcast(this, 0, new Intent(
                deliverySmsStatus), 0);
        if (checkPermission(Manifest.permission.SEND_SMS)) {
            btnSend.setEnabled(true);
        } else {
            this.requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_EXTERNAL_STORAGE}, SEND_SMS_PERMISSION_REQUEST_CODE); //Any number
        }


        btnSend.setEnabled(false);

        btn_filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wait.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, 10);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSend.setEnabled(true);
                wait.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        if (checkPermission(Manifest.permission.SEND_SMS) && Patterns.PHONE.matcher(explrObject.getString("phoneNumber")).matches() == true) {
                            SmsManager smsManager = SmsManager.getDefault();

                            String stringPhoneNumber =  explrObject.getString("phoneNumber");
                            String stringName =  explrObject.getString("name");
                            String stringContent =  explrObject.getString("content");
                            String stringTime =  explrObject.getString("dateTime");

                            if (stringContent.length() <= 159) {
                                smsManager.sendTextMessage(stringPhoneNumber, null, stringContent, send, delivered);
                            } else {
                                ArrayList<String> parts = smsManager.divideMessage(stringContent);
                                int numParts = parts.size();
                                smsManager.sendMultipartTextMessage(stringPhoneNumber, null, parts, null, null);
                            }
                            Toast.makeText(MainActivity.this, "Đã gửi tin nhắn " + i + " thành công!", Toast.LENGTH_SHORT).show();
                            Thread.sleep(50000);
                        } else {
                            Toast.makeText(MainActivity.this, "Permission Denined!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    btnSend.setEnabled(false);
                    wait.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException | InterruptedException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //String path = data.getData().getPath();
                    String path = uri.getPath();
                    String urlPath = Environment.getExternalStorageDirectory().toString();
                    if (urlPath.charAt(urlPath.length() - 1) == '/') {
                        path
                                =urlPath + path.substring(path.indexOf(":") + 1);
                    } else {
                        path = urlPath + "/" + path.substring(path.indexOf(":") + 1);
                    }
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    txt_filePicker.setText(path);
                    WorkbookSettings ws = new WorkbookSettings();
                    ws.setGCDisabled(true);
                    File file = new File(path);
                    if (file != null) {
                        wait.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        try {
                            workbook = Workbook.getWorkbook(file);
                            Sheet sheet = workbook.getSheet(0);
                            //Cell[] row = sheet.getRow(1);
                            //text.setText(row[0].getContents());
                            storyPhoneNumber.clear();
                            storyContent.clear();
                            storyAddress.clear();
                            storyTime.clear();
                            for (int i = 0; i < sheet.getRows(); i++) {
                                Cell[] row = sheet.getRow(i);
                                if (Patterns.PHONE.matcher(row[3].getContents()).matches() == true) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("phoneNumber", row[3].getContents());
                                    jsonObject.put("address", row[2].getContents());
                                    jsonObject.put("dateTime", row[4].getContents());

                                    String checkContent = "TTDVVL moi ong(ba) " + row[1].getContents() + " den " + row[2].getContents() + " luc " + row[4].getContents() + " nhan ket qua TCTN. Khi di mang theo CMND va so BHXH ban goc";
                                    if (checkContent.length() > 159) {
                                        checkContent = "TTDVVL moi ong(ba) " + firstLetterWordAndFullLastWord(row[1].getContents()) + " den " + row[2].getContents() + " luc " + row[4].getContents() + " nhan ket qua TCTN. Khi di mang theo CMND va so BHXH ban goc";
                                        if (checkContent.length() > 159) {
                                            jsonObject.put("name", row[1].getContents());
                                            jsonObject.put("content", checkContent.replace("/", "-"));
                                        } else {
                                            jsonObject.put("name", row[1].getContents());
                                            jsonObject.put("content", checkContent.replace("/", "-"));
                                        }
                                    } else {
                                        jsonObject.put("name", row[1].getContents());
                                        jsonObject.put("content", checkContent.replace("/", "-"));
                                    }
                                    jsonArray.put(jsonObject);
                                    storyPhoneNumber.add(row[3].getContents());
                                    //unicodeString is the expected output in unicode
                                    String unicodeString = getUnicodeString(escapeUnicodeText(row[0].getContents()));
                                    //i want to make small u into Capital from unicode String
                                    String resultUnicode = unicodeString.replace("\\u", "\\U");
                                    storyContent.add(checkContent.replace("\\u", "\\U").replace("/", "-"));
                                    storyAddress.add(row[2].getContents().replace("\\u", "\\U"));
                                    storyTime.add(row[4].getContents().replace("\\u", "\\U"));
                                }
                            }
                            showData();
                            adapter.notifyDataSetChanged();
                            btnSend.setEnabled(true);
                            wait.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                        } catch (IOException e) {
                            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                            wait.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        } catch (BiffException e) {
                            Toast.makeText(this, "File excel phải là định dạng *.xls", Toast.LENGTH_LONG).show();
                            wait.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        } catch (JSONException e) {
                            Toast.makeText(this, "Lỗi xử lý", Toast.LENGTH_LONG).show();
                            wait.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi không đọc được file", Toast.LENGTH_SHORT).show();
                        wait.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        sendReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "Tin nhắn đã được gửi", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "Không có dịch vụ nhắn tin trên điện thoại", Toast.LENGTH_LONG).show();
                        break;
                }

            }
        };
        deliveredRecevier = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "Tin nhắn đã được chuyển đi", Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "Lỗi tin nhắn không thể chuyển", Toast.LENGTH_LONG).show();
                        break;
                }

            }
        };
        registerReceiver(sendReceiver, new IntentFilter(sendingSmsStatus));
        registerReceiver(deliveredRecevier, new IntentFilter(deliverySmsStatus));
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(sendReceiver);
        unregisterReceiver(deliveredRecevier);
    }


    private void showData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, storyPhoneNumber, storyContent, storyAddress, storyTime);
        recyclerView.setAdapter(adapter);
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkFilePermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        }else{
            Log.e(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    public String escapeUnicodeText(String input) {

        StringBuilder b = new StringBuilder(input.length());

        java.util.Formatter f = new java.util.Formatter(b);

        for (char c : input.toCharArray()) {
            if (c < 128) {
                b.append(c);
            } else {
                f.format("\\u%04x", (int) c);
            }
        }

        return b.toString();
    }

    public String getUnicodeString(String myString) {
        String text = "";
        try {

            byte[] utf8Bytes = myString.getBytes("UTF8");
            text = new String(utf8Bytes, "UTF8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    // character of each word.
    public String firstLetterWordAndFullLastWord(String str)
    {
        String result = "";

        // Traverse the string.
        boolean v = true;
        for (int i = 0; i < str.length(); i++)
        {
            // If it is space, set v as true.
            if (str.charAt(i) == ' ' && str.lastIndexOf(" ") != i)
            {
                v = true;
            }

            // Else check if v is true or not.
            // If true, copy character in output
            // string and set v as false.
            else if (str.charAt(i) != ' ' && v == true)
            {
                result += (str.charAt(i));
                result += '.';
                v = false;
            }
        }
        result += str.substring(str.lastIndexOf(" ") + 1);

        return result;
    }
}