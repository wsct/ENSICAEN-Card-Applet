package fr.ensicaen.smartcards.tb100like;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemTest {

    @Test
    void createFileSystem() {
        short size = 0x100;
        FileSystem fileSystem = new FileSystem(size, (byte) 2, (byte) 3);

        assertEquals(size * 4, fileSystem.memory.length);
    }

    @Test
    void eraseBytesInMemory() {
        FileSystem fileSystem = new FileSystem((short) 0x8, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) '0', (byte) '1', (byte) '2', (byte) '3',
                (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
                (byte) 'C', (byte) 'D', (byte) 'D', (byte) 'F'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x04;
        // Words in memory: F W W W   W F F F
        fileSystem.write(source, sourceOffset, offset, length);

        // Words in memory: F W F F   W F F F
        fileSystem.erase((short) 0x08, (short) 0x08);

        byte[] expected = new byte[]{
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                (byte) '0', (byte) '1', (byte) '2', (byte) '3',
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                (byte) 'C', (byte) 'D', (byte) 'D', (byte) 'F',
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE
        };
        assertArrayEquals(expected, fileSystem.memory);
        assertEquals(2, fileSystem.getFreeLength((short) 0x02, (short) 0x04));
    }

    @Test
    void getFreeDF() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);

        DedicatedFile df1 = fileSystem.getFreeDF();

        // Same instance is returned because it is not yet used
        assertEquals(df1, fileSystem.getFreeDF());

        df1.setup(null, (short) 0, (short) 0x10, new byte[]{}, (short) 0, (short) 0);
        DedicatedFile df2 = fileSystem.getFreeDF();

        // New instance is returned because df1 is now used
        assertNotEquals(df1, df2);

        df2.setup(null, (short) 0x20, (short) 0x10, new byte[]{}, (short) 0, (short) 0);

        // Max DF reached
        assertNull(fileSystem.getFreeDF());
    }

    @Test
    void getFreeEF() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 1, (byte) 2);

        ElementaryFile ef1 = fileSystem.getFreeEF();

        // Same instance is returned because it is not yet used
        assertEquals(ef1, fileSystem.getFreeEF());

        ef1.setup(null, (short) 0, (short) 0x10, new byte[]{}, (short) 0, (short) 0);
        ElementaryFile ef2 = fileSystem.getFreeEF();

        // New instance is returned because ef1 is now used
        assertNotEquals(ef1, ef2);

        ef2.setup(null, (short) 0x20, (short) 0x10, new byte[]{}, (short) 0, (short) 0);

        // Max EF reached
        assertNull(fileSystem.getFreeEF());
    }

    @Test
    void getFreeLength() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) '0', (byte) '1', (byte) '2', (byte) '3',
                (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'A', (byte) 'B'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x10;
        // Words in memory: F F F F   W W W F   F F F F   F F F F
        fileSystem.write(source, sourceOffset, offset, length);

        assertEquals(0, fileSystem.getWrittenLength((short) 0, (short) 16));
        assertEquals(3, fileSystem.getWrittenLength((short) 4, (short) 16));
        assertEquals(1, fileSystem.getWrittenLength((short) 6, (short) 16));
        assertEquals(0, fileSystem.getWrittenLength((short) 7, (short) 16));
    }

    @Test
    void getWrittenLength() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) '0', (byte) '1', (byte) '2', (byte) '3',
                (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'A', (byte) 'B'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x10;
        // Words in memory: F F F F   W W W F   F F F F   F F F F
        fileSystem.write(source, sourceOffset, offset, length);

        assertEquals(4, fileSystem.getFreeLength((short) 0, (short) 16));
        assertEquals(1, fileSystem.getFreeLength((short) 3, (short) 16));
        assertEquals(0, fileSystem.getFreeLength((short) 5, (short) 16));
        assertEquals(9, fileSystem.getFreeLength((short) 7, (short) 16));
    }

    @Test
    void readBytesInMemory() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) '1', (byte) '2', (byte) '3', (byte) '4'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x10;
        fileSystem.write(source, sourceOffset, offset, length);

        short readOffset = (short) (offset - 4);
        short readLength = (short) (length + 8);
        byte[] output = new byte[readLength];
        fileSystem.read(readOffset, output, (short) 0, readLength, false);

        byte[] expected = new byte[]{
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                (byte) '1', (byte) '2', (byte) '3', (byte) '4',
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE
        };
        assertArrayEquals(expected, output);
    }

    @Test
    void secureReadBytesInMemory() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) '1', (byte) '2', (byte) '3', (byte) '4'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x10;
        fileSystem.write(source, sourceOffset, offset, length);

        short readOffset = (short) (offset - 4);
        short readLength = (short) (length + 8);
        byte[] output = new byte[readLength];
        fileSystem.read(readOffset, output, (short) 0, readLength, true);

        byte[] expected = new byte[]{
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.WRITTEN_BYTE, FileSystem.WRITTEN_BYTE, FileSystem.WRITTEN_BYTE, FileSystem.WRITTEN_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE
        };
        assertArrayEquals(expected, output);
    }

    @Test
    void writeBytesInMemory() {
        FileSystem fileSystem = new FileSystem((short) 0x100, (byte) 2, (byte) 3);
        byte[] source = new byte[]{
                (byte) 'X', (byte) 'X', (byte) 'X', (byte) 'X',
                (byte) '1', (byte) '2', (byte) '3', (byte) '4'
        };
        short sourceOffset = (short) 0x04;
        short length = (short) (source.length - sourceOffset);
        short offset = (short) 0x10;

        fileSystem.write(source, sourceOffset, offset, length);

        byte[] memoryExpected = new byte[]{
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                (byte) '1', (byte) '2', (byte) '3', (byte) '4',
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
                FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE, FileSystem.FREE_BYTE,
        };
        byte[] memoryExtract = new byte[memoryExpected.length];
        System.arraycopy(fileSystem.memory, 0, memoryExtract, 0, memoryExpected.length);
        assertArrayEquals(memoryExpected, memoryExtract);
        byte[] attributesExpected = new byte[]{
                FileSystem.ATTRIBUTE_FREE, FileSystem.ATTRIBUTE_FREE, FileSystem.ATTRIBUTE_FREE, FileSystem.ATTRIBUTE_FREE,
                FileSystem.ATTRIBUTE_WRITTEN, FileSystem.ATTRIBUTE_FREE, FileSystem.ATTRIBUTE_FREE, FileSystem.ATTRIBUTE_FREE
        };
        byte[] attributesExtract = new byte[0x08];
        System.arraycopy(fileSystem.attributes, 0, attributesExtract, 0, attributesExpected.length);
        assertArrayEquals(attributesExpected, attributesExtract);
    }
}