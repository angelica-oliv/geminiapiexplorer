## Gemini Explorer: AI Text Generation on Android

This project, **Gemini Explorer**, explores the possibilities of the Gemini API in Android development. You can generate sentences, texts, and even recipes based on chosen images and custom prompts.

### Getting Started

1. **Clone the repository:**

```bash
git clone https://github.com/angelica-oliv/geminiapiexplorer.git
```

2. **Open in Android Studio:**

Import the project into Android Studio and run it on your device or emulator.

3. **Add your API key:**

- Create a project on [https://aistudio.google.com/](https://aistudio.google.com/) and obtain your API key.
- **Never commit your API key to version control!** Add it securely as a local property in Android Studio, following their documentation.

### Usage

1. **Select an image:**

Choose an image from your device or capture one with your camera.

2. **Generate text:**

The chosen image will automatically receive a humorous description.

3. **Custom prompts:**

For more granular control, you can modify the "prompt" value in `GeminiExplorerViewModel.kt` (line 26). Explore different prompts and see what results you get!


## Contributing

Feel free to fork this repository and contribute your own improvements or features. Please make sure to follow good coding practices and create pull requests for your changes.

## Support

For any questions or suggestions, feel free to open an issue on this repository.
