package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;

public class TB100Test extends Applet {

	// Known CLA bytes
	final static byte CLA_ISO7816 = (byte) 0x00;
	final static byte CLA_PROPRIETARY = (byte) 0x80;
	// Known INS bytes
	final static byte INS_CREATE_DF = (byte) 0xCD;
	final static byte INS_CREATE_EF = (byte) 0xCE;
	final static byte INS_DELETE_FILE = (byte) 0xDF;
	final static byte INS_WRITE_EF = (byte) 0x20;
	final static byte INS_READ_EF = (byte) 0x21;
	final static byte INS_FIND_BY_FID = (byte) 0x22;

	// File System parameters
	final static short FS_SIZE = (short) 0x0100;
	final static byte FS_DF_MAX = (byte) 0x08;
	final static byte FS_EF_MAX = (byte) 0x08;

	private final DedicatedFile masterFile;

	public TB100Test() {
		// MF initialization
		FileSystem fileSystem = new FileSystem(FS_SIZE, FS_DF_MAX, FS_EF_MAX);
		masterFile = fileSystem.getFreeDF();
		masterFile.setup(null, (short) 0, FS_SIZE, Constants.MF_HEADER, (short) 0, (short) Constants.MF_HEADER.length);

		DedicatedFile df = masterFile.createDedicatedFile((short) 0x04, (short) 0x20,
				new byte[] { (byte) 0x6F, (byte) 0x00, (byte) 0x0A, (byte) 0x0B }, (short) 0, (short) 4);
		ElementaryFile ef = df.createElementaryFile((short) 0x02, (short) 0x04,
				new byte[] { (byte) 0x7F, (byte) 0x01, (byte) 0x02, (byte) 0x03 }, (short) 0, (short) 4);
		ef.write(new byte[] { (byte) 'H', (byte) 'I', (byte) '!', (byte) '!' }, (short) 0, (short) 0, (short) 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new TB100Test().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(APDU apdu) {
		if (selectingApplet()) {
			processAppletSelection(apdu);
			return;
		}

		byte[] buffer = apdu.getBuffer();

		if (buffer[ISO7816.CLA_ISO7816] == CLA_ISO7816) {
			switch (buffer[ISO7816.OFFSET_INS]) {
			case (byte) 0x00:

				break;
			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}

		if (buffer[ISO7816.CLA_ISO7816] == CLA_PROPRIETARY) {
			switch (buffer[ISO7816.OFFSET_INS]) {
			case INS_CREATE_DF:
				processCreateDF(apdu);
				break;
			case INS_CREATE_EF:
				processCreateEF(apdu);
				break;
			case INS_DELETE_FILE:
				processDeleteFile(apdu);
				break;
			case INS_FIND_BY_FID:
				processFindByFid(apdu);
				break;
			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
	}

	/**
	 * Process applet selection: returns the FCI in the UDR.
	 *
	 * @param apdu The incoming APDU object
	 */
	private void processAppletSelection(APDU apdu) {
		short outputLength = 4;

		apdu.setOutgoing();
		apdu.setOutgoingLength(outputLength);

		byte[] output = new byte[outputLength];

		DedicatedFile df = (DedicatedFile) masterFile.findFileByFileId((short) 0x6F00);
		ElementaryFile ef = (ElementaryFile) df.findFileByFileId((short) 0x7F01);
		ef.read((short) 0, output, (short) 0, (short) 1, false);

		apdu.sendBytesLong(output, (short) 0, (short) output.length);
	}

	/**
	 * Creates a new DF in MF.
	 * 
	 * C-APDU: 80 CD 00 00 Lc [<Offset> <Size> <Header>] 01
	 * 
	 * <Offset>, <Size>: 2 bytes
	 * 
	 * R-APDU: <index> SW1 SW2
	 */
	private void processCreateDF(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);
		short offset = Util.getShort(buffer, udcOffset);
		short size = Util.getShort(buffer, (short) (udcOffset + 2));

		File file = masterFile.createDedicatedFile(offset, size, buffer, (short) (udcOffset + 4), (short) (lc - 4));
		if (file == null) {
			ISOException.throwIt(ISO7816.SW_FILE_FULL);
		}
	}

	/**
	 * Creates a new EF in MF.
	 * 
	 * C-APDU: 80 CE 00 00 Lc [<Offset> <Size> <Header>] 01
	 * 
	 * <Offset>, <Size>: 2 bytes
	 * 
	 * R-APDU: <index> SW1 SW2
	 */
	private void processCreateEF(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		short udcOffset = APDUHelpers.getOffsetCdata(apdu);
		short lc = APDUHelpers.getIncomingLength(apdu);
		short offset = Util.getShort(buffer, udcOffset);
		short size = Util.getShort(buffer, (short) (udcOffset + 2));

		File file = masterFile.createElementaryFile(offset, size, buffer, (short) (udcOffset + 4), (short) (lc - 4));
		if (file == null) {
			ISOException.throwIt(ISO7816.SW_FILE_FULL);
		}
	}

	/**
	 * Deletes a file by its index.
	 * 
	 * C-APDU: 80 DF 00 00 01 <nth>
	 * 
	 * <nth>: 1 byte
	 */
	private void processDeleteFile(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		if (!masterFile.deleteFile(Util.getShort(buffer, ISO7816.OFFSET_CDATA))) {
			ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
		}
	}

	/**
	 * Finds a file by its FID.
	 * 
	 * C-APDU: 80 22 00 00 02 <FID>
	 * 
	 * <FID>: 2 bytes
	 */
	private void processFindByFid(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short bufferLength = apdu.setIncomingAndReceive();

		File file = masterFile.findFileByFileId(Util.getShort(buffer, ISO7816.OFFSET_CDATA));
		if (file == null) {
			ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
		}

		file.getHeader(buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, file.getHeaderSize());
	}
}
