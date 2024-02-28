package com.angelicao.geminiapiexplorer

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface GeminiExplorerUiState {

    val prompt: String
    /**
     * Empty state when the screen is first shown
     */
    data class Initial(override val prompt: String) : GeminiExplorerUiState

    /**
     * Still loading
     */
    data class Loading(override val prompt: String) : GeminiExplorerUiState

    /**
     * Text has been generated
     */
    data class Success(
        override val prompt: String,
        val outputText: String
    ) : GeminiExplorerUiState

    /**
     * There was an error generating text
     */
    data class Error(
        override val prompt: String,
        val errorMessage: String
    ) : GeminiExplorerUiState
}