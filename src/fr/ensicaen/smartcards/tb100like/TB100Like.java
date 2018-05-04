/**
 * @author ENSICAEN
 */
 
package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;
import javacard.security.*;


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
				
			case Constants.INS_GENERATE_RANDOM :
				processGenerateRandom(apdu);
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
	
	/**
	 * Process GENERATE RANDOM instruction
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processGenerateRandom(APDU apdu){

		byte[] apduBuffer = apdu.getBuffer();
		short Le = apdu.setOutgoing();
		
		// verify that Le='08'
		if(Le!=8){
			// if not => error
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		else{
		// generate 8 random bytes
			RandomData rndGen = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
			rndGen.generateData(  apduBuffer , (short)0, (short)8 );
		// and send it !
			apdu.setOutgoingLength( (short)8 );
			apdu.sendBytes( (short)0, (short)8 );
			ISOException.throwIt(ISO7816.SW_NO_ERROR);
		}
	}

}
