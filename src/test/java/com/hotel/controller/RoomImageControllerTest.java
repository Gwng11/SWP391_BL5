package com.hotel.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomImageControllerTest {
    @Test
    void acceptsExpectedJpegPngAndWebpSignatures() {
        assertTrue(RoomImageController.hasExpectedSignature(
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, "image/jpeg"));
        assertTrue(RoomImageController.hasExpectedSignature(
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, "image/png"));
        assertTrue(RoomImageController.hasExpectedSignature(
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'}, "image/webp"));
    }

    @Test
    void rejectsSpoofedOrTruncatedUploads() {
        assertFalse(RoomImageController.hasExpectedSignature("not-an-image".getBytes(), "image/png"));
        assertFalse(RoomImageController.hasExpectedSignature(new byte[]{(byte) 0xFF}, "image/jpeg"));
        assertFalse(RoomImageController.hasExpectedSignature(null, "image/webp"));
    }
}
