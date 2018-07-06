package fr.ensicaen.smartcards.helpers;

/**
 * Helper class managing a pre allocated array of bytes.
 * Mostly used to ease APDU buffer usage when unit testing.
 */
public class ApduBuffer {
    private byte[] buffer = new byte[255];

    /**
     * Initializes the instance with an initial value.
     *
     * @param hexaString Hexadecimal string representation of a byte array.
     */
    public ApduBuffer(String hexaString) {
        set(hexaString);
    }

    /**
     * Sets the content of the buffer.
     *
     * @param hexaString Hexadecimal string representation of the content.
     * @return Current instance.
     */
    public ApduBuffer set(String hexaString) {
        return set(Helpers.hexaToBytes(hexaString));
    }

    /**
     * Sets the content of the buffer.
     *
     * @param source Data as a byte array.
     * @return Current instance.
     */
    public ApduBuffer set(byte[] source) {
        return set(source, (short) 0, (short) source.length);
    }

    /**
     * Sets the content of the buffer.
     *
     * @param source Array containing the content.
     * @param offset Offset of the useful data in the source.
     * @param length Length of the useful data.
     * @return Current instance.
     */
    public ApduBuffer set(byte[] source, short offset, short length) {
        System.arraycopy(source, 0, buffer, offset, length);
        return this;
    }

    /**
     * Gets the internal buffer.
     *
     * @return
     */
    public byte[] get() {
        return buffer;
    }

    /**
     * Gets an extract of the buffer (length first bytes).
     *
     * @param length Number of bytes to retrieve starting from the start of the buffer.
     * @return
     */
    public byte[] getRange(short length) {
        return getRange((short) 0, length);
    }

    /**
     * Gets an extract of the buffer from offset to length.
     *
     * @param offset Offset of the first byte to retrieve.
     * @param length Number of bytes to retrieve starting from offset.
     * @return
     */
    public byte[] getRange(short offset, short length) {
        byte[] chunk = new byte[length];
        System.arraycopy(buffer, offset, chunk, 0, length);
        return chunk;
    }
}
