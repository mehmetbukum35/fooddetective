package com.mehmetbukum.fooddetective.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeParserTest {

    @Test
    fun normalizeSingleQuery_addsEPrefixWhenOnlyDigits() {
        assertEquals("E160", CodeParser.normalizeSingleQuery("160"))
    }

    @Test
    fun normalizeSingleQuery_uppercasesLetterSuffix() {
        assertEquals("E160A", CodeParser.normalizeSingleQuery("e160a"))
    }

    @Test
    fun normalizeSingleQuery_removesSpacesBetweenPrefixAndNumber() {
        assertEquals("E330", CodeParser.normalizeSingleQuery("E 330"))
    }

    @Test
    fun normalizeSingleQuery_removesDashBetweenPrefixAndNumber() {
        assertEquals("E330", CodeParser.normalizeSingleQuery("E-330"))
    }

    @Test
    fun normalizeSingleQuery_convertsCurrencyPrefixToE() {
        assertEquals("E211", CodeParser.normalizeSingleQuery("€211"))
        assertEquals("E211", CodeParser.normalizeSingleQuery("£211"))
    }

    @Test
    fun extractCodes_readsNormalECodesFromOcrText() {
        val result = CodeParser.extractCodes("İçindekiler: E 330, e160a ve E120")

        assertEquals(listOf("E330", "E160A", "E120"), result)
    }

    @Test
    fun extractCodes_removesDuplicateCodesButKeepsFirstSeenOrder() {
        val result = CodeParser.extractCodes("E330, e330, E160A, E 160A, E120")

        assertEquals(listOf("E330", "E160A", "E120"), result)
    }

    @Test
    fun extractCodes_convertsSuspectCReadingToECode() {
        val result = CodeParser.extractCodes("Renklendirici: C160")

        assertEquals(listOf("E160"), result)
    }

    @Test
    fun extractCodes_convertsSuspectFReadingToECode() {
        val result = CodeParser.extractCodes("İçindekiler: F330")

        assertEquals(listOf("E330"), result)
    }

    @Test
    fun extractCodes_convertsSuspectCurrencyReadingToECode() {
        val result = CodeParser.extractCodes("Koruyucu: €211")

        assertEquals(listOf("E211"), result)
    }

    @Test
    fun extractCodes_returnsEmptyListForBlankText() {
        assertEquals(emptyList<String>(), CodeParser.extractCodes("   "))
    }

    @Test
    fun matchesRange_returnsTrueWhenCodeInsideRange() {
        assertTrue(CodeParser.matchesRange("E1410", "E1400-E1450"))
    }

    @Test
    fun matchesRange_returnsTrueWhenCodeHasLetterSuffixInsideRange() {
        assertTrue(CodeParser.matchesRange("E1410A", "E1400-E1450"))
    }

    @Test
    fun matchesRange_returnsFalseWhenCodeOutsideRange() {
        assertFalse(CodeParser.matchesRange("E1500", "E1400-E1450"))
    }

    @Test
    fun matchesRange_returnsFalseForInvalidRangeText() {
        assertFalse(CodeParser.matchesRange("E1410", "E1400"))
        assertFalse(CodeParser.matchesRange("E1410", "EABC-E1450"))
    }
}
