package javacard.framework;

public class ISO7816 {
    public static final byte INS_SELECT = (byte) 0xA4;
    public static final short OFFSET_INS = 1;
    public static final short OFFSET_P1 = 2;
    public static final short OFFSET_P2 = 3;
    public static final short OFFSET_LC = 4;
    public static final short OFFSET_CDATA = 5;

    public static final short CLA_ISO7816 = 0;

    public static final short SW_CONDITIONS_NOT_SATISFIED = 0x6985;
    public static final short SW_DATA_INVALID = 0x6983;
    public static final short SW_FILE_FULL = 0x6A84;
    public static final short SW_FILE_NOT_FOUND = 0x6A82;
    public static final short SW_INS_NOT_SUPPORTED = 0x6D00;
    public static final short SW_WRONG_DATA = 0x6A80;
    public static final short SW_WRONG_LENGTH = 0x6700;
    public static final short SW_WRONG_P1P2 = 0x6B00;
}
