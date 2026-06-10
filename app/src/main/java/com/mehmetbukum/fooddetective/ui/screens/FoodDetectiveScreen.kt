package com.mehmetbukum.fooddetective.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mehmetbukum.fooddetective.ApiConnectionStatus
import com.mehmetbukum.fooddetective.FoodDetectiveUiState
import com.mehmetbukum.fooddetective.FoodDetectiveViewModel
import com.mehmetbukum.fooddetective.OcrResultsList
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.ResultCard
import com.mehmetbukum.fooddetective.SearchPanel
import com.mehmetbukum.fooddetective.UiText
import com.mehmetbukum.fooddetective.asString
import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.localization.AppLanguage
import com.mehmetbukum.fooddetective.ocr.CameraScreen
import com.mehmetbukum.fooddetective.ocr.TextRecognizerHelper
import com.mehmetbukum.fooddetective.ui.components.AboutGuideDialog
import com.mehmetbukum.fooddetective.ui.components.AppSettingsPicker
import com.mehmetbukum.fooddetective.ui.components.ErrorCard
import com.mehmetbukum.fooddetective.ui.components.LoadingState
import com.mehmetbukum.fooddetective.ui.components.NotFoundText
import com.mehmetbukum.fooddetective.ui.components.PremiumHeader
import com.mehmetbukum.fooddetective.ui.theme.AppThemeMode

@Composable
fun FoodDetectiveScreen(
    viewModel: FoodDetectiveViewModel,
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeMode,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCameraPermissionRationale by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.searchFromOcr {
                TextRecognizerHelper.recognizeFromUri(context, uri)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openCamera()
        } else {
            viewModel.setCameraError(UiText.Resource(R.string.camera_permission_denied_message))
        }
    }

    val requestCameraPermission = {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val openCameraWithPermission = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            viewModel.openCamera()
        } else if (context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true) {
            showCameraPermissionRationale = true
        } else {
            requestCameraPermission()
        }
    }

    if (showCameraPermissionRationale) {
        CameraPermissionRationaleDialog(
            onDismiss = { showCameraPermissionRationale = false },
            onAllowPermission = {
                showCameraPermissionRationale = false
                requestCameraPermission()
            },
            onChooseGallery = {
                showCameraPermissionRationale = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    if (uiState.showCamera) {
        CameraScreen(
            onImageCaptured = { bitmap, rotation ->
                viewModel.searchFromOcr {
                    TextRecognizerHelper.recognizeFromBitmap(bitmap, rotation)
                }
            },
            onLiveTextConfirmed = { rawText ->
                viewModel.searchFromOcr { rawText }
            },
            onGalleryClick = { galleryLauncher.launch("image/*") },
            onClose = { viewModel.closeCamera() },
            onError = { message -> viewModel.setCameraError(message) }
        )
        return
    }

    FoodDetectiveContent(
        state = uiState,
        selectedLanguage = selectedLanguage,
        selectedTheme = selectedTheme,
        onLanguageSelected = onLanguageSelected,
        onThemeSelected = onThemeSelected,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearch = { viewModel.search() },
        onCameraClick = openCameraWithPermission,
        onGalleryClick = { galleryLauncher.launch("image/*") },
        onSuggestionClick = viewModel::selectSuggestion,
        onQuickSearch = { code -> viewModel.search(code) },
        onRetryCamera = openCameraWithPermission,
        onRetryGallery = { galleryLauncher.launch("image/*") }
    )
}

@Composable
private fun CameraPermissionRationaleDialog(
    onDismiss: () -> Unit,
    onAllowPermission: () -> Unit,
    onChooseGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.camera_permission_message),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = stringResource(R.string.camera_permission_rationale_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onAllowPermission) {
                Text(stringResource(R.string.button_allow_permission))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onChooseGallery) {
                    Text(stringResource(R.string.button_choose_gallery))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.button_close))
                }
            }
        }
    )
}

@Composable
fun FoodDetectiveContent(
    state: FoodDetectiveUiState,
    selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
    selectedTheme: AppThemeMode = AppThemeMode.SYSTEM,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onThemeSelected: (AppThemeMode) -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSuggestionClick: (Additive) -> Unit,
    onQuickSearch: (String) -> Unit,
    onRetryCamera: () -> Unit,
    onRetryGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAboutGuide by remember { mutableStateOf(false) }

    if (showAboutGuide) {
        AboutGuideDialog(
            language = selectedLanguage,
            onDismiss = { showAboutGuide = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumHeader(
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ApiConnectionIndicator(status = state.apiConnectionStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    AppSettingsPicker(
                        selectedTheme = selectedTheme,
                        selectedLanguage = selectedLanguage,
                        onThemeSelected = onThemeSelected,
                        onLanguageSelected = onLanguageSelected,
                        apiConnectionStatus = state.apiConnectionStatus,
                        syncMessage = state.syncMessage,
                        onAboutClick = { showAboutGuide = true }
                    )
                }
            }
        )

        SearchPanel(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-22).dp),
            searchQuery = state.searchQuery,
            suggestions = state.suggestions,
            onSearchQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onSuggestionClick = onSuggestionClick,
            onQuickSearch = onQuickSearch
        )

        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(horizontal = 20.dp))
        } else {
            state.errorMessage?.let { message ->
                ErrorCard(message = message.asString(), modifier = Modifier.padding(horizontal = 20.dp))
            }
        }

        if (!state.isLoading) {
            AnimatedVisibility(
                visible = state.singleResult != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 360)) +
                    slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(durationMillis = 360)) +
                    scaleIn(initialScale = 0.97f, animationSpec = tween(durationMillis = 360)) +
                    expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 180)) + shrinkVertically(animationSpec = tween(durationMillis = 180))
            ) {
                state.singleResult?.let { additive ->
                    ResultCard(additive = additive, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }

            if (state.singleResult == null && state.hasSearched && state.ocrResult == null && state.errorMessage == null) {
                NotFoundText(text = stringResource(R.string.search_not_found_message), modifier = Modifier.padding(horizontal = 20.dp))
            }

            AnimatedVisibility(
                visible = state.ocrResult != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 340)) +
                    slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(durationMillis = 340)) +
                    expandVertically(animationSpec = tween(durationMillis = 320)),
                exit = fadeOut(animationSpec = tween(durationMillis = 180)) + shrinkVertically(animationSpec = tween(durationMillis = 180))
            ) {
                state.ocrResult?.let { result ->
                    OcrResultsList(
                        result = result,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onRetryCamera = onRetryCamera,
                        onRetryGallery = onRetryGallery
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiConnectionIndicator(status: ApiConnectionStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        ApiConnectionStatus.CHECKING -> stringResource(R.string.api_status_checking)
        ApiConnectionStatus.ONLINE -> stringResource(R.string.api_status_online)
        ApiConnectionStatus.LOCAL -> stringResource(R.string.api_status_local)
        ApiConnectionStatus.OFFLINE -> stringResource(R.string.api_status_offline)
    }

    val dotColor = when (status) {
        ApiConnectionStatus.CHECKING -> Color(0xFFFFC107)
        ApiConnectionStatus.ONLINE -> Color(0xFF2ECC71)
        ApiConnectionStatus.LOCAL -> Color(0xFF42A5F5)
        ApiConnectionStatus.OFFLINE -> Color(0xFFE74C3C)
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
