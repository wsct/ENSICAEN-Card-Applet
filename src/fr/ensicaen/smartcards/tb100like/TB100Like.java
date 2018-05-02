/**
 * @author ENSICAEN
 */
 
package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;

/**
 * TB100 like applet
 */
public class TB100Like extends Applet
{
	/**
	 * @Override
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new TB100Like().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	/**
	 * @Override
	 */
	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			processAppletSelection(apdu);
			return;
		}

		byte[] apduBuffer = apdu.getBuffer();
		
		switch (apduBuffer[ISO7816.OFFSET_INS])
		{
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
		apdu.setOutgoing();
		apdu.setOutgoingLength((short) Constants.FCI.length);
		apdu.sendBytesLong(Constants.FCI, (short) 0, (short) Constants.FCI.length);
	}
	
	/**
	 * Process SELECT instruction
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processSelect(APDU apdu) {
		// TODO
	}

}
