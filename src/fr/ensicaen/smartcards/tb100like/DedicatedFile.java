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
	 * @param offset       Offset of the start of the file in DF body (WORDS).
	 * @param size         Size used by the file (header + body) (WORDS).
	 * @param header       Buffer containing the header of the file.
	 * @param headerOffset Offset of the header in previous buffer (BYTES).
	 * @param headerLength Length of the header (BYTES).
	 * 
	 * @return Index of the new file.
	 */
	public final DedicatedFile createDedicatedFile(short offset, short size, byte[] header, short headerOffset,
			short headerLength) {
		if (_childrenCount == MAX_CHILDREN) {
			return null;
		}

		DedicatedFile file = _fileSystem.getFreeDF();
		if (file != null) {
			file.setup(this, offset, size, header, headerOffset, headerLength);

			_children[_childrenCount] = file;
			_childrenCount++;
		}

		return file;
	}

	/**
	 * Creates a new EF starting at offset and using size bytes.
	 * 
	 * @param offset       Offset of the start of the file in DF body (WORDS).
	 * @param size         Size used by the file (header + body) (WORDS).
	 * @param header       Buffer containing the header of the file.
	 * @param headerOffset Offset of the header in previous buffer (BYTES).
	 * @param headerLength Length of the header (BYTES).
	 * 
	 * @return Index of the new file.
	 */
	public final ElementaryFile createElementaryFile(short offset, short size, byte[] header, short headerOffset,
			short headerLength) {
		if (_childrenCount == MAX_CHILDREN) {
			return null;
		}

		ElementaryFile file = _fileSystem.getFreeEF();
		if (file != null) {
			file.setup(this, offset, size, header, headerOffset, headerLength);

			_children[_childrenCount] = file;
			_childrenCount++;
		}

		return file;
	}

	/**
	 * Deletes a file by its FID.
	 * 
	 * @param fid FID of the file.
	 * 
	 * @return false is the file is not found.
	 */
	public final boolean deleteFile(short fid) {
		File file = findFileByFileId(fid);
		if (file == null) {
			return false;
		}

		file.release();

		// Update the children
		byte fileIndex = 0;
		while (fileIndex < _childrenCount && _children[fileIndex] != file) {
			fileIndex++;
		}

		for (byte i = (byte) (fileIndex + 1); i < _childrenCount; i++) {
			_children[(byte) (i - 1)] = _children[i];
		}
		_childrenCount--;
		_children[_childrenCount] = null;

		return true;
	}

	/**
	 * Find the file in DF having a given file identifier.
	 * 
	 * @return null if not found or the found {@link File} instance.
	 */
	public final File findFileByFileId(short fid) {
		byte index = 0;
		while (index < _childrenCount) {
			if (_children[index].getFileId() == fid) {
				return _children[index];
			}
			index++;
		}

		return null;
	}

	/**
	 * Returns the nth child file. A call to hasChild should be done to verify the
	 * existence.
	 * 
	 * @param nth Index of the file in the DF.
	 * 
	 * @param nth Index of the child (starts at 0)
	 */
	public final File getChild(byte nth) {
		return _children[nth];
	}

	/**
	 * Returns true if the nth child file exists.
	 * 
	 * @param nth Index of the file in the DF.
	 */
	public final boolean hasChild(byte nth) {
		return nth >= 0 && nth < _childrenCount;
	}
}
