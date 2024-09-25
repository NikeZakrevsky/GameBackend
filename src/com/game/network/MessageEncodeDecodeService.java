package com.game.network;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageEncodeDecodeService {

    public String decodeMessage(ByteBuffer buffer) {
        // Read the first byte to determine the frame info
        byte b = buffer.get();
        boolean isFin = (b & 0x80) != 0; // FIN bit
        int opcode = b & 0x0F; // Opcode

        // Read the second byte for masking and payload length
        b = buffer.get();
        boolean isMasked = (b & 0x80) != 0; // Mask bit
        int payloadLength = b & 0x7F; // Payload length
        // Handle payload length for cases greater than 125
        if (payloadLength == 126) {
            payloadLength = buffer.getShort() & 0xFFFF; // Read next two bytes
        } else if (payloadLength == 127) {
            buffer.position(buffer.position() + 8); // Skip for large payloads (not commonly used)
        }

        // Read masking key if it is masked
        byte[] maskingKey = null;
        if (isMasked) {
            maskingKey = new byte[4];
            buffer.get(maskingKey);
        }

        // Read the payload data
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        // Unmask the payload
        if (isMasked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= maskingKey[i % 4]; // Unmasking
            }
        }

        // Convert to string and return
        return new String(payload, StandardCharsets.UTF_8);
    }

    public ByteBuffer encodeMessage(String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // Calculate the length of the message
        int messageLength = messageBytes.length;
        ByteBuffer buffer;

        // Determine the size of the buffer based on the length of the message
        if (messageLength <= 125) {
            // 1 byte for the control byte + 1 byte for the length + message length
            buffer = ByteBuffer.allocate(1 + 1 + messageLength);
            buffer.put((byte) 0x81); // FIN + text frame
            buffer.put((byte) messageLength); // Length of the message
        } else if (messageLength >= 126 && messageLength <= 65535) {
            // 1 byte for the control byte + 1 byte for the extended length + 2 bytes for the length + message length
            buffer = ByteBuffer.allocate(1 + 1 + 2 + messageLength);
            buffer.put((byte) 0x81); // FIN + text frame
            buffer.put((byte) 126); // Length is 126 or more but less than 65536
            buffer.putShort((short) messageLength); // 2 bytes for the length
        } else {
            // 1 byte for the control byte + 1 byte for the extended length + 8 bytes for the length + message length
            buffer = ByteBuffer.allocate(1 + 1 + 8 + messageLength);
            buffer.put((byte) 0x81); // FIN + text frame
            buffer.put((byte) 127); // Length is greater than 65535
            buffer.putLong(messageLength); // 8 bytes for the length
        }

        buffer.put(messageBytes); // Message content
        buffer.flip(); // Prepare for writing

        return buffer;
    }
}
