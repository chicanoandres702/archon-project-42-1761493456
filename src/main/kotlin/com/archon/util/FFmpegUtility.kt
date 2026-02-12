package com.archon.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Custom exception for FFmpeg operational errors.
 */
class FFmpegExecutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

interface CommandExecutor {
    fun execute(command: List<String>): ExecutionResult
}

data class ExecutionResult(
    val exitCode: Int,
    val output: String
)

/**
 * Executes a system command using ProcessBuilder.
 * @param timeoutSeconds Maximum time allowed for command execution.
 */
class RealCommandExecutor(private val timeoutSeconds: Long = 60L) : CommandExecutor {
    override fun execute(command: List<String>): ExecutionResult {
        
        // Log the command structure (replace with proper logging framework in production)
        println("Executing command: ${command.joinToString(" ")}") 

        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true) // Merge stderr into stdout
                .start()

            val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }

            if (!finished) {
                process.destroyForcibly()
                throw FFmpegExecutionException("Command timed out after $timeoutSeconds seconds.")
            }

            val exitCode = process.exitValue()
            
            return ExecutionResult(exitCode, output)
        } catch (e: Exception) {
            throw FFmpegExecutionException("Error executing process: ${e.message}", e)
        }
    }
}

/**
 * Utility class for safely wrapping FFmpeg command execution for post-processing tasks.
 * Relies on an injectable CommandExecutor for process execution.
 * Assumes 'ffmpeg' is accessible in the execution environment.
 */
class FFmpegUtility(private val executor: CommandExecutor) {

    private val FFMPEG_COMMAND = "ffmpeg"

    /**
     * Executes a full command sequence and validates the exit code.
     * Arguments must be provided as a list of strings to prevent shell injection.
     * @param args The list of arguments following 'ffmpeg'.
     * @throws FFmpegExecutionException if the command fails or times out.
     */
    fun runFFmpeg(args: List<String>): String {
        val fullCommand = mutableListOf(FFMPEG_COMMAND).apply { addAll(args) }
        
        val result = executor.execute(fullCommand)

        if (result.exitCode != 0) {
            throw FFmpegExecutionException("FFmpeg command failed with exit code ${result.exitCode}. Output:\n${result.output}")
        }
        return result.output
    }

    /**
     * Converts a source image file to a specified target format and compression settings.
     *
     * @param sourceFile The input file path.
     * @param targetFile The output file path.
     * @param extension The target file extension (e.g., "png", "webp", "jpg"). Case-insensitive.
     * @param quality Optional quality setting for lossy formats (1-100). Ignored if null or for lossless formats.
     */
    fun convertImage(
        sourceFile: File,
        targetFile: File,
        extension: String,
        quality: Int? = null
    ) {
        if (!sourceFile.exists()) {
            throw IllegalArgumentException("Source file does not exist: ${sourceFile.absolutePath}")
        }

        // Use absolute paths
        val args = mutableListOf(
            "-i", sourceFile.absolutePath,
            "-y" // Overwrite output files without asking
        )

        val normalizedExtension = extension.toLowerCase()

        when (normalizedExtension) {
            "png" -> {
                // PNG compression level (1-9). Quality mapping to compression.
                val compressionLevel = quality?.let { (it / 10).coerceIn(1, 9) } ?: 6
                args.addAll(listOf("-compression_level", compressionLevel.toString()))
            }
            "webp" -> {
                // WEBP standard quality (0-100)
                quality?.let {
                    args.addAll(listOf("-q:v", it.coerceIn(0, 100).toString()))
                }
            }
            "jpeg", "jpg" -> {
                // JPEG quality (1-100)
                quality?.let {
                    args.addAll(listOf("-q:v", it.coerceIn(1, 100).toString()))
                }
            }
            else -> {
                // Default handling for other extensions (e.g., tiff, bmp) using standard ffmpeg defaults
            }
        }

        // Output file path is the last argument defining the container format
        args.add(targetFile.absolutePath)

        runFFmpeg(args)
    }
}