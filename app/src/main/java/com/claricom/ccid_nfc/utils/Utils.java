package com.claricom.ccid_nfc.utils;

public class Utils {
    public static String convertByteArrayToHexString(byte [] inarray, String join) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            if (!join.isEmpty() && !out.isEmpty()) {
                out += " ";
            }
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public static void reverseByteArray(byte[] array) {

        if (null == array) {
            return;
        }

        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static String convertByteArrayToDecString(byte[] bytes) {
        final char[] decimalArray = "0123456789".toCharArray();
        char[] decimalChars = new char[bytes.length * 4];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            decimalChars[j * 4] = decimalArray[v / 100];
            decimalChars[j * 4 + 1] = decimalArray[(v / 10) % 10];
            decimalChars[j * 4 + 2] = decimalArray[v % 10];
//            decimalChars[j * 4 + 3] = ' ';
        }
        return new String(decimalChars);
    }


}
