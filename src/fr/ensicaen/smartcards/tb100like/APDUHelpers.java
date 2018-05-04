package fr.ensicaen.smartcards.tb100like;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Util;

/**
 * Defines some helper methods for APDU.
 */
public class APDUHelpers {

	/**
	 * Returns Lc field of the APDU.
	 * 
	 * Same as javacard 3 APDU.getIncomingLength() instance method.
	 */
	public static short getIncomingLength(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		if ((buffer[ISO7816.OFFSET_LC] & (byte) 0x80) == (byte) 0x00) {
			return Util.makeShort((byte) 0x00, buffer[ISO7816.OFFSET_LC]);
		} else {
			return Util.makeShort(buffer[(byte) (ISO7816.OFFSET_LC + 1)], buffer[(byte) (ISO7816.OFFSET_LC + 2)]);
		}
	}

	/**
	 * Returns UDC offset in the APDU buffer.
	 * 
	 * Same as javacard 3 APDU.getOffsetCdata() instance method.
	 */
	public static short getOffsetCdata(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		return (buffer[ISO7816.OFFSET_LC] < (byte) 0x80) ? ISO7816.OFFSET_CDATA : ISO7816.OFFSET_CDATA + 2;
	}
}
