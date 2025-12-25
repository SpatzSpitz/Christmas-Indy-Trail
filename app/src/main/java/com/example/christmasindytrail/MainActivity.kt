package com.example.christmasindytrail

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.example.christmasindytrail.data.Post
import com.example.christmasindytrail.ui.theme.ChristmasIndyTrailTheme
import com.example.christmasindytrail.ui.theme.CyanAccent
import com.example.christmasindytrail.ui.theme.GoldAccent
import com.example.christmasindytrail.ui.theme.Obsidian
import com.example.christmasindytrail.ui.theme.SurfaceDark
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            ChristmasIndyTrailTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.message) {
                    uiState.message?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearMessage()
                    }
                }

                if (uiState.isLoading || uiState.trail == null) {
                    LoadingScreen()
                } else {
                    TrailScreen(
                        state = uiState,
                        snackbarHostState = snackbarHostState,
                        onScan = viewModel::handleScan,
                        onReveal = viewModel::revealNextHint,
                        onOpenScanner = viewModel::goToScanner,
                        onReset = viewModel::resetProgress,
                        onStartFromIntro = viewModel::startFromIntro
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailScreen(
    state: UiState,
    snackbarHostState: SnackbarHostState,
    onScan: (String) -> Unit,
    onReveal: () -> Unit,
    onOpenScanner: () -> Unit,
    onReset: () -> Unit,
    onStartFromIntro: () -> Unit
) {
    val trail = state.trail ?: return
    val progress = state.progressIndex
    val total = trail.posts.size
    val nextPost = trail.posts.getOrNull(progress)
    val completed = progress >= total && state.currentPost == null

    Scaffold(
        topBar = {},
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val scale = remember { Animatable(0.9f) }
                LaunchedEffect(data.visuals.message) {
                    scale.snapTo(0.9f)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                Snackbar(
                    containerColor = GoldAccent,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            shadowElevation = 24f
                        )
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = GoldAccent.copy(alpha = 0.7f),
                                blurRadius = 18f
                            )
                        )
                    )
                }
            }
        },
        containerColor = Obsidian
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.showIntro -> IntroScreen(
                    title = trail.title ?: "Indy Trail",
                    description = trail.description ?: "Starte den Trail mit dem ersten QR-Code.",
                    onStart = onStartFromIntro
                )
                completed -> CompletionScreen()
                state.currentPost != null -> PostScreen(
                    post = state.currentPost,
                    revealedHints = state.revealedHints,
                    onReveal = onReveal,
                    onNext = onOpenScanner,
                    isLast = progress >= total
                )
                state.showScanner || nextPost == null -> ScannerScreen(expectedId = nextPost?.id, onScan = onScan)
                else -> PostScreen(
                    post = nextPost,
                    revealedHints = state.revealedHints,
                    onReveal = onReveal,
                    onNext = onOpenScanner,
                    isLast = progress >= total
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CyanAccent)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun IndyTopBar(
    title: String,
    subtitle: String?,
    progressText: String,
    onReset: () -> Unit
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onReset() })
                    }
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        actions = {
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
private fun ScannerScreen(expectedId: String?, onScan: (String) -> Unit) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
            Text(
                text = expectedId?.let { "Posten $it" } ?: "Bereit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scanne den naechsten Code, um den Posten freizuschalten.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 0.dp, vertical = 8.dp)
        ) {
            if (hasCameraPermission) {
                QrScannerView(onResult = onScan)
            } else {
                PermissionPlaceholder(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }
            ScanOverlay()
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PostScreen(
    post: Post,
    revealedHints: Int,
    onReveal: () -> Unit,
    onNext: () -> Unit,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = post.title ?: "Posten ${post.id}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CyanAccent
        )
        if (!post.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = post.text ?: "", color = Color.LightGray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0x33FFFFFF))
        Spacer(modifier = Modifier.height(12.dp))

        post.hintFiles.take(revealedHints).forEachIndexed { index, file ->
            HintCard(order = index + 1, filePath = file.absolutePath)
            Spacer(modifier = Modifier.height(12.dp))
        }

        RowActionButtons(
            canRevealMore = revealedHints < post.hintFiles.size,
            onReveal = onReveal,
            onNext = onNext,
            isLast = isLast
        )
    }
}

@Composable
private fun RowActionButtons(
    canRevealMore: Boolean,
    onReveal: () -> Unit,
    onNext: () -> Unit,
    isLast: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onReveal,
            enabled = canRevealMore,
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = if (canRevealMore) "Mehr Hilfe" else "Alle Hinweise sichtbar")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onNext,
            enabled = !isLast,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = if (isLast) "Trail beendet" else "Naechsten QR scannen")
        }
    }
}

@Composable
private fun HintCard(order: Int, filePath: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = "Hinweis $order", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = filePath,
            contentDescription = "Hinweis $order",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(Color(0x3300E1FF), RoundedCornerShape(12.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

@Composable
private fun CompletionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Glueckwunsch!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CyanAccent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Du hast alle Posten des Trails abgeschlossen.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IntroScreen(
    title: String,
    description: String,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title
                    .replace("INDY", "", ignoreCase = true)
                    .replace("  ", " ")
                    .trim()
                    .uppercase()
                    .replace(" ", "\n"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                lineHeight = 38.sp,
                color = CyanAccent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Divider(
                color = CyanAccent.copy(alpha = 0.5f),
                thickness = 2.dp,
                modifier = Modifier
                    .width(120.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "QR-Code scannen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PermissionPlaceholder(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Kamerazugriff benoetigt", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequestPermission) { Text("Zugriff erlauben") }
    }
}

@Composable
private fun QrScannerView(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner, cameraProviderFuture) {
        val cameraExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(analyzerExecutor, QrAnalyzer(onResult))
                }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (_: Exception) {
            }
        }
        cameraProviderFuture.addListener(listener, cameraExecutor)
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            analyzerExecutor.shutdown()
        }
    }

    AndroidPreview(modifier = Modifier.fillMaxSize(), previewView = previewView)
}

@Composable
private fun AndroidPreview(modifier: Modifier, previewView: PreviewView) {
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}

private class QrAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    @Volatile
    private var lastValue: String? = null
    @Volatile
    private var lastTimestamp: Long = 0L

    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close(); return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                if (value != null) {
                    val now = System.currentTimeMillis()
                    if (value != lastValue || now - lastTimestamp > 1000) {
                        lastValue = value
                        lastTimestamp = now
                        onResult(value)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

@Composable
private fun ScanOverlay(frameSize: Dp = 240.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan-line")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val lineShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line"
    )
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val framePx = with(density) { frameSize.toPx() }
            val left = (size.width - framePx) / 2f
            val top = (size.height - framePx) / 2f
            val frameRectSize = androidx.compose.ui.geometry.Size(framePx, framePx)
            val lineY = top + framePx * lineShift

            drawRect(
                color = Color(0x99000000),
                size = size
            )
            drawRoundRect(
                color = CyanAccent.copy(alpha = pulse),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = frameRectSize,
                style = Stroke(width = 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
            drawLine(
                color = GoldAccent,
                start = androidx.compose.ui.geometry.Offset(left + 12f, lineY),
                end = androidx.compose.ui.geometry.Offset(left + framePx - 12f, lineY),
                strokeWidth = 5f
            )
        }
    }
}
