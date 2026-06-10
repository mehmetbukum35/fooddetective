package com.mehmetbukum.fooddetective

import com.mehmetbukum.fooddetective.data.AdditivesVersionResponse
import com.mehmetbukum.fooddetective.data.SyncErrorReason
import com.mehmetbukum.fooddetective.data.SyncResult
import com.mehmetbukum.fooddetective.data.SyncSkipReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncMessageMapperTest {

    @Test
    fun success_withSyncText_returnsSuccessUpdatedMessage() {
        val result = SyncResult.Success(
            updatedCount = 12,
            version = version()
        )

        assertEquals(
            UiText.Resource(R.string.sync_success_updated_at, listOf("10 Jun 2026 12:00")),
            SyncMessageMapper.toUserMessage(result, "10 Jun 2026 12:00")
        )
    }

    @Test
    fun success_withoutSyncText_returnsNullMessage() {
        val result = SyncResult.Success(
            updatedCount = 12,
            version = version()
        )

        assertNull(SyncMessageMapper.toUserMessage(result, null))
    }

    @Test
    fun noChange_withSyncText_returnsSuccessCheckedMessage() {
        val result = SyncResult.NoChange(version())

        assertEquals(
            UiText.Resource(R.string.sync_success_checked_at, listOf("10 Jun 2026 12:00")),
            SyncMessageMapper.toUserMessage(result, "10 Jun 2026 12:00")
        )
    }

    @Test
    fun skipped_remoteDataSourceMissing_returnsRemoteSourceMessage() {
        val result = SyncResult.Skipped(SyncSkipReason.RemoteDataSourceMissing)

        assertEquals(
            UiText.Resource(R.string.sync_skip_remote_source_missing),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun skipped_emptyRemoteList_returnsEmptyListMessage() {
        val result = SyncResult.Skipped(SyncSkipReason.EmptyRemoteList)

        assertEquals(
            UiText.Resource(R.string.sync_skip_empty_remote_list),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun skipped_inconsistentCount_returnsCountArgs() {
        val result = SyncResult.Skipped(
            SyncSkipReason.InconsistentCount(expected = 500, actual = 420)
        )

        assertEquals(
            UiText.Resource(R.string.sync_skip_inconsistent_count, listOf(500, 420)),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun skipped_blankCodeRows_returnsBlankCodeCountArg() {
        val result = SyncResult.Skipped(SyncSkipReason.BlankCodeRows(count = 3))

        assertEquals(
            UiText.Resource(R.string.sync_skip_blank_code_rows, listOf(3)),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun skipped_duplicateCodes_returnsFirstFiveCodesOnly() {
        val result = SyncResult.Skipped(
            SyncSkipReason.DuplicateCodes(
                listOf("E100", "E101", "E102", "E103", "E104", "E105")
            )
        )

        assertEquals(
            UiText.Resource(
                R.string.sync_skip_duplicate_codes,
                listOf("E100, E101, E102, E103, E104")
            ),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun error_unexpectedWithDetail_returnsDetailMessage() {
        val result = SyncResult.Error(SyncErrorReason.Unexpected("Timeout"))

        assertEquals(
            UiText.Resource(R.string.sync_error_unexpected_with_detail, listOf("Timeout")),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    @Test
    fun error_unexpectedWithoutDetail_returnsGenericMessage() {
        val result = SyncResult.Error(SyncErrorReason.Unexpected(null))

        assertEquals(
            UiText.Resource(R.string.sync_error_unexpected),
            SyncMessageMapper.toUserMessage(result, null)
        )
    }

    private fun version(): AdditivesVersionResponse {
        return AdditivesVersionResponse(
            total_count = 12,
            last_updated = "2026-06-10",
            version_hash = "abc123"
        )
    }
}
