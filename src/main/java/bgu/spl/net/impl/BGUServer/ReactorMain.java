package bgu.spl.net.impl.BGUServer;

import bgu.spl.net.api.ClientMessage;
import bgu.spl.net.api.MessageEncoderDecoderImpl;

import java.nio.charset.StandardCharsets;

public class ReactorMain {

    public static short bytesToShort(byte[] byteArr){
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public static byte[] shortToBytes(short num){
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static byte[] mergeBytes(byte[] a, byte[] b) {
        //return merged array, a is first b is second
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static void main (String [] args) {


        //TESTING USER MESSAGES - DecodeNextByte
//        short op = 6;
//        byte[] b1 = shortToBytes(op);
//        String name="noa";
//        byte[] b2 = name.getBytes(StandardCharsets.UTF_8);
//        byte[] b3 = {'\0'};
//        String pass="dabi";
//        byte[] b4 = pass.getBytes(StandardCharsets.UTF_8);
//        byte[] b5 = {'\0'};
//        String birthday ="date";
//        byte[] b6 = birthday.getBytes(StandardCharsets.UTF_8);
//        byte[] b7 = {'\0'};
//        byte[] b8 = {'\n'};
//        byte[] b9 = mergeBytes(b1,b2);
//        byte[] b10 = mergeBytes(b9,b3);
//        byte[] b11 = mergeBytes(b10,b4);
//        byte[] b12 = mergeBytes(b11,b5);
//        byte[] b13 = mergeBytes(b12,b6);
//        byte[] b14 = mergeBytes(b13,b7);
//        byte[] b15 = mergeBytes(b14,b8);
//
//        ClientMessage c=null;
//        for (int i=0; i<b15.length; i++) {
//            c = (ClientMessage) MessageEncoderDecoderImpl.decodeNextByte(b15[i]);
//        }
//        System.out.println(c.getOp());
//        System.out.println(c.getUsername());
//        System.out.println(c.getContent());
//        System.out.println(c.getSendDate());
    }
}
