# Offline Arabic OCR Android App

This is an Android application that performs offline Optical Character Recognition (OCR) for Arabic text using the Tesseract OCR engine. The app is designed to work completely offline, making it suitable for environments without internet access.

## Features

*   **Offline OCR:** All OCR processing is done on the device without requiring an internet connection.
*   **Arabic Language Support:** Specifically configured for accurate recognition of Arabic text.
*   **Tesseract OCR:** Leverages the powerful open-source Tesseract OCR engine.
*   **Android Native:** Built natively for Android using Kotlin.

## How to Build and Run

1.  **Clone the Repository:**
    ```bash
    git clone <repository_url>
    cd OfflineArabicOcrApp
    ```
2.  **Open in Android Studio:**
    Open the project in [Android Studio](https://developer.android.com/studio).
3.  **Install Tesseract Language Data:**
    For Tesseract to work, you need to provide the Arabic language data file (`ara.traineddata`).
    *   Download `ara.traineddata` from the [Tesseract OCR traineddata repository](https://github.com/tesseract-ocr/tessdata_fast/blob/main/ara.traineddata).
    *   Place the downloaded `ara.traineddata` file into the `app/src/main/assets/tessdata/` directory of this project. You might need to create these directories if they don't exist yet.
    *   **Important:** Ensure the file is named `ara.traineddata` and is directly inside `assets/tessdata/`.
4.  **Sync Project with Gradle Files:**
    Click on "Sync Project with Gradle Files" in Android Studio to ensure all dependencies are resolved.
5.  **Run on Device/Emulator:**
    Connect an Android device or start an Android emulator, then click the "Run" button (green triangle) in Android Studio to install and launch the application.

## Technologies Used

*   **Kotlin:** Primary programming language for Android development.
*   **Android SDK:** Core Android development framework.
*   **Tesseract OCR:** Open-source OCR engine.
*   **Tess-Two (or similar Tesseract Wrapper):** An Android-specific wrapper for Tesseract (to be integrated).
*   **Gradle:** Build automation tool for Android projects.

## Contributing

(Future section for contribution guidelines)

## License

(Future section for license information)