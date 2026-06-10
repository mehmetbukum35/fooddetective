package com.mehmetbukum.fooddetective

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.OcrSearchResult
import com.mehmetbukum.fooddetective.ui.screens.FoodDetectiveContent
import com.mehmetbukum.fooddetective.ui.theme.EDetectiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FoodDetectiveContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun content_showsHeaderAndSearchActions() {
        setContent()

        composeRule.onNodeWithText(text(R.string.header_label)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.header_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.button_scan_label)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.button_choose_label_photo)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.label_popular)).assertIsDisplayed()
    }

    @Test
    fun content_hasImportantAccessibilityDescriptions() {
        setContent(
            state = FoodDetectiveUiState(
                singleResult = additive(code = "E330", name = "Sitrik Asit"),
                hasSearched = true
            )
        )

        composeRule.onNode(
            hasContentDescription(text(R.string.a11y_header_description))
        ).assertIsDisplayed()

        composeRule.onNode(
            hasContentDescription(text(R.string.a11y_search_panel))
        ).assertIsDisplayed()

        composeRule.onNode(
            hasContentDescription(
                context.getString(
                    R.string.a11y_result_card,
                    "E330",
                    "Sitrik Asit",
                    "Asitlik düzenleyici",
                    text(R.string.halal_halal),
                    text(R.string.risk_low)
                )
            )
        ).assertIsDisplayed()
    }

    @Test
    fun content_rendersInDarkTheme() {
        setContent(darkTheme = true)

        composeRule.onNodeWithText(text(R.string.header_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.button_scan_label)).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun searchField_typingInvokesCallback() {
        val typedValues = mutableListOf<String>()
        setContent(
            onSearchQueryChange = { typedValues.add(it) }
        )

        composeRule.onNode(hasSetTextAction())
            .performTextInput("E330")

        assertTrue(typedValues.isNotEmpty())
        assertEquals("E330", typedValues.last())
    }

    @Test
    fun content_showsSingleResultCard() {
        setContent(
            state = FoodDetectiveUiState(
                singleResult = additive(code = "E330", name = "Sitrik Asit"),
                hasSearched = true
            )
        )

        composeRule.onNodeWithText("E330").assertIsDisplayed()
        composeRule.onNodeWithText("Sitrik Asit").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.label_halal)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.label_risk)).assertIsDisplayed()
    }

    @Test
    fun content_showsNotFoundMessageAfterSearch() {
        setContent(
            state = FoodDetectiveUiState(
                hasSearched = true,
                singleResult = null,
                ocrResult = null,
                errorMessage = null,
                isLoading = false
            )
        )

        composeRule.onNodeWithText(text(R.string.not_found_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.search_not_found_message)).assertIsDisplayed()
    }

    @Test
    fun content_showsLoadingState() {
        setContent(
            state = FoodDetectiveUiState(isLoading = true)
        )

        composeRule.onNodeWithText(text(R.string.loading_label_reading_tag)).assertIsDisplayed()
    }

    @Test
    fun content_showsErrorMessage() {
        val errorMessage = "Test hatası"
        setContent(
            state = FoodDetectiveUiState(errorMessage = UiText.Dynamic(errorMessage))
        )

        composeRule.onNodeWithText(text(R.string.error_title_operation_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun content_showsOcrResultSummaryAndRetryButtons() {
        var retryCameraClicked = false
        var retryGalleryClicked = false
        setContent(
            state = FoodDetectiveUiState(
                hasSearched = true,
                ocrResult = OcrSearchResult(
                    found = emptyList(),
                    notFoundCodes = emptyList(),
                    totalDetected = 0,
                    rawText = ""
                )
            ),
            onRetryCamera = { retryCameraClicked = true },
            onRetryGallery = { retryGalleryClicked = true }
        )

        composeRule.onNode(
            hasContentDescription(context.getString(R.string.ocr_summary, 0, 0))
        ).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ocr_result_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ocr_stat_detected)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ocr_stat_found)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ocr_stat_not_found)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ocr_unreadable_title)).assertIsDisplayed()

        composeRule.onNodeWithTag("ocr_retry_camera").performScrollTo().performClick()
        composeRule.onNodeWithTag("ocr_retry_gallery").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertTrue(retryCameraClicked)
            assertTrue(retryGalleryClicked)
        }
    }

    private fun setContent(
        state: FoodDetectiveUiState = FoodDetectiveUiState(),
        darkTheme: Boolean = false,
        onSearchQueryChange: (String) -> Unit = {},
        onSearch: () -> Unit = {},
        onCameraClick: () -> Unit = {},
        onGalleryClick: () -> Unit = {},
        onSuggestionClick: (Additive) -> Unit = {},
        onQuickSearch: (String) -> Unit = {},
        onRetryCamera: () -> Unit = {},
        onRetryGallery: () -> Unit = {}
    ) {
        composeRule.setContent {
            EDetectiveTheme(darkTheme = darkTheme) {
                FoodDetectiveContent(
                    state = state,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick,
                    onSuggestionClick = onSuggestionClick,
                    onQuickSearch = onQuickSearch,
                    onRetryCamera = onRetryCamera,
                    onRetryGallery = onRetryGallery
                )
            }
        }
    }

    private fun text(resId: Int): String = context.getString(resId)

    private fun additive(code: String, name: String): Additive {
        return Additive(
            code = code,
            name_tr = name,
            functional_class = "Asitlik düzenleyici",
            halal_status = "Helal",
            health_status = "Genel kullanımda düşük risklidir.",
            risk_level = "Düşük",
            description = "Test açıklaması",
            warning = null,
            name_en = null,
            functional_class_en = null,
            health_status_en = null,
            description_en = null,
            warning_en = null
        )
    }
}
