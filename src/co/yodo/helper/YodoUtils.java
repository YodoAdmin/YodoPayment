package co.yodo.helper;

public class YodoUtils {
	 public static String bytesToHex(byte[] data) {
		 if(data == null)
			 return null;
		 
		 int len = data.length;
         String str = "";
         for(int i = 0; i < len; i++) {
        	 if((data[i]&0xFF) < 16)
        		 str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
             else
                 str = str + java.lang.Integer.toHexString(data[i]&0xFF);
         }
         return str;
     }
	 
	 public static byte[] hexToBytes(String str) {
		 if(str == null) {
			 return null;
         } else if(str.length() < 2) {
             return null;
         } else {
        	 int len = str.length() / 2;
             byte[] buffer = new byte[len];
             for(int i = 0; i < len; i++) {
            	 buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
             }
             return buffer;
         }
	 }
}
