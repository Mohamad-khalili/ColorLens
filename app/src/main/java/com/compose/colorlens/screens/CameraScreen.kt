package com.compose.colorlens.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.get
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.compose.colorlens.R
import com.compose.colorlens.models.NamedColor
import com.compose.colorlens.utils.findNearestColor
import com.compose.colorlens.utils.getContrastTextColor
import com.compose.colorlens.utils.loadColorDataset
import java.io.File

@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val colorDataset = remember { loadColorDataset(context) }

    var detectedColor by remember { mutableStateOf<NamedColor?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        startCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            imageCapture = imageCapture
        )
    }

    //Focus
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White, CircleShape)
                .background(Color.Transparent)
        ) {
            Image(
                painterResource(R.drawable.add_24),
                contentDescription = "",
                Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
            )

        }

        //showing founded color
        detectedColor?.let { color ->
            val detailsColor = getContrastTextColor(color.r, color.g, color.b)
            val rgbCode = LocalContext.current.getString(R.string.rgb, color.r, color.g, color.b)

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 36.dp)
                    .background(
                        Color(color.r, color.g, color.b),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = {
                        chooser(context, color,rgbCode)
                    })
                    .padding(16.dp),
            ) {
                Text(
                    text = color.name,
                    color = detailsColor,
                    fontSize = 20.sp
                )

                Text(
                    text = color.hex,
                    color = detailsColor.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    text = rgbCode,
                    color = detailsColor.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )

                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.copy_24),
                    tint = detailsColor,
                    contentDescription = null
                )
            }
        }

        Button(

            onClick = {
                takePhoto(context, imageCapture) { bitmap ->
                    val centerX = bitmap.width / 2
                    val centerY = bitmap.height / 2
                    val pixel = bitmap[centerX, centerY]
                    detectedColor = findNearestColor(pixel, colorDataset)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)

        ) {
            Text(text = "Get Color details")
        }

    }
}

fun chooser(context: Context, color: NamedColor,rgbCode: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "${color.name} \n ${color.hex} \n $rgbCode")
        type = "text/plain"
    }

// Create intent to show chooser
    val chooser = Intent.createChooser(sendIntent, "Select", null)

// Try to invoke the intent.
    try {
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}


@SuppressLint("UnsafeOptInUsageError")
private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Preview
        val preview = androidx.camera.core.Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                /*imageAnalyzer*/
                imageCapture
            )

        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}


private fun takePhoto(context: Context, imageCapture: ImageCapture, onResult: (Bitmap) -> Unit) {
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        File(context.cacheDir, "photo.jpg")
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(File(context.cacheDir, "photo.jpg").path)
                onResult(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )

}
