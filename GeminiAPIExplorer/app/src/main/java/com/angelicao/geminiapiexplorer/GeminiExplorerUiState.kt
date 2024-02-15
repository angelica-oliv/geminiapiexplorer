package com.angelicao.geminiapiexplorer

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface GeminiExplorerUiState {

    /**
     * Empty state when the screen is first shown
     */
    object Initial : GeminiExplorerUiState

    /**
     * Still loading
     */
    object Loading : GeminiExplorerUiState

    /**
     * Text has been generated
     */
    data class Success(
        val outputText: String
    ) : GeminiExplorerUiState

    /**
     * There was an error generating text
     */
    data class Error(
        val errorMessage: String
    ) : GeminiExplorerUiState
}