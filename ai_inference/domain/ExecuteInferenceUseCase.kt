package com.archon.ai_inference.domain

import com.archon.ai_inference.data.InferenceRepository
import javax.inject.Inject

/**
 * Use Case for orchestrating the AI model execution process.
 * This encapsulates the business logic accessed by the Worker.
 */
internal class ExecuteInferenceUseCase @Inject constructor(
    private val inferenceRepository: InferenceRepository
) {
    internal data class InferenceInput(val rawInput: String)
    
    /**
     * Executes the inference using the provided input data.
     */
    suspend operator fun invoke(input: InferenceInput): Result<String> {
        // Business Rule: Ensure input is validated before costly execution
        if (input.rawInput.isBlank()) {
            return Result.failure(IllegalArgumentException("Inference input cannot be empty or null."))
        }
        
        // Delegate execution to the data layer
        return inferenceRepository.executeScalingModel(input.rawInput)
    }
}