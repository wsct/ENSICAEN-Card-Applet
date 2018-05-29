package fr.ensicaen.smartcards.tb100like;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeaderParserTest {

    @Test
    void parseMasterFileHeader() {
        byte[] buffer = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x9F, (byte) 0x3F, (byte) 0x00, (byte) 0x02, (byte) 0xA1, (byte) 0xFF, (byte) 0xFF, (byte) 0x9E, (byte) 0x81, (byte) 0x90, (byte) 0x00
        };

        HeaderParser parser = new HeaderParser();
        parser.parse(buffer, (short) 4);

        assertEquals(HeaderParser.FILETYPE_DF, parser.fileType);
        assertEquals((short) 0x3F00, parser.fileIdentifier);
        assertEquals((short) 8, parser.headerLength);
        assertEquals((short) 0x029F, parser.bodyLength);
    }

    @Test
    void parseDFHeader() {
        byte[] buffer = new byte[]{
                (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0x62
        };

        HeaderParser parser = new HeaderParser();
        parser.parse(buffer, (short) 0);

        assertEquals(HeaderParser.FILETYPE_DF, parser.fileType);
        assertEquals((short) 0x7F00, parser.fileIdentifier);
        assertEquals((short) 8, parser.headerLength);
        assertEquals((short) 0x0020, parser.bodyLength);
    }

    @Test
    void parseEFWZHeader() {
        byte[] buffer = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0E, (byte) 0x6F, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x83
        };

        HeaderParser parser = new HeaderParser();
        parser.parse(buffer, (short) 4);

        assertEquals(HeaderParser.FILETYPE_EFWZ, parser.fileType);
        assertEquals((short) 0x6F01, parser.fileIdentifier);
        assertEquals((short) 8, parser.headerLength);
        assertEquals((short) 0x000E, parser.bodyLength);
    }

    @Test
    void parseEFSZHeader() {
        byte[] buffer = new byte[]{
                (byte) 0x0E, (byte) 0x10, (byte) 0x03, (byte) 0xDF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };

        HeaderParser parser = new HeaderParser();
        parser.parse(buffer, (short) 0);

        assertEquals(HeaderParser.FILETYPE_EFSZ, parser.fileType);
        assertEquals((short) 0x0E10, parser.fileIdentifier);
        assertEquals((short) 4, parser.headerLength);
        assertEquals((short) 3, parser.bodyLength);
    }
}