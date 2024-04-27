package com.angelicao.geminiapiexplorer

import android.graphics.Bitmap
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiExplorerViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val prompt = "Cuéntame la receta del plato de la imagen"
    //        val prompt = "Escribe una frase divertida sobre la imagen"
    private val _uiState: MutableStateFlow<GeminiExplorerUiState> =
        MutableStateFlow(GeminiExplorerUiState.Initial(prompt))
    val uiState: StateFlow<GeminiExplorerUiState> =
        _uiState.asStateFlow()

    fun analyzeImages(selectedImages: List<Bitmap>) {
        _uiState.value = GeminiExplorerUiState.Loading(prompt)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var outputContent = ""

                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = GeminiExplorerUiState.Success(prompt, outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = GeminiExplorerUiState.Error(prompt,e.localizedMessage ?: "")
            }
        }
    }
}