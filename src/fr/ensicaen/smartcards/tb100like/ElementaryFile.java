/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Elementary File implementation inspired by TB100.
 */
public class ElementaryFile extends File {

	/**
	 * Creates a new empty EF.
	 * 
	 * @param fileSystem The file system instance this DF belongs.
	 */
	public ElementaryFile(FileSystem fileSystem) {
		super(fileSystem);
	}

	//
	// >> File
	//

	/**
	 * {@inheritDoc}
	 */
	protected final void clearInternals() {
		// TODO Clear used space.
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAvailable(short offset, short length) {
		// TODO Implement availability check
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isDF() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isEF() {
		return true;
	}

	//
	// Public Methods
	//

	/**
	 * Write bytes to the file.
	 * 
	 * @param source       Buffer containing the data to write.
	 * @param sourceOffset Offset of the data in previous buffer.
	 * @param offset       Offset in the body where to write the data.
	 * @param length       Length of the data to write.
	 * 
	 * @return offset + length;
	 */
	public short write(byte[] source, short sourceOffset, short offset, short length) {
		_fileSystem.write(source, sourceOffset, getInMemoryOffset(offset), length);

		return (short) (offset + length);
	}

	/**
	 * Read bytes from the file.
	 * 
	 * @param offset       Offset in the body where to start the reading.
	 * @param output       Output buffer.
	 * @param outputOffset Offset in the output buffer where to write the data.
	 * @param length       Length of the data to read.
	 * 
	 * @return offset + length;
	 */
	public short read(short offset, byte[] output, short outputOffset, short length) {
		_fileSystem.read(getInMemoryOffset(offset), output, outputOffset, length);

		return (short) (offset + length);
	}
}
