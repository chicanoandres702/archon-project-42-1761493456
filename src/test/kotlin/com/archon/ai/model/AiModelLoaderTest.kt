package com.archon.ai.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AiModelLoaderTest {

    private val loader = AiModelLoader()

    private val validConfig = ModelConfig(
        name = "TestModelV1",
        expectedInputShape = listOf(1, 224, 224, 3),
        modelFilePath = "/path/to/test_model.tflite"
    )

    // --- Validation Logic Tests ---

    @Test
    fun `validateInputShape returns true for identical shapes`() {
        val expected = listOf(1, 100, 100)
        val actual = listOf(1, 100, 100)
        assertTrue(loader.validateInputShape(expected, actual))
    }

    @Test
    fun `validateInputShape returns false when shape size mismatches`() {
        val expected = listOf(1, 100)
        val actual = listOf(1, 100, 3)
        assertFalse(loader.validateInputShape(expected, actual))
    }

    @Test
    fun `validateInputShape returns false when dimension value mismatches`() {
        val expected = listOf(1, 224, 224, 3)
        val actual = listOf(1, 224, 225, 3) // Mismatch on third dimension
        assertFalse(loader.validateInputShape(expected, actual))
    }

    // --- Load Model Tests ---

    @Test
    fun `loadModel successfully loads model with correct shape`() {
        val runtimeShape = listOf(1, 224, 224, 3)
        val loadedModel = loader.loadModel(validConfig, runtimeShape)

        assertNotNull(loadedModel)
        assertEquals(validConfig.name, loadedModel.config.name)
        // Verify that the mocked initialization created a handle
        assertEquals(validConfig.modelFilePath.hashCode(), loadedModel.handle)
    }

    @Test
    fun `loadModel throws InputShapeValidationException on shape mismatch`() {
        val runtimeShape = listOf(1, 224, 224, 4) // Wrong channel count
        
        val exception = assertThrows<InputShapeValidationException> {
            loader.loadModel(validConfig, runtimeShape)
        }

        assertTrue(exception.message!!.contains("Input shape mismatch for model 'TestModelV1'"))
    }
}