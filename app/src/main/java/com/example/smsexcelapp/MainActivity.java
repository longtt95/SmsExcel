package com.example.smsexcelapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    Workbook workbook;
    Adapter adapter;
    ProgressBar progressBar;
    TextView txt_filePicker;
    Button btn_filePicker,btnSend;
    Intent myFileIntent;
    TextView wait;
    RecyclerView recyclerView;
    List<String> storyPhoneNumber,storyContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_filePicker = (TextView)findViewById(R.id.textView);
        btn_filePicker = (Button) findViewById(R.id.button);
        btnSend = (Button) findViewById(R.id.button2);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        wait = findViewById(R.id.wait);

        wait.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        btn_filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, 10);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    String path = data.getData().getPath();
                    txt_filePicker.setText(path);
                    WorkbookSettings ws = new WorkbookSettings();
                    ws.setGCDisabled(true);
                    File file = new File(path);
                    if (file != null) {
                        wait.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        try {
                            workbook = Workbook.getWorkbook(file);
                            Sheet sheet = workbook.getSheet(0);
                            //Cell[] row = sheet.getRow(1);
                            //text.setText(row[0].getContents());
                            for(int i = 0;i< sheet.getRows();i++){
                                Cell[] row = sheet.getRow(i);
                                storyPhoneNumber.add(row[0].getContents());
                                storyContent.add( row[1].getContents());
                            }
                            showData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (BiffException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Loi khong doc duoc file", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void showData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this,storyPhoneNumber,storyContent);
        recyclerView.setAdapter(adapter);
    }
}
