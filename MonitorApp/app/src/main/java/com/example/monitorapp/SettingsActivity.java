package com.example.monitorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.InputStream;
import java.net.InetAddress;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextIP, editTextPort;
    private Button btnConnect, btnDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTextIP = findViewById(R.id.editTextIP);
        editTextPort = findViewById(R.id.editTextPort);

        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        init();
    }

    private void init(){
        editTextIP.setText(MainActivity.mAddr);
        editTextPort.setText(MainActivity.mPort+"");

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputNoProblem()){
                    MainActivity.mAddr = editTextIP.getText().toString();
                    MainActivity.mPort = Integer.parseInt(editTextPort.getText().toString());
                    MainActivity.startConnection();
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.endConnection();
            }
        });
    }

    private boolean inputNoProblem(){
        String portStr = editTextPort.getText().toString();
        String addrStr = editTextIP.getText().toString();
        if (!validPort(portStr)){
            Toast.makeText(getApplicationContext(),"Port Input Error",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!validIP(addrStr)){
            Toast.makeText(getApplicationContext(),"IP Input Error",Toast.LENGTH_SHORT).show();
            return false;
        }
        MainActivity.mAddr = addrStr;
        MainActivity.mPort = Integer.parseInt(portStr);
        return true;
    }

    public boolean validPort (String port){
        return !(port == null || port.isEmpty() ||
                !((1023 < Integer.parseInt(port))&&(Integer.parseInt(port) <= 65535)));
    }

    public boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
