/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Master File 3F00 inspired by TB100.
 */
public class MasterFile extends DedicatedFile {

	private static final byte[] mfHeader = new byte[] { (byte) 0x3F, (byte) 0x00, (byte) 0x20, (byte) 0x18 };

	protected final byte[] _memory;

	/**
	 * @param size Number of bytes used by the MF.
	 */
	public MasterFile(short size) {
		super(null, (short) 0, size, mfHeader, (short) 0, (short) mfHeader.length);

		_memory = new byte[getLength()];

		// The header must be explicitely set because memory is not allocated when
		// super(...) is called
		setHeader(mfHeader, (short) 0, (short) mfHeader.length);
	}

	//
	// >> File
	//

	protected final byte[] getMemory() {
		return _memory;
	}
}
