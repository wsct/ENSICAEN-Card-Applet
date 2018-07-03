package fr.ensicaen.smartcards.helpers;

public class Helpers {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a byte array to an hexa string.
     *
     * @param bytes
     * @return
     * @source https://stackoverflow.com/a/9855338/1774251
     */
    public static String bytesToHexa(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts an hexa string to a byte array.
     *
     * @param hexaString
     * @return
     * @source http://java2s.com/Code/Java/Data-Type/hexStringToByteArray.htm
     */
    public static byte[] hexaToBytes(String hexaString) {
        byte[] b = new byte[hexaString.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hexaString.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

}
