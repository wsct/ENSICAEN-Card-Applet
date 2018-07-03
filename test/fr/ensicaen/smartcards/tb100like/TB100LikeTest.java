package fr.ensicaen.smartcards.tb100like;

import fr.ensicaen.smartcards.helpers.ApduBuffer;
import fr.ensicaen.smartcards.helpers.Helpers;
import javacard.framework.APDU;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TB100LikeTest {

    @Test
    void processAppletSelection() {
        ApduBuffer buffer = new ApduBuffer("00A404000B");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());

        TB100Like applet = new TB100Like();
        applet.processAppletSelection(apdu);

        // Intercept and verify outgoing buffer parameters
        ArgumentCaptor<Short> offset = ArgumentCaptor.forClass(Short.class);
        ArgumentCaptor<Short> length = ArgumentCaptor.forClass(Short.class);
        verify(apdu).setOutgoingAndSend(offset.capture(), length.capture());
        assertEquals((short) 0, (short) offset.getValue());
        assertEquals((short) Constants.MF_HEADER.length, (short) length.getValue());

        // Verify APDU response content
        assertArrayEquals(Constants.MF_HEADER, buffer.getRange(length.getValue()));
        // No status word as we're faking JC.
    }

    @Test
    void processCreateDedicatedFile() {
        ApduBuffer buffer = new ApduBuffer("00E0001308");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());
        when(apdu.setIncomingAndReceive()).thenAnswer(invocationOnMock -> {
            buffer.set("00E00013087F000022FFFFFE62").get();
            return (short) 8;
        });

        TB100Like applet = new TB100Like();
        applet.process(apdu);

        // Verify APDU response content
        // No status word as we're faking JC.
    }

    @Test
    void processCreateElementaryFile() {
        ApduBuffer buffer = new ApduBuffer("00E0004008");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());
        when(apdu.setIncomingAndReceive()).thenAnswer(invocationOnMock -> {
            buffer.set("00E00040082F010010FFFFFF83").get();
            return (short) 8;
        });

        TB100Like applet = new TB100Like();
        applet.process(apdu);

        // Verify APDU response content
        // No status word as we're faking JC.
    }

    @Test
    void processDeleteDedicatedFile() {
        ApduBuffer buffer = new ApduBuffer("00E4000002");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());
        when(apdu.setIncomingAndReceive()).thenAnswer(invocationOnMock -> {
            buffer.set("7F00").get();
            return (short) 2;
        });

        TB100Like applet = new TB100Like();
        applet._masterFile.createDedicatedFile((short) 0x04, (short) 0x20, Helpers.hexaToBytes("7F000022FFFFFE62"), (short) 0, (short) 8);

        applet.process(apdu);

        // Verify APDU response content
        // No status word as we're faking JC.
    }

    @Test
    void processMasterFileSelection() {
        ApduBuffer buffer = new ApduBuffer("00A4000002");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());
        when(apdu.setIncomingAndReceive()).thenAnswer(invocationOnMock -> {
            buffer.set("00A40000023F00").get();
            return (short) 2;
        });

        TB100Like applet = new TB100Like();
        applet.process(apdu);

        // Intercept and verify outgoing buffer parameters
        ArgumentCaptor<Short> offset = ArgumentCaptor.forClass(Short.class);
        ArgumentCaptor<Short> length = ArgumentCaptor.forClass(Short.class);
        verify(apdu).setOutgoingAndSend(offset.capture(), length.capture());
        assertEquals((short) 0, (short) offset.getValue());
        assertEquals((short) (4 + Constants.MF_HEADER.length), (short) length.getValue());

        // Verify APDU response content
        assertArrayEquals(Helpers.hexaToBytes("0000029F"), buffer.getRange((short) 4));
        assertArrayEquals(Constants.MF_HEADER, buffer.getRange((short) 4, (short) Constants.MF_HEADER.length));
        // No status word as we're faking JC.
    }
}
