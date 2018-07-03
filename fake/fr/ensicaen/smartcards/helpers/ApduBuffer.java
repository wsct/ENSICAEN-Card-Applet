package fr.ensicaen.smartcards.helpers;

public class ApduBuffer {
    private byte[] buffer = new byte[255];

    public ApduBuffer(String hexaString) {
        byte[] bytes = Helpers.hexaToBytes(hexaString);
        System.arraycopy(bytes, 0, buffer, 0, bytes.length);
    }

    public void set(byte[] source) {
        System.arraycopy(source, 0, buffer, 0, source.length);
    }

    public void set(byte[] source, short offset, short length) {
        System.arraycopy(source, 0, buffer, offset, length);
    }

    public byte[] get() {
        return buffer;
    }

    public byte[] getRange(short length) {
        byte[] chunk = new byte[length];
        System.arraycopy(buffer, 0, chunk, 0, length);
        return chunk;
    }
}
