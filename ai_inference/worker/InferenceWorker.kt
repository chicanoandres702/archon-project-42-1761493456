package com.archon.ai_inference.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.archon.ai_inference.domain.ExecuteInferenceUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber // Assuming Timber for logging

/**
 * A CoroutineWorker responsible for executing the long-running AI scaling inference.
 * It delegates the actual business logic to the ExecuteInferenceUseCase.
 */
@HiltWorker
internal class InferenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val executeInferenceUseCase: ExecuteInferenceUseCase
) : CoroutineWorker(appContext, workerParams) {

    internal companion object {
        internal const val INPUT_KEY_DATA = "INPUT_RAW_DATA"
        internal const val OUTPUT_KEY_RESULT = "OUTPUT_INFERENCE_RESULT"
        internal const val OUTPUT_KEY_ERROR = "OUTPUT_INFERENCE_ERROR"
        internal const val TAG = "InferenceWorker"
    }

    override suspend fun doWork(): Result {
        // Retrieve input data required for inference
        val inputDataString = inputData.getString(INPUT_KEY_DATA)
        
        if (inputDataString.isNullOrBlank()) {
            Timber.e("$TAG: Execution attempted without required input data.")
            return Result.failure(
                workDataOf(OUTPUT_KEY_ERROR to "Required input data missing.")
            )
        }

        Timber.i("$TAG: Starting inference execution for data: ${inputDataString.take(20)}...")

        val input = ExecuteInferenceUseCase.InferenceInput(inputDataString)
        
        // Execute domain logic (Layered Architecture adherence)
        val executionResult = executeInferenceUseCase(input)
        
        return executionResult.fold(
            onSuccess = { output ->
                Timber.d("$TAG: Inference completed successfully.")
                // Return result to WorkManager
                Result.success(
                    workDataOf(OUTPUT_KEY_RESULT to output)
                )
            },
            onFailure = { throwable ->
                Timber.e(throwable, "$TAG: Inference failed.")
                // Return failure status and error details
                Result.failure(
                    workDataOf(OUTPUT_KEY_ERROR to throwable.localizedMessage ?: "Unspecified ML model failure")
                )
            }
        )
    }
}