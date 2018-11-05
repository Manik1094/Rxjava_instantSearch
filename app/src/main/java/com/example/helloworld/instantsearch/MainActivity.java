package com.example.helloworld.instantsearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    Button btn_local_search , btn_remote_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btn_local_search = findViewById(R.id.btn_local_search);
        btn_remote_search = findViewById(R.id.btn_remote_search);

        btn_local_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this , LocalSearchActivity.class);
                startActivity(intent);
            }
        });

        btn_remote_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , RemoteSearchActivity.class);
                startActivity(intent);
            }
        });


    }
}
