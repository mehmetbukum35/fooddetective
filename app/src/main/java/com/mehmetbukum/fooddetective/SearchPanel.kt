package com.mehmetbukum.fooddetective

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.localizedFunctionalClass
import com.mehmetbukum.fooddetective.data.localizedName
import com.mehmetbukum.fooddetective.domain.HalalStatus
import com.mehmetbukum.fooddetective.domain.RiskLevel
import com.mehmetbukum.fooddetective.ui.components.SmallStatusPill
import com.mehmetbukum.fooddetective.ui.theme.LocalAppDarkTheme

private data class QuickSearchItem(
    val code: String,
    @StringRes val labelRes: Int
)

private val QUICK_SEARCH_ITEMS = listOf(
    QuickSearchItem("E120", R.string.quick_search_e120),
    QuickSearchItem("E330", R.string.quick_search_e330),
    QuickSearchItem("E621", R.string.quick_search_e621),
    QuickSearchItem("E211", R.string.quick_search_e211)
)

@Composable
fun SearchPanel(
    modifier: Modifier = Modifier,
    searchQuery: String,
    suggestions: List<Additive>,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSuggestionClick: (Additive) -> Unit,
    onQuickSearch: (String) -> Unit
) {
    val isTyping = searchQuery.trim().isNotEmpty()
    val visibleSuggestions = suggestions.take(4)
    val panelDescription = stringResource(R.string.a11y_search_panel)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = panelDescription },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SearchPanelIntro()
            Spacer(modifier = Modifier.height(12.dp))

            SearchField(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearch = onSearch
            )

            if (isTyping && visibleSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                InstantSuggestionsLayer(
                    suggestions = visibleSuggestions,
                    onSuggestionClick = onSuggestionClick
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            PrimaryScanActions(
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick
            )

            if (!isTyping) {
                Spacer(modifier = Modifier.height(16.dp))
                LabelText(stringResource(R.string.label_popular))
                Spacer(modifier = Modifier.height(8.dp))
                CompactQuickSearchRows(onQuickSearch = onQuickSearch)
            }
        }
    }
}

@Composable
private fun SearchPanelIntro() {
    Column {
        Text(
            text = stringResource(R.string.search_panel_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.search_panel_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PrimaryScanActions(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onCameraClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.button_scan_label),
                fontWeight = FontWeight.Black
            )
        }

        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.button_choose_label_photo),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                stringResource(R.string.search_placeholder),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        },
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        letterSpacing = 2.2.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CompactQuickSearchRows(onQuickSearch: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QUICK_SEARCH_ITEMS.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val label = stringResource(item.labelRes)
                    val quickSearchDescription = stringResource(
                        R.string.a11y_quick_search_item,
                        item.code,
                        label
                    )
                    AssistChip(
                        onClick = { onQuickSearch(item.code) },
                        label = {
                            Text(
                                text = "${item.code} · $label",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = quickSearchDescription },
                        shape = RoundedCornerShape(100.dp)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InstantSuggestionsLayer(
    suggestions: List<Additive>,
    onSuggestionClick: (Additive) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            suggestions.forEach { additive ->
                CompactSuggestionRow(
                    additive = additive,
                    onClick = { onSuggestionClick(additive) }
                )
            }
        }
    }
}

@Composable
private fun CompactSuggestionRow(additive: Additive, onClick: () -> Unit) {
    val isEnglish = LocalConfiguration.current.locales[0].language == "en"
    val additiveName = additive.localizedName(isEnglish)
    val functionalClass = additive.localizedFunctionalClass(isEnglish)
    val riskLevel = RiskLevel.fromRaw(additive.risk_level)
    val halalStatus = HalalStatus.fromRaw(additive.halal_status)
    val riskLabel = stringResource(riskLevel.labelRes)
    val halalLabel = stringResource(halalStatus.labelRes)
    val isDark = LocalAppDarkTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = additive.code,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = additiveName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!functionalClass.isNullOrBlank()) {
                Text(
                    text = functionalClass,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            SmallStatusPill(
                text = riskLabel,
                color = riskColor(riskLevel, isDark),
                background = riskContainerColor(riskLevel, isDark)
            )
            Spacer(modifier = Modifier.height(4.dp))
            SmallStatusPill(
                text = halalLabel,
                color = halalColor(halalStatus, isDark),
                background = halalContainerColor(halalStatus, isDark)
            )
        }
    }
}
