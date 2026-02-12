package com.archon.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * Mock executor to capture commands and simulate results/failures without requiring actual FFmpeg installation.
 */
class MockCommandExecutor(
    var mockResult: ExecutionResult = ExecutionResult(0, "Mock Success Output"),
    var capturedCommand: List<String>? = null,
    var shouldThrow: Boolean = false
) : CommandExecutor {
    override fun execute(command: List<String>): ExecutionResult {
        capturedCommand = command
        if (shouldThrow) {
            // Simulate failure during process startup/timeout
            throw FFmpegExecutionException("Simulated execution failure")
        }
        return mockResult
    }
}

class FFmpegUtilityTest {

    private lateinit var mockExecutor: MockCommandExecutor
    private lateinit var ffMpegUtility: FFmpegUtility

    @TempDir
    lateinit var tempDir: Path

    private lateinit var inputFile: File

    @BeforeEach
    fun setUp() {
        mockExecutor = MockCommandExecutor()
        ffMpegUtility = FFmpegUtility(mockExecutor)

        // Create a dummy input file for existence checks
        inputFile = tempDir.resolve("input.tiff").toFile()
        inputFile.createNewFile()
    }

    // --- Tests for runFFmpeg execution safety and error handling ---

    @Test
    fun `runFFmpeg executes command correctly and returns output`() {
        val testArgs = listOf("-version")
        val output = ffMpegUtility.runFFmpeg(testArgs)

        assertTrue(mockExecutor.capturedCommand!!.contains("ffmpeg"))
        assertTrue(mockExecutor.capturedCommand!!.contains("-version"))
        assertEquals("Mock Success Output", output)
    }

    @Test
    fun `runFFmpeg throws FFmpegExecutionException on non-zero exit code`() {
        mockExecutor.mockResult = ExecutionResult(1, "Error details")

        val exception = assertThrows<FFmpegExecutionException> {
            ffMpegUtility.runFFmpeg(listOf("-i", "bad_path"))
        }
        assertTrue(exception.message!!.contains("failed with exit code 1"))
    }

    @Test
    fun `runFFmpeg throws FFmpegExecutionException on underlying execution failure`() {
        mockExecutor.shouldThrow = true
        assertThrows<FFmpegExecutionException> {
            ffMpegUtility.runFFmpeg(listOf())
        }
    }

    // --- Tests for convertImage command generation ---

    @Test
    fun `convertImage throws IllegalArgumentException if source file does not exist`() {
        val nonExistentFile = tempDir.resolve("non_existent.jpg").toFile()
        val outputFile = tempDir.resolve("output.webp").toFile()

        assertThrows<IllegalArgumentException> {
            ffMpegUtility.convertImage(nonExistentFile, outputFile, "webp")
        }
    }

    @Test
    fun `convertImage generates correct command for WEBP conversion with quality 80`() {
        val targetFileWebp = tempDir.resolve("output.webp").toFile()
        ffMpegUtility.convertImage(inputFile, targetFileWebp, "WEBP", 80)

        val command = mockExecutor.capturedCommand!!
        
        // Command structure verification
        assertTrue(command.contains(inputFile.absolutePath))
        assertTrue(command.contains(targetFileWebp.absolutePath))
        assertTrue(command.contains("-q:v"))
        assertTrue(command.contains("80"))
    }

    @Test
    fun `convertImage generates correct command for PNG conversion with high quality`() {
        val targetFilePng = tempDir.resolve("output.png").toFile()
        ffMpegUtility.convertImage(inputFile, targetFilePng, "PNG", 95) // Quality 95 maps to compression level 9

        val command = mockExecutor.capturedCommand!!
        
        // PNG uses -compression_level
        assertTrue(command.contains("-compression_level"))
        assertTrue(command.contains("9"))
    }

    @Test
    fun `convertImage uses default compression level for PNG if quality is null`() {
        val targetFilePng = tempDir.resolve("output.png").toFile()
        ffMpegUtility.convertImage(inputFile, targetFilePng, "PNG", null) 

        val command = mockExecutor.capturedCommand!!
        
        // Default level is 6
        assertTrue(command.contains("-compression_level"))
        assertTrue(command.contains("6"))
    }

    @Test
    fun `convertImage uses default quality settings for JPG if quality is null`() {
        val targetFileJpg = tempDir.resolve("output.jpg").toFile()
        ffMpegUtility.convertImage(inputFile, targetFileJpg, "JPG", null)

        val command = mockExecutor.capturedCommand!!
        
        // Should not contain explicit quality flags (-q:v), relying on ffmpeg default
        assertFalse(command.contains("-q:v"))
        
        assertTrue(command.contains(inputFile.absolutePath))
        assertTrue(command.contains(targetFileJpg.absolutePath))
    }

    @Test
    fun `convertImage handles mixed case extensions correctly`() {
        val targetFileWebp = tempDir.resolve("output.WEBP").toFile()
        ffMpegUtility.convertImage(inputFile, targetFileWebp, "wEbP", 50)

        val command = mockExecutor.capturedCommand!!
        
        assertTrue(command.contains("-q:v"))
        assertTrue(command.contains("50"))
    }

    @Test
    fun `convertImage handles non-specialized extensions`() {
        val targetFileTiff = tempDir.resolve("output.tiff").toFile()
        ffMpegUtility.convertImage(inputFile, targetFileTiff, "TIFF", 50)

        val command = mockExecutor.capturedCommand!!
        
        // Should not contain quality flags
        assertFalse(command.contains("-q:v"))
        assertFalse(command.contains("-compression_level"))
        
        assertTrue(command.contains(inputFile.absolutePath))
        assertTrue(command.contains(targetFileTiff.absolutePath))
    }
}