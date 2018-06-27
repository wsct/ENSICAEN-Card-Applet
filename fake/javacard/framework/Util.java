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

    /**
     * Compares an array from the speciﬁed source array, beginning at the speciﬁed position, with the speciﬁed
     * position of the destination array from left to right. Returns the ternary result of the comparison : less
     * than(-1), equal(0) or greater than(1).
     *
     * @param src     source byte array
     * @param srcOff  offset within source byte array to start compare
     * @param dest    destination byte array
     * @param destOff offset within destination byte array to start compare
     * @param length  byte length to be compared
     * @return
     */
    public static final byte arrayCompare(byte[] src, short srcOff, byte[] dest, short destOff, short length) {
        if (srcOff < 0 || destOff < 0 || length < 0 || srcOff + length > src.length || destOff + length > dest.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (src == null || dest == null) {
            throw new NullPointerException();
        }
        short index = 0;
        int byteCompareResult;
        do {
            byteCompareResult = Byte.compare(src[srcOff + index], dest[destOff + index]);
            index++;
        } while (index < length && byteCompareResult == 0);

        return (byte) byteCompareResult;
    }

    public static void setShort(byte[] buffer, short i, short i1) {
    }
}
