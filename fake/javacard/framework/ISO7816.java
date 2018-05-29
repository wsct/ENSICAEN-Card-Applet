package javacard.framework;

public class ISO7816 {
    public static final short OFFSET_INS = 1;
    public static final short OFFSET_P1 = 2;
    public static final short OFFSET_LC = 4;
    public static final short OFFSET_CDATA = 5;
    public static final short CLA_ISO7816 = 0;
    public static final short SW_INS_NOT_SUPPORTED = 0;
    public static final short SW_FILE_FULL = 0;
    public static final short SW_FILE_NOT_FOUND = 0;
    public static final byte INS_SELECT = 0x14;
    public static final short SW_WRONG_LENGTH = 0;
    public static final short SW_CONDITIONS_NOT_SATISFIED = 0;
    public static final short SW_WRONG_P1P2 = 0;
    public static final short SW_WRONG_DATA = 0;
    public static final short SW_DATA_INVALID = 0;
}
