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

			case Constants.INS_READ_BINARY:
				processReadBinary(apdu);
				break;

			case Constants.INS_WRITE_BINARY:
				processWriteBinary(apdu);
				break;
				
			case Constants.INS_ERASE:
				processErase(apdu);
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
	 * Process SELECT instruction (CC4).
	 * <p>
	 * C-APDU: <code>00 A4 00 00 02 {FID} {Le}</code>
	 * </p>
	 * <p>
	 * R-APDU: <code>{offset} {size} {header}</code>
	 * </p>
	 * <p>
	 * offset, size: 2 bytes each (WORDS).
	 * </p>
	 * 
	 * @param apdu The incoming APDU object.
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

		// Update current DF / EF
		if (file.isDF()) {
			_currentDF = (DedicatedFile) file;
			_currentEF = null;
		} else {
			_currentEF = (ElementaryFile) file;
		}

		// Build and send R-APDU
		Util.setShort(buffer, (short) 0, (short) (file._inParentBodyOffset >> 2));
		Util.setShort(buffer, (short) 2, (short) (file.getLength() >> 2));
		file.getHeader(buffer, (short) 4);
		apdu.setOutgoingAndSend((short) 0, (short) (4 + file.getHeaderSize()));
	}

	/**
	 * Process READ BINARY instruction (CC2)
	 *
	 * <p>
	 * C-APDU: <code>00 B0 00 00 {Le}</code>
	 * </p>
	 * <p>
	 * R-APDU when an EF is selected: <code>{data in EF}</code>
	 * </p>
	 * <p>
	 * R-APDU when no EF is selected: <code>{headers of EF in current DF}</code>
	 * </p>
	 * @param apdu The incoming APDU object
	 */
	private void processReadBinary(APDU apdu){
		byte[] apduBuffer = apdu.getBuffer();
		short le = apdu.setOutgoing();
		
		if(_currentEF != null){
			// an EF is selected ==> read binary 
			// TODO: check => is _currentEF an EF/SZ
			// TODO: check => is le < _currentEF.getLength
						
			// read file
			_currentEF.read((short)0, apduBuffer, (short) 0, le);			
		}
		else{
			// no EF selected ==> DIR 
			short offset = 0;
			byte currentChildNumber = 0;
			File fileChild = _currentDF.getChild(currentChildNumber);
			// get header of each file in current DF
			while(fileChild!=null && offset<le){
				fileChild.getHeader(apduBuffer, offset);
				offset += fileChild.getHeaderSize();
				fileChild = _currentDF.getChild(++currentChildNumber);
			}
		}
		// and send data!
		apdu.setOutgoingLength((short) le);
		apdu.sendBytes((short) 0, (short) le);
	}

	/**
	 * Process WRITE BINARY instruction (CC3)
	 *
	 * <p>
	 * C-APDU: <code>00 B0 00 00 {Lc} {data} </code>
	 * </p>
	 * @param apdu The incoming APDU object
	 */
	private void processWriteBinary(APDU apdu){
		
		if(_currentEF == null){
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
			return;
		}
		
		// TODO: check ==> security of current EF
		// TODO: check ==> is lc < _currentEF.getLength
		byte[] apduBuffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();
		
		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);		
		
		_currentEF.write(apduBuffer, udcOffset, (short)0, (short)(lc/4));
			
	}

	/**
	 * Process ERASE instruction (CC3)
	 *
	 * <p>
	 * C-APDU: <code>00 0E 00 00 {Lc} {data} </code>
	 * </p>
	 * @param apdu The incoming APDU object
	 */
	private void processErase(APDU apdu){
		// TODO: check ==> security of current EF
		
		// Check if there is a current EF
		if(_currentEF == null){
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
				
		byte[] apduBuffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();		
				
		// Check if Lc ==2
		short lc = APDUHelpers.getIncomingLength(apdu);		
		if(lc != 2){
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		
		// check if offset < bodyLength
		short bodyLength = (short)(_currentEF.getLength() - _currentEF.getHeaderSize()); // in WORDS	
		short offset = Util.getShort(apduBuffer, ISO7816.OFFSET_P1); // in WORDS		
		if(offset >= bodyLength){
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		}
		
		// check if offset+length <= bodyLength
		short udcOffset = APDUHelpers.getOffsetCdata(apdu);		
		short length = Util.getShort(apduBuffer, udcOffset); // in WORDS
		if(offset+length > bodyLength){
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}
				
		_currentEF.erase(offset, length);
			
	}


	/**
	 * Process GENERATE RANDOM instruction (CC2)
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processGenerateRandom(APDU apdu) {

		byte[] apduBuffer = apdu.getBuffer();
		short le = apdu.setOutgoing();

		// verify that Le='08'
		if (le != 8) {
			// if not => error
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		} else {
			// generate 8 random bytes
			RandomData rndGen = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
			rndGen.generateData(apduBuffer, (short) 0, (short) 8);
			// and send it!
			apdu.setOutgoingLength((short) 8);
			apdu.sendBytes((short) 0, (short) 8);
		}
	}
}
