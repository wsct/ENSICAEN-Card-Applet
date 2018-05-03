/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 *
 */
public final class Constants {

	public static final byte INS_SELECT = (byte) 0xA4;

	public static final byte[] FCI_APPLET = new byte[] { (byte) 'T', (byte) 'B', (byte) '1', (byte) '0', (byte) '0',
			(byte) 'L', (byte) 'I', (byte) 'K', (byte) 'E' };

	public static final byte[] FCI_MF = new byte[] { (byte) 0x3F, (byte) 0x00, (byte) 0x20, (byte) 0x18 };

	public static final short MF_LENGTH = (short) 0x0800;
}
