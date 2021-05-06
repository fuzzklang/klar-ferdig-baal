package com.example.team_23

import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.AlertColors
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Area
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Info
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

/* Test Alert-klassen */
class TestAlert {
    @Test
    fun testGetPolygonValidString() {
        // Tester at streng med koordinater (polygon) konverteres til korrekt liste
        val expected = listOf(
            LatLng(60.874500,4.447833),
            LatLng(60.793833,4.493000),
            LatLng(60.720000,4.534167),
            LatLng(60.707333,4.541167),
            LatLng(60.620667,4.589167),
            LatLng(60.556667,4.623167)
        )
        val testString = "60.874500,4.447833 60.793833,4.493000 60.720000,4.534167 60.707333,4.541167 60.620667,4.589167 60.556667,4.623167"
        val area = Area(null, testString)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(expected, testAlert.getPolygon())
    }

    @Test(expected = NumberFormatException::class)
    fun testGetPolygonInvalidStringLotsOfWhitespaces() {
        // getPolygon håndterer ikke strenger med uforventet mye whitespace eller tilsvarende.
        // Forventer at feil kastes (NumberFormatException)
        val testString = " 60.874500,      4.447833 60.793833   ,4.493000         60.720000,4.534167            "
        val area = Area(null, testString)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        testAlert.getPolygon()
    }

    @Test(expected = NumberFormatException::class)
    fun testGetPolygonMalformattedStringLetterInNumber() {
        // Test konvertering av feilformatert polygon-streng
        // Forventer at feil kastes (NumberFormatException)
        val testString = "XXX60.874500,4.447833 60.793833,4.493000"
        val area = Area(null, testString)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        testAlert.getPolygon()
    }

    @Test
    fun testGetPolygonEmptyString() {
        // Dersom strengen er tom forventes en tom liste
        val expected = listOf<LatLng>()
        val testString = ""
        val area = Area(null, testString)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(expected, testAlert.getPolygon())
    }

    @Test
    fun testGetPolygonRepeatedly() {
        // Forventer ingen feilmeldinger etter å ha kalt getPolygon flere ganger etter hverandre
        val expected = listOf(
            LatLng(60.874500,4.447833),
            LatLng(60.793833,4.493000),
            LatLng(60.720000,4.534167),
            LatLng(60.707333,4.541167),
            LatLng(60.620667,4.589167),
            LatLng(60.556667,4.623167)
        )
        val testString = "60.874500,4.447833 60.793833,4.493000 60.720000,4.534167 60.707333,4.541167 60.620667,4.589167 60.556667,4.623167"
        val area = Area(null, testString)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        testAlert.getPolygon()
        testAlert.getPolygon()
        testAlert.getPolygon()
        assertEquals(expected, testAlert.getPolygon())
    }

    @Test
    fun testGetAlertColorObservedModerate() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorObservedSevere() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorObservedExtreme() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.RED, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelyModerate() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelySevere() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelyExtreme() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.RED, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleModerate() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleSevere() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleExtreme() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelyModerate() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelySevere() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelyExtreme() {
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertInvalidCertainty() {
        // Ugyldige beskrivelse i certainty. Forventer UNKNOWN
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "invalidValue", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertInvalidSeverity() {
        // Ugyldige beskrivelse i severity. Forventer UNKNOWN
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "invalidValue", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertEmptySeverityAndEmptyCertainty() {
        // Både severity og certainty er tomme strenger. Forventer UNKNOWN
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "", "", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertNullValues() {
        // Dersom verdiene er null returneres UNKNOWN. Kan muligens lede til feil, følg med.
        val area = Area(null, null)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }
}