package com.example.monitorapp;

import java.util.Arrays;

public class HelperFunctions {
    static byte[] getByteArrayFromString(String str){
        return str.getBytes();
    }

    static String getStringFromByteArray (byte[] byteArr){
        return Arrays.toString(byteArr);
    }
}
