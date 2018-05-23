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

		case Constants.INS_CREATE_FILE:
			processCreateFile(apdu);
			break;

		case Constants.INS_DELETE_FILE:
			processDeleteFile(apdu);
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
		byte[] buffer = apdu.getBuffer();

		_masterFile.getHeader(buffer, (short) 0);

		apdu.setOutgoingAndSend((short) 0, _masterFile.getHeaderSize());
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
	 * offset: offset of first word of the file, 2 bytes (WORDS).
	 * </p>
	 * <p>
	 * size: size of the body of the file (WORDS).
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
		short headerSize = file.getHeaderSize();
		Util.setShort(buffer, (short) 0, (short) (file._inParentBodyOffset >> 2));
		Util.setShort(buffer, (short) 2, (short) ((file.getLength() - headerSize)));
		file.getHeader(buffer, (short) 4);
		apdu.setOutgoingAndSend((short) 0, (short) (4 + (headerSize << 2)));
	}

	/**
	 * Process READ BINARY instruction (CC2)
	 *
	 * <p>
	 * C-APDU: <code>00 B0 {P1-P2: offset} {Le}</code>
	 * </p>
	 * <p>
	 * R-APDU when an EF is selected: <code>{data in EF}</code>
	 * </p>
	 * <p>
	 * R-APDU when no EF is selected: <code>{headers of EF in current DF}</code>
	 * </p>
	 * 
	 * @param apdu The incoming APDU object
	 */
	private void processReadBinary(APDU apdu) {
		short le = apdu.setOutgoing(); // in BYTES
		short wordCount = (short)((le+3)/4);
		byte[] buffer = JCSystem.makeTransientByteArray( (short)(wordCount*4), JCSystem.CLEAR_ON_DESELECT);

		if (_currentEF != null) {
			// an EF is selected ==> read binary
			short offset = Util.getShort( apdu.getCurrentAPDUBuffer() , ISO7816.OFFSET_P1); // in WORDS

			verifyOutOfFile(offset, wordCount);
			
			_currentEF.read(offset, buffer, (short) 0, wordCount );
			// TODO: check => is _currentEF an EF/SZ
			
		} else {
			// no EF selected ==> DIR
			short offset = 0;
			byte currentChildNumber = 0;
			File fileChild = _currentDF.getChild(currentChildNumber);
			// get header of each file in current DF
			while (fileChild != null && offset < le) {
				fileChild.getHeader(buffer, offset);
				offset += fileChild.getHeaderSize();
				fileChild = _currentDF.getChild(++currentChildNumber);
			}
		}
		// and send data!
		apdu.setOutgoingLength(le);
		apdu.sendBytesLong(buffer, (short)0, le);
		
	}

	/**
	 * Process WRITE BINARY instruction (CC3)
	 *
	 * <p>
	 * C-APDU: <code>00 B0 {P1-P2: offset} {Lc} {data} </code>
	 * </p>
	 * 
	 * @param apdu The incoming APDU object
	 */
	private void processWriteBinary(APDU apdu) {
		// TODO: check ==> security of current EF

		byte[] apduBuffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short offset = Util.getShort(apduBuffer, ISO7816.OFFSET_P1); // in WORDS

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short length = APDUHelpers.getIncomingLength(apdu); // in BYTES
		
		short wordCount = (short)((length+3)/4); // length in WORDS
		verifyOutOfFile(offset, wordCount);
					
		if( !_currentEF.isAvailable(offset, wordCount) ){
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		
		byte[] buffer = JCSystem.makeTransientByteArray( (short)(wordCount*4), JCSystem.CLEAR_ON_DESELECT);

		Util.arrayCopyNonAtomic(apduBuffer, udcOffset, buffer, (short)0, length);
		for(short i=length; i < wordCount*4; i++){
			buffer[i]= (byte)0xFF;
		}
								
		_currentEF.write(buffer, (short)0, offset, wordCount );

	}

	/**
	 * Process ERASE instruction (CC3)
	 *
	 * <p>
	 * C-APDU: <code>00 0E {P1-P2: offset} {Lc} {data: length} </code>
	 * </p>
	 * 
	 * @param apdu The incoming APDU object
	 */
	private void processErase(APDU apdu){
		// TODO: check ==> security of current EF
		
		byte[] apduBuffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();		
				
		// Check if Lc ==2
		short lc = APDUHelpers.getIncomingLength(apdu);		
		if(lc != 2){
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		
		short offset = Util.getShort(apduBuffer, ISO7816.OFFSET_P1); // in WORDS		

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);		
		short length = Util.getShort(apduBuffer, udcOffset); // in WORDS
		
		verifyOutOfFile(offset, length);
		
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

	/**
	 * Process CREATE FILE instruction (E0)
	 * <p>
	 * C-APDU: 00 E0 {P1} 00 {Lc} {offset} {size} {header}
	 * </p>
	 * <p>
	 * P1: 01 for an DF, 02 for a EF
	 * </p>
	 * <p>
	 * offset: offset of first word of the file, 2 bytes (WORDS).
	 * </p>
	 * <p>
	 * size: size of the body of the file (WORDS).
	 * </p>
	 * <p>
	 * header: header of the new file, must be word aligned.
	 * </p>
	 * 
	 * @param apdu The incoming APDU object.
	 */
	private void processCreateFile(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);

		if (lc < 8) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		}

		short offset = Util.getShort(buffer, udcOffset);
		short size = Util.getShort(buffer, (short) (udcOffset + 2));
		short fid = Util.getShort(buffer, (short) (udcOffset + 4));
		short headerOffset = (short) (udcOffset + 4);
		short headerLength = (short) (lc - 4);

		File file = null;

		byte p1 = buffer[ISO7816.OFFSET_P1];
		switch (p1) {
		case Constants.P1_CREATE_FILE_DF:
			file = _currentDF.createDedicatedFile(offset, size, buffer, headerOffset, headerLength);
			break;
		case Constants.P1_CREATE_FILE_EF:
			file = _currentDF.createElementaryFile(offset, size, buffer, headerOffset, headerLength);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		}

		if (file == null) {
			ISOException.throwIt(Constants.SW_NOT_ENOUGH_SPACE_IN_FILE);
		}
	}

	/**
	 * Process DELETE FILE instruction (E4)
	 * <p>
	 * C-APDU: 00 E4 00 00 02 {fid}
	 * </p>
	 * 
	 * @param apdu The incoming APDU object.
	 */
	private void processDeleteFile(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);

		if (lc != 2) {
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		}

		short fid = Util.getShort(buffer, udcOffset);

		if (_currentDF.deleteFile(fid) == false) {
			ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
		}

		if (fid == _currentEF.getFileId()) {
			_currentEF = null;
		}
	}
	
	/**
	 * Verify that offset+length fit in current EF
	 */
	private void verifyOutOfFile(short offset, short length){
		// Check if there is a current EF
		if(_currentEF == null){
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
		
		short bodyLength = (short)(_currentEF.getLength() - _currentEF.getHeaderSize()); // in WORDS
		
		// check if offset < bodyLength
		if(offset >= bodyLength){
			ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
		}
		
		// check if offset+length <= bodyLength
		if(offset+length > bodyLength){
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
		}
		
	}
	
}
