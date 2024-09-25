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

        // Вычисляем длину сообщения
        int messageLength = messageBytes.length;
        ByteBuffer buffer;

        // Определяем размер буфера в зависимости от длины сообщения
        if (messageLength <= 125) {
            buffer = ByteBuffer.allocate(1 + 1 + messageLength);
            buffer.put((byte) 0x81); // FIN + текстовый фрейм
            buffer.put((byte) messageLength); // Длина сообщения
        } else if (messageLength >= 126 && messageLength <= 65535) {
            buffer = ByteBuffer.allocate(1 + 2 + messageLength);
            buffer.put((byte) 0x81); // FIN + текстовый фрейм
            buffer.put((byte) 126); // длина сообщения больше 125, но меньше 65536
            buffer.putShort((short) messageLength); // 2 байта для длины
        } else {
            buffer = ByteBuffer.allocate(1 + 8 + messageLength);
            buffer.put((byte) 0x81); // FIN + текстовый фрейм
            buffer.put((byte) 127); // длина сообщения больше 65535
            buffer.putLong(messageLength); // 8 байт для длины
        }

        buffer.put(messageBytes); // Содержимое сообщения
        buffer.flip(); // Подготовка к записи

        return buffer;
    }
}
