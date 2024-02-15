package com.angelicao.geminiapiexplorer

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.google.ai.client.generativeai.GenerativeModel
import com.angelicao.geminiapiexplorer.ui.theme.GeminiAPIExplorerTheme
import com.angelicao.geminiapiexplorer.util.UriSaver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAPIExplorerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro-vision",
                        apiKey = BuildConfig.apiKey
                    )
                    val viewModel = GeminiExplorerViewModel(generativeModel)
                    GeminiExplorerRoute(viewModel)
                }
            }
        }
    }
}

@Composable
internal fun GeminiExplorerRoute(
    geminiExplorerViewModel: GeminiExplorerViewModel = viewModel()
) {
    val geminiExplorerUiState by geminiExplorerViewModel.uiState.collectAsState()

    GeminiExplorerScreen(geminiExplorerUiState, onImagesSelected = { inputText ->
        geminiExplorerViewModel.analyzeImages(inputText)
    })
}

suspend fun getBitmapImages(imageUris: List<Uri>,
                    imageRequestBuilder: ImageRequest.Builder,
                    imageLoader: ImageLoader) =
    imageUris.mapNotNull {
        val imageRequest = imageRequestBuilder
            .data(it)
            // Scale the image down to 768px for faster uploads
            .size(size = 768)
            .precision(Precision.EXACT)
            .build()
        try {
            val result = imageLoader.execute(imageRequest)
            if (result is SuccessResult) {
                return@mapNotNull (result.drawable as BitmapDrawable).bitmap
            } else {
                return@mapNotNull null
            }
        } catch (e: Exception) {
            return@mapNotNull null
        }
    }

@Composable
fun GeminiExplorerScreen(
    uiState: GeminiExplorerUiState = GeminiExplorerUiState.Initial,
    onImagesSelected: (List<Bitmap>) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        imageUri?.let {
            coroutineScope.launch {
                imageUris.clear()
                imageUris.add(it)

                val bitmapImages = getBitmapImages(
                    imageUris = imageUris,
                    imageRequestBuilder = imageRequestBuilder,
                    imageLoader = imageLoader
                )

                onImagesSelected(bitmapImages)
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Card(
                modifier = Modifier.wrapContentWidth()
            ) {
                IconButton(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_image),
                    )
                }
            }
        }
        LazyRow(
            modifier = Modifier.padding(all = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            items(imageUris)  { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .requiredSize(360.dp)
                )
            }
        }
        when (uiState) {
            GeminiExplorerUiState.Initial -> {
                // Nothing is shown
            }

            GeminiExplorerUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is GeminiExplorerUiState.Success -> {
                Row(modifier = Modifier.padding(all = 8.dp)) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Person Icon"
                    )
                    Text(
                        text = uiState.outputText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            is GeminiExplorerUiState.Error -> {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun GeminiExplorerScreenPreview() {
    GeminiExplorerScreen()
}