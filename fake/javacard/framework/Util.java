package javacard.framework;

public class Util {
    /**
     * Concatenates the two parameter bytes to form a short value.
     *
     * @param b1 the first byte ( high order byte ).
     * @param b2 the second byte ( low order byte )
     * @return the short value the concatenated result
     */
    public static short makeShort(byte b1, byte b2) {
        return (short) (((b1 & 0xFF) << 8) | (b2 & 0xFF));
    }

    /**
     * Concatenates two bytes in a byte array to form a short value.
     *
     * @param bArray byte array
     * @param bOff   offset within byte array containing first byte (the high order byte)
     * @return the short value the concatenated result
     */
    public static short getShort(byte[] bArray, short bOff) {
        return makeShort(bArray[bOff], bArray[bOff + 1]);
    }

    /**
     * Fills the byte array (non-atomically) beginning at the specified position, for the specified length with the
     * specified byte value.
     *
     * @param bArray the byte array
     * @param bOff   offset within byte array to start filling bValue into
     * @param bLen   byte length to be filled
     * @param bValue the value to fill the byte array with
     */
    public static void arrayFillNonAtomic(byte[] bArray, short bOff, short bLen, byte bValue) {
        for (int i = 0; i < bLen; i++) {
            bArray[bOff + i] = bValue;
        }
    }

    public static void arrayCopyNonAtomic(byte[] source, short sourceOffset, byte[] output, short outputOffset, short writtenLength) {
        System.arraycopy(source, sourceOffset, output, outputOffset, writtenLength);
    }

    /**
     * Compares an array from the specified source array, beginning at the specified position, with the specified
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
    public static byte arrayCompare(byte[] src, short srcOff, byte[] dest, short destOff, short length) {
        if (src == null || dest == null) {
            throw new NullPointerException();
        }
        if (srcOff < 0 || destOff < 0 || length < 0 || srcOff + length > src.length || destOff + length > dest.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        short index = 0;
        int byteCompareResult;
        do {
            byteCompareResult = Byte.compare(src[srcOff + index], dest[destOff + index]);
            index++;
        } while (index < length && byteCompareResult == 0);

        return (byte) byteCompareResult;
    }

    /**
     * Deposits the short value as two successive bytes at the specified offset in the byte array.
     *
     * @param bArray byte array
     * @param bOff   offset within byte array to deposit the first byte (the high order byte)
     * @param sValue the short value to set into array.
     * @return bOff+2
     */
    public static short setShort(byte[] bArray, short bOff, short sValue) {
        bArray[bOff] = (byte) ((sValue >> 8) & 0xFF);
        bOff++;
        bArray[bOff] = (byte) (sValue & 0xFF);
        bOff++;
        return bOff;
    }
}
