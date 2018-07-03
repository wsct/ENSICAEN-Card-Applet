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

    private TB100Like applet = new TB100Like();

    @Test
    void processAppletSelection() {
        ApduBuffer buffer = new ApduBuffer("00A404000B");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());

        applet.processAppletSelection(apdu);

        // Intercept and verify outgoing buffer parameters
        ArgumentCaptor<Short> offset = ArgumentCaptor.forClass(Short.class);
        ArgumentCaptor<Short> length = ArgumentCaptor.forClass(Short.class);
        verify(apdu).setOutgoingAndSend(offset.capture(), length.capture());
        assertEquals((short) 0, (short) offset.getValue());
        assertEquals((short) Constants.MF_HEADER.length, (short) length.getValue());

        // Verify APDU response content
        assertArrayEquals(Constants.MF_HEADER, buffer.getRange(length.getValue()));
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
    }
}
