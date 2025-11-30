package com.example.ai.image;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Service responsible for converting AI model output tensors into high-resolution Bitmaps (>= 4K)
 * and saving them to permanent storage.
 */
public class ImageProcessorService {

    private static final String TAG = "ImageProcessorService";
    // Minimum 4K UHD dimensions
    private static final int MIN_4K_WIDTH = 3840;
    private static final int MIN_4K_HEIGHT = 2160;

    /**
     * Converts a model output tensor buffer (e.g., FloatBuffer) back into a Bitmap.
     * NOTE: This is a placeholder implementation. Real implementation requires specific knowledge
     * of the tensor data layout (channel order, normalization, size) specific to the ML framework.
     *
     * @param tensorBuffer The raw output buffer from the AI model (or null if not available).
     * @param width The intended width of the output image before scaling.
     * @param height The intended height of the output image before scaling.
     * @return The resulting Bitmap, or null if conversion fails.
     */
    public Bitmap convertTensorToBitmap(FloatBuffer tensorBuffer, int width, int height) {
        if (tensorBuffer == null || width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid tensor buffer or dimensions provided.");
            return null;
        }

        // --- Placeholder Implementation Start ---
        // In a real application, sophisticated logic involving reading tensorBuffer, 
        // color space conversion, and pixel packing would occur here.
        try {
            // Create a dummy bitmap for demonstration/testing the subsequent steps
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Log.i(TAG, "Tensor successfully converted to initial Bitmap: " + width + "x" + height);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error during tensor to bitmap conversion.", e);
            return null;
        }
        // --- Placeholder Implementation End ---
    }

    /**
     * Scales a Bitmap to meet or exceed 4K resolution (3840x2160).
     * If the bitmap is already 4K or larger, it is returned as is.
     *
     * @param inputBitmap The bitmap to scale.
     * @return The scaled Bitmap, or the original if no scaling is needed or if scaling fails.
     */
    public Bitmap scaleTo4K(Bitmap inputBitmap) {
        if (inputBitmap == null) {
            Log.e(TAG, "Input bitmap is null.");
            return null;
        }

        int currentWidth = inputBitmap.getWidth();
        int currentHeight = inputBitmap.getHeight();

        if (currentWidth >= MIN_4K_WIDTH && currentHeight >= MIN_4K_HEIGHT) {
            Log.d(TAG, "Bitmap already meets 4K requirements: " + currentWidth + "x" + currentHeight);
            return inputBitmap;
        }

        float scaleFactorWidth = (float) MIN_4K_WIDTH / currentWidth;
        float scaleFactorHeight = (float) MIN_4K_HEIGHT / currentHeight;

        // Choose the largest scale factor to ensure BOTH dimensions meet the minimum 4K requirement
        float scaleFactor = Math.max(scaleFactorWidth, scaleFactorHeight);

        // Calculate new dimensions (for logging purposes, actual dimensions handled by Matrix)
        int newWidth = Math.round(currentWidth * scaleFactor);
        int newHeight = Math.round(currentHeight * scaleFactor);

        Log.i(TAG, String.format("Scaling bitmap from %dx%d to %dx%d (Factor: %.2f)",
                currentWidth, currentHeight, newWidth, newHeight, scaleFactor));

        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);

        try {
            // Use filtering (true) for high-quality upscaling
            Bitmap scaledBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, currentWidth, currentHeight, matrix, true);
            
            // Note: Caller is responsible for recycling the input/intermediate bitmaps if they are no longer needed.
            
            return scaledBitmap;
        } catch (OutOfMemoryError e) {
            // Handle critical resource exhaustion gracefully, return original image or fail.
            Log.e(TAG, "Out of Memory during 4K scaling. Returning original bitmap.", e);
            return inputBitmap; 
        } catch (Exception e) {
            Log.e(TAG, "Error during bitmap scaling. Returning original bitmap.", e);
            return inputBitmap;
        }
    }

    /**
     * Saves the provided Bitmap to the specified file path as a high-quality JPEG.
     *
     * @param bitmap The bitmap to save.
     * @param outputFile The file path where the image should be saved.
     * @return True if successful, false otherwise.
     */
    public boolean saveBitmap(Bitmap bitmap, File outputFile) {
        if (bitmap == null || outputFile == null) {
            Log.e(TAG, "Bitmap or output file is null.");
            return false;
        }

        FileOutputStream out = null;
        boolean success = false;
        try {
            // Ensure parent directories exist
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory structure: " + parentDir.getAbsolutePath());
                    return false;
                }
            }

            out = new FileOutputStream(outputFile);
            
            // Compression quality set high (100) for generated AI output fidelity
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); 
            
            out.flush();
            success = true;
            Log.i(TAG, "Successfully saved image of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file: " + outputFile.getAbsolutePath(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream.", e);
                }
            }
        }
        return success;
    }

    /**
     * Executes the full pipeline: conversion, scaling, and saving.
     */
    public boolean processAndSaveHighResolutionImage(FloatBuffer tensorBuffer, int initialWidth, int initialHeight, File outputFile) {
        Bitmap initialBitmap = convertTensorToBitmap(tensorBuffer, initialWidth, initialHeight);
        if (initialBitmap == null) {
            return false;
        }

        Bitmap highResBitmap = scaleTo4K(initialBitmap);
        
        boolean success = saveBitmap(highResBitmap, outputFile);
        
        // Cleanup/Recycling considerations:
        // If the scaling created a new highResBitmap, and initialBitmap is no longer needed,
        // and highResBitmap is only needed for the save operation (and not displayed),
        // proper memory management should happen here (e.g., recycling unused bitmaps).
        
        return success;
    }
}