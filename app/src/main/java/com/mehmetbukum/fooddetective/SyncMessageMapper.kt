package com.mehmetbukum.fooddetective

import com.mehmetbukum.fooddetective.data.SyncErrorReason
import com.mehmetbukum.fooddetective.data.SyncResult
import com.mehmetbukum.fooddetective.data.SyncSkipReason

internal object SyncMessageMapper {
    fun toUserMessage(syncResult: SyncResult, successfulSyncText: String?): UiText? {
        return when (syncResult) {
            is SyncResult.Success -> successfulSyncText?.let { syncText ->
                UiText.Resource(R.string.sync_success_updated_at, listOf(syncText))
            }

            is SyncResult.NoChange -> successfulSyncText?.let { syncText ->
                UiText.Resource(R.string.sync_success_checked_at, listOf(syncText))
            }

            is SyncResult.Skipped -> syncResult.reason.toUiText()
            is SyncResult.Error -> syncResult.reason.toUiText()
        }
    }

    fun SyncSkipReason.toUiText(): UiText {
        return when (this) {
            SyncSkipReason.RemoteDataSourceMissing -> UiText.Resource(R.string.sync_skip_remote_source_missing)
            SyncSkipReason.EmptyRemoteList -> UiText.Resource(R.string.sync_skip_empty_remote_list)
            is SyncSkipReason.InconsistentCount -> UiText.Resource(
                R.string.sync_skip_inconsistent_count,
                listOf(expected, actual)
            )
            is SyncSkipReason.BlankCodeRows -> UiText.Resource(
                R.string.sync_skip_blank_code_rows,
                listOf(count)
            )
            is SyncSkipReason.DuplicateCodes -> UiText.Resource(
                R.string.sync_skip_duplicate_codes,
                listOf(codes.take(5).joinToString())
            )
        }
    }

    fun SyncErrorReason.toUiText(): UiText {
        return when (this) {
            is SyncErrorReason.Unexpected -> technicalMessage
                ?.takeIf { it.isNotBlank() }
                ?.let { UiText.Resource(R.string.sync_error_unexpected_with_detail, listOf(it)) }
                ?: UiText.Resource(R.string.sync_error_unexpected)
        }
    }
}
