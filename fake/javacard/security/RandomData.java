package javacard.security;

public class RandomData {
    public static final byte ALG_SECURE_RANDOM = 0;

    public static RandomData getInstance(byte algorithm) {
        return new RandomData();
    }

    public void generateData(byte[] apduBuffer, short offset, short length) {
    }
}
