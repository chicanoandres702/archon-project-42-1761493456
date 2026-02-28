package com.archon.ai.preprocessing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class ImageProcessorTest {

    private ImageProcessor processor;
    private static final int WIDTH = 2;
    private static final int HEIGHT = 2;
    private static final float EPSILON = 0.0001f; // Tolerance for float comparisons

    @BeforeEach
    void setUp() {
        processor = new ImageProcessor(WIDTH, HEIGHT);
    }

    @Test
    void constructor_invalidDimensions_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ImageProcessor(0, 100));
        assertThrows(IllegalArgumentException.class, () -> new ImageProcessor(100, -1));
    }

    // --- Testing convertRawToPixels (Simulated Conversion) ---

    @Test
    void convertRawToPixels_validInput_returnsCorrectSize() {
        byte[] mockJpegData = new byte[100];
        int[] pixels = processor.convertRawToPixels(mockJpegData);
        assertEquals(WIDTH * HEIGHT, pixels.length);
    }

    @Test
    void convertRawToPixels_emptyInput_throwsException() {
        byte[] emptyData = new byte[0];
        assertThrows(IllegalArgumentException.class, () -> {
            processor.convertRawToPixels(emptyData);
        });
    }

    // --- Testing normalizeAndScale (Core Normalization Logic) ---

    @Test
    void normalizeAndScale_whitePixel_normalizesToPositiveOne() {
        // ARGB: 0xFFFFFFFF (R=255, G=255, B=255)
        int whitePixel = 0xFFFFFFFF;
        int[] pixels = {whitePixel};

        ImageProcessor oneByOneProcessor = new ImageProcessor(1, 1);
        FloatBuffer result = oneByOneProcessor.normalizeAndScale(pixels);

        assertEquals(3, result.capacity()); 
        
        // (255 - 127.5) / 127.5 = 1.0
        assertEquals(1.0f, result.get(0), EPSILON); // R
        assertEquals(1.0f, result.get(1), EPSILON); // G
        assertEquals(1.0f, result.get(2), EPSILON); // B
    }

    @Test
    void normalizeAndScale_blackPixel_normalizesToNegativeOne() {
        // ARGB: 0xFF000000 (R=0, G=0, B=0)
        int blackPixel = 0xFF000000;
        int[] pixels = {blackPixel};

        ImageProcessor oneByOneProcessor = new ImageProcessor(1, 1);
        FloatBuffer result = oneByOneProcessor.normalizeAndScale(pixels);

        // (0 - 127.5) / 127.5 = -1.0
        assertEquals(-1.0f, result.get(0), EPSILON); // R
        assertEquals(-1.0f, result.get(1), EPSILON); // G
        assertEquals(-1.0f, result.get(2), EPSILON); // B
    }

    @Test
    void normalizeAndScale_sizeMismatch_throwsException() {
        int[] wrongSizePixels = new int[WIDTH * HEIGHT + 1]; 
        assertThrows(IllegalArgumentException.class, () -> {
            processor.normalizeAndScale(wrongSizePixels);
        });
    }
    
    @Test
    void normalizeAndScale_mixedPixels_correctOrderAndNormalization() {
        // 2x2 image = 4 pixels * 3 channels = 12 floats expected
        
        // P1: White (255, 255, 255) -> (1.0, 1.0, 1.0)
        // P2: Black (0, 0, 0) -> (-1.0, -1.0, -1.0)
        // P3: Red (255, 0, 0) -> (1.0, -1.0, -1.0)

        int[] pixels = {
            0xFFFFFFFF, // P1: White
            0xFF000000, // P2: Black
            0xFFFF0000, // P3: Red
            0xFF0000FF  // P4: Blue (0, 0, 255) -> (-1.0, -1.0, 1.0)
        };

        FloatBuffer result = processor.normalizeAndScale(pixels);
        
        assertEquals(12, result.capacity()); 
        
        // P1 (RGB)
        assertEquals(1.0f, result.get(0), EPSILON);
        assertEquals(1.0f, result.get(1), EPSILON);
        assertEquals(1.0f, result.get(2), EPSILON);
        
        // P2 (RGB)
        assertEquals(-1.0f, result.get(3), EPSILON);
        assertEquals(-1.0f, result.get(4), EPSILON);
        assertEquals(-1.0f, result.get(5), EPSILON);
        
        // P3 (RGB)
        assertEquals(1.0f, result.get(6), EPSILON); // R
        assertEquals(-1.0f, result.get(7), EPSILON); // G
        assertEquals(-1.0f, result.get(8), EPSILON); // B

        // P4 (RGB)
        assertEquals(-1.0f, result.get(9), EPSILON); // R
        assertEquals(-1.0f, result.get(10), EPSILON); // G
        assertEquals(1.0f, result.get(11), EPSILON); // B
    }
}