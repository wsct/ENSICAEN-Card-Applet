/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Dedicated File implementation inspired by TB100.
 */
public class DedicatedFile extends File {

	private static final byte MAX_CHILDREN = 10;

	private final File[] _children = new File[MAX_CHILDREN];

	private byte _childrenCount = 0;

	/**
	 * Creates a new empty DF.
	 * 
	 * @param fileSystem The file system instance this DF belongs.
	 */
	public DedicatedFile(FileSystem fileSystem) {
		super(fileSystem);
	}

	//
	// >> File
	//

	/**
	 * {@inheritDoc}
	 */
	protected final void clearInternals() {
		// TODO Delete all children
		for (byte i = 0; i < _childrenCount; i++) {
			deleteFile(i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isAvailable(short offset, short length) {
		for (byte i = 0; i < _childrenCount; i++) {
			if (offset < (short) (_children[i].getOffset() + _children[i].getLength())
					|| (short) (offset + length) > _children[i].getOffset()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isDF() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isEF() {
		return false;
	}

	//
	// >> Public Methods
	//

	/**
	 * Creates a new DF starting at offset and using size bytes.
	 * 
	 * @param offset       Offset of the start of the file in DF body.
	 * @param size         Size used by the file (header + body).
	 * @param header       Buffer containing the header of the file.
	 * @param headerOffset Offset of the header in previous buffer.
	 * @param headerLength Length of the header (in bytes).
	 * 
	 * @return Index of the new file.
	 */
	public byte createDedicatedFile(short offset, short size, byte[] header, short headerOffset, short headerLength) {
		if (_childrenCount == MAX_CHILDREN) {
			return -1;
		}

		_children[_childrenCount] = _fileSystem.getFreeDF();
		_children[_childrenCount].setup(this, offset, size, header, headerOffset, headerLength);
		_childrenCount++;

		return (byte) (_childrenCount - 1);
	}

	/**
	 * Creates a new EF starting at offset and using size bytes.
	 * 
	 * @param offset       Offset of the start of the file in DF body.
	 * @param size         Size used by the file (header + body).
	 * @param header       Buffer containing the header of the file.
	 * @param headerOffset Offset of the header in previous buffer.
	 * @param headerLength Length of the header (in bytes).
	 * 
	 * @return Index of the new file.
	 */
	public byte createElementaryFile(short offset, short size, byte[] header, short headerOffset, short headerLength) {
		if (_childrenCount == MAX_CHILDREN) {
			return -1;
		}

		_children[_childrenCount] = _fileSystem.getFreeEF();
		_children[_childrenCount].setup(this, offset, size, header, headerOffset, headerLength);
		_childrenCount++;

		return (byte) (_childrenCount - 1);
	}

	/**
	 * Deletes a file by its index.
	 * 
	 * @param nth index of the file in the DF.
	 * 
	 * @return false is the file is not found.
	 */
	public boolean deleteFile(byte nth) {
		if (nth <= 0 || nth >= _childrenCount) {
			return false;
		}

		_children[nth].release();

		for (byte i = (byte) (nth + 1); i < _childrenCount; i++) {
			_children[(byte) (i - 1)] = _children[i];
		}
		_childrenCount--;
		_children[_childrenCount] = null;

		return true;
	}

	/**
	 * Returns the nth child file. A call to hasChild should be done to verify the
	 * existence.
	 * 
	 * @param nth Index of the file in the DF.
	 * 
	 * @param nth Index of the child (starts at 0)
	 */
	public File getChild(byte nth) {
		return _children[nth];
	}

	/**
	 * Returns true if the nth child file exists.
	 * 
	 * @param nth Index of the file in the DF.
	 */
	public boolean hasChild(byte nth) {
		return nth >= 0 && nth < _childrenCount;
	}
}
