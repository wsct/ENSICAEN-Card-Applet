/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;
import javacard.security.*;

/**
 * TB100 like applet
 */
public class TB100Like extends Applet {

	private final static byte[] DF7F00_HEADER = new byte[] { (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x22,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0x62 };
	private final static byte[] EF_6F01_HEADER = new byte[] { (byte) 0x6F, (byte) 0x01, (byte) 0x00, (byte) 0x10,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x83 };

	private final DedicatedFile _masterFile;

	/**
	 * Currently selected DF.
	 */
	private DedicatedFile _currentDF;
	/**
	 * Currently selected EF (may be <code>null</code> if none is selected).
	 */
	private ElementaryFile _currentEF;

	/**
	 * {@inheritDoc}
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new TB100Like().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public TB100Like() {
		_masterFile = new DedicatedFile(new FileSystem(Constants.FILESYSTEM_SIZE, Constants.DF_MAX, Constants.EF_MAX));
		_masterFile.setup(null, (short) 0, Constants.FILESYSTEM_SIZE, Constants.MF_HEADER, (short) 0,
				(short) Constants.MF_HEADER.length);

		DedicatedFile df = _masterFile.createDedicatedFile((short) 0x0013, (short) 0x0022, DF7F00_HEADER, (short) 0,
				(short) DF7F00_HEADER.length);
		df.createElementaryFile((short) 0x0000, (short) 0x0010, EF_6F01_HEADER, (short) 0,
				(short) EF_6F01_HEADER.length);

		_currentDF = _masterFile;
		_currentEF = null;
	}

	/**
	 * {@inheritDoc}
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

		case Constants.INS_GENERATE_RANDOM:
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
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);

		if (lc != 2) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		short fid = Util.getShort(buffer, udcOffset);

		File file;
		if (fid == (short) 0x3F00) {
			file = _masterFile;
		} else {
			file = _currentDF.findFileByFileId(fid);
		}

		if (file == null) {
			ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
		}

		if (file.isDF()) {
			_currentDF = (DedicatedFile) file;
			_currentEF = null;
		} else {
			_currentEF = (ElementaryFile) file;
		}
	}

	/**
	 * Process GENERATE RANDOM instruction
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processGenerateRandom(APDU apdu) {

		byte[] apduBuffer = apdu.getBuffer();
		short Le = apdu.setOutgoing();

		// verify that Le='08'
		if (Le != 8) {
			// if not => error
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		} else {
			// generate 8 random bytes
			RandomData rndGen = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
			rndGen.generateData(apduBuffer, (short) 0, (short) 8);
			// and send it !
			apdu.setOutgoingLength((short) 8);
			apdu.sendBytes((short) 0, (short) 8);
			ISOException.throwIt(ISO7816.SW_NO_ERROR);
		}
	}

}
