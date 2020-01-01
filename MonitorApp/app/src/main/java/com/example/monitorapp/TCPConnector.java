package com.example.monitorapp;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class TCPConnector {

    private static final int HEADERSIZE = 10;

    private boolean isConnected = false;
    private TCPConnector mTCPConnector = this;

    private Socket mSocket = null;
    private InetAddress mInetAddress= null;
    private int PORT;

    private final static String REQUESTCHECKCONN = "1";

    Context context;

    public TCPConnector(Context context,String address,int port) {
        try {
            this.context = context;
            mInetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        PORT = port;
    }


    public void connectToSocket(final String address , final int port){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInetAddress = InetAddress.getByName(address);
                    PORT = port;
                    mSocket = new Socket(mInetAddress, port);
                    sendData(REQUESTCHECKCONN.getBytes());
                    isConnected = true;

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendData(final byte[] buf){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("tag",buf.length+"");

                    byte[] encoded_Buffer = Base64.encode(buf,Base64.DEFAULT);
                    OutputStream out = mSocket.getOutputStream();
                    out.write(encoded_Buffer);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Still not working properly
     * @return
     */
    public byte[] receiveData(){
        final byte [] buf = new byte[1000];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if (mSocket != null) {
                        mSocket.setSoTimeout(500);
                        InputStream in = mSocket.getInputStream();
                        in.read(buf);
                    }
                } catch (SocketTimeoutException e){
                    Log.d("tag", "TimeOut");
                    mTCPConnector.connectToSocket(mInetAddress.getHostAddress(),PORT);
                    e.printStackTrace();
                } catch (IOException e){
                    Log.d("tag", "ERROR Reading BEAT");
                    mTCPConnector.connectToSocket(mInetAddress.getHostAddress(),PORT);
                    e.printStackTrace();
                }catch (IllegalArgumentException e){
                    Log.d("tag", "ERROR Decoding BEAT");
                    e.printStackTrace();
                }
            }
        }).start();
        return buf;
    }

    /**
     * Adds Header Size of the packet to be sent
     * @param msg
     * @return
     */
    public byte[] prepMSG (String msg){
        String str = msg.length() +"";
        while (str.length() < HEADERSIZE)
            str += " ";
        str += msg;
        return Base64.encode(str.getBytes(),Base64.NO_WRAP);
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

    public void closeConnection (){
        isConnected = false;
        try {
            mSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return isConnected;
    }
}
