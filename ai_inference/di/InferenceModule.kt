package com.archon.ai_inference.di

import com.archon.ai_inference.data.DefaultInferenceRepository
import com.archon.ai_inference.data.InferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt Module for wiring AI Inference dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class InferenceModule {

    @Binds
    abstract fun bindInferenceRepository(
        impl: DefaultInferenceRepository
    ): InferenceRepository
}