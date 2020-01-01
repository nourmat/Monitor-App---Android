package com.example.monitorapp;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPConnector {
    private static final int HEADERSIZE = 10;

    private DatagramSocket mSocket = null;
    private InetAddress mInetAddress= null;
    private int PORT;

    private final static String REQUESTCHECKCONN = "1";

    Context context;

    public UDPConnector(Context context) {
        this.context = context;
    }

    public void connectToSocket(final String address , final int port){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setmSocket(new DatagramSocket());
                    mInetAddress = InetAddress.getByName(address);
                    PORT = port;

                    byte[] buf = prepMSG(REQUESTCHECKCONN);
                    sendData(buf);
                }catch (IOException e){}
            }
        }).start();
    }

    public void closeConnection (){
        mSocket.close();
    }

    public void sendData(final byte[] buf){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("tag",buf.length+"");
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, mInetAddress, PORT);
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setmSocket(DatagramSocket mSocket) {
        this.mSocket = mSocket;
    }

    public byte[] prepMSG (String msg){
        String str = msg.length() +"";
        while (str.length() < HEADERSIZE)
            str += " ";
        str += msg;
        return Base64.encode(str.getBytes(),Base64.DEFAULT);
    }

    public byte[] prepMSGByte (byte[] buf){
        String str = buf.length + "";
        while (str.length() < HEADERSIZE)
            str += " ";

        byte[] strBytes = str.getBytes();
        byte[] finalByte = new byte[buf.length + strBytes.length];
        System.arraycopy(strBytes,0,finalByte,0,strBytes.length);
        System.arraycopy(buf,0,finalByte,strBytes.length,buf.length);

        return Base64.encode(finalByte,Base64.NO_WRAP);
    }

    public boolean isReachable(){
        try {
            return mSocket.getInetAddress().isReachable(200);
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}