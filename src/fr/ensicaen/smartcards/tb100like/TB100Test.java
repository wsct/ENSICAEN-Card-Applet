package fr.ensicaen.smartcards.tb100like;

import javacard.framework.*;

public class TB100Test extends Applet {

	private final DedicatedFile masterFile = new MasterFile((short) 0x0200);

	public TB100Test() {
		byte dfIndex = masterFile.createDedicatedFile((short) 0x20, (short) 0x80,
				new byte[] { (byte) 0x6F, (byte) 0x00 });
		File file = masterFile.getChild(dfIndex);
		if (file.isDF()) {
			byte efIndex = ((DedicatedFile) file).createElementaryFile((short) 0x04, (short) 0x04,
					new byte[] { (byte) 0x7F, (byte) 0x01 });
			File subFile = ((DedicatedFile) file).getChild(efIndex);
			if (subFile.isEF()) {
				((ElementaryFile) subFile).write(new byte[] { (byte) 'H', (byte) 'I', (byte) '!', (byte) '!' },
						(short) 0, (short) 0, (short) 4);
			}
		}
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new TB100Test().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
		if (selectingApplet()) {
			processAppletSelection(apdu);
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) 0x00:
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
		short headerSize = masterFile.getHeaderSize();

		apdu.setOutgoing();
		apdu.setOutgoingLength(headerSize);

		byte[] output = new byte[headerSize];

		DedicatedFile df = (DedicatedFile) masterFile.getChild((byte) 0);
		ElementaryFile ef = (ElementaryFile) df.getChild((byte) 0);
		ef.read((short) 0, output, (short) 0, (short) 4);

		apdu.sendBytesLong(output, (short) 0, (short) output.length);
	}
}
