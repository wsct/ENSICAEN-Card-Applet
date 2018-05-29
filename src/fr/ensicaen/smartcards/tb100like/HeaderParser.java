package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

public class HeaderParser {
    public static final byte FILETYPE_DF = (byte) 0x3D;
    public static final byte FILETYPE_EFWZ = (byte) 0x2D;
    public static final byte FILETYPE_EFSZ = (byte) 0x0C;

    /**
     * File type: one of <c>Constants.FILETYPE_*</>.
     */
    public byte fileType;
    /**
     * Length of the header (BYTES).
     */
    public byte headerLength;
    /**
     * Length of file body (WORDS).
     */
    public short bodyLength;
    /**
     * FID of the file.
     */
    public short fileIdentifier;

    /**
     * Parses the header starting at offset in header byte array.
     *
     * @return true if successful
     */
    public boolean parse(byte[] header, short offset) {
        fileIdentifier = Util.getShort(header, offset);

        byte qualifier = header[offset];
        if ((qualifier & FILETYPE_DF) == FILETYPE_DF) {
            fileType = FILETYPE_DF;
            headerLength = 8;
            bodyLength = (short) (Util.getShort(header, (short) (offset + 2)) - 2);
        } else if ((qualifier & FILETYPE_EFWZ) == FILETYPE_EFWZ) {
            fileType = FILETYPE_EFWZ;
            headerLength = 8;
            bodyLength = (short) (Util.getShort(header, (short) (offset + 2)) - 2);
        } else if ((qualifier & FILETYPE_EFSZ) == FILETYPE_EFSZ) {
            fileType = FILETYPE_EFSZ;
            headerLength = 4;
            bodyLength = (short) (header[offset + 2] & 0x0F);
        } else {
            return false;
        }

        return true;
    }
}