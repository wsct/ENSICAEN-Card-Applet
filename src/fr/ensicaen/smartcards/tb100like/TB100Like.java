/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;

/**
 * TB100 like applet
 */
public class TB100Like extends Applet {

	private final DedicatedFile _masterFile;

	/**
	 * @Override
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new TB100Like().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public TB100Like() {
		_masterFile = new MasterFile(Constants.MF_LENGTH);
	}

	/**
	 * @Override
	 */
	public void process(APDU apdu) {
		if (selectingApplet()) {
			processAppletSelection(apdu);
			return;
		}

		byte[] apduBuffer = apdu.getBuffer();

		switch (apduBuffer[ISO7816.OFFSET_INS]) {
		case ISO7816.INS_SELECT:
			processSelect(apdu);
			break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	/**
	 * Process applet selection: returns the FCI in the UDR.
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processAppletSelection(APDU apdu) {
		short headerSize = _masterFile.getHeaderSize();

		apdu.setOutgoing();
		apdu.setOutgoingLength(headerSize);

		byte[] output = new byte[headerSize];
		_masterFile.getHeader(output, (short) 0);

		apdu.sendBytesLong(output, (short) 0, (short) output.length);
	}

	/**
	 * Process SELECT instruction
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processSelect(APDU apdu) {
		// TODO Select inner files
	}

}
