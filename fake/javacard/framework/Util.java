package javacard.framework;

public class Util {
    public static short makeShort(byte left, byte right) {
        return (short) (((left & 0xFF) << 8) | (right & 0xFF));
    }

    public static short getShort(byte[] buffer, short offset) {
        return makeShort(buffer[offset], buffer[offset + 1]);
    }

    public static void arrayFillNonAtomic(byte[] buffer, short offset, short length, byte value) {
        for (int i = 0; i < length; i++) {
            buffer[offset + i] = value;
        }
    }

    public static void arrayCopyNonAtomic(byte[] source, short sourceOffset, byte[] output, short outputOffset, short writtenLength) {
        System.arraycopy(source, sourceOffset, output, outputOffset, writtenLength);
    }

    public static void setShort(byte[] buffer, short i, short i1) {
    }
}
