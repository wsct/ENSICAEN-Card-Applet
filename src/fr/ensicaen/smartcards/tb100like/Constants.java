/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 *
 */
public final class Constants {

	public static final byte INS_GENERATE_RANDOM = (byte) 0xC4;
	public static final byte INS_SELECT = (byte) 0xA4;

	public static final byte[] FCI_APPLET = new byte[] { (byte) 'T', (byte) 'B', (byte) '1', (byte) '0', (byte) '0',
			(byte) 'L', (byte) 'I', (byte) 'K', (byte) 'E' };

	public static final byte[] MF_HEADER = new byte[] { (byte) 0x3F, (byte) 0x00, (byte) 0x02, (byte) 0xA1, (byte) 0xFF,
			(byte) 0xFF, (byte) 0x9E, (byte) 0x81 };

	public static final short FILESYSTEM_SIZE = (short) 0x02A1;
	public static final byte DF_MAX = (byte) 0x08;
	public static final byte EF_MAX = (byte) 0x10;
}
