package javacard.framework;

public class Util {
    public static short makeShort(byte left, byte right) {
        return (short) (((left & 0xFF) << 8) | (right & 0xFF));
    }

    public static short getShort(byte[] buffer, short offset) {
        return makeShort(buffer[offset], buffer[offset + 1]);
    }

    public static void arrayFillNonAtomic(byte[] memory, short offset, short length, byte value) {
    }

    public static void arrayCopyNonAtomic(byte[] memory, short i, byte[] output, short outputOffset, short writtenLength) {
    }

    public static void setShort(byte[] buffer, short i, short i1) {
    }
}
