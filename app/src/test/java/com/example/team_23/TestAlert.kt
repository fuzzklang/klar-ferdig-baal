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


    /*
    * ===== GET ALERT COLOR =====
    * Testene under tester at Alert.getAlert returnerer forventet farevarsel-farge.
    * Fargene (YELLOW, ORANGE, RED, UNKNOWN) er definert i AlertColors (en enum class).
    * Farge for varsel beregnes ut ifra en kombinasjon av severity og certainty
    * beskrevet på met.no: https://www.met.no/vaer-og-klima/ekstremvaervarsler-og-andre-farevarsler/faregradering-i-farger
    */
    @Test
    fun testGetAlertColorObservedModerate() {
        // Forventer at
        // severity: moderate + certainty: observed -> gult farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorObservedSevere() {
        // Forventer at
        // severity: severe + certainty: observed -> oransje farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorObservedExtreme() {
        // Forventer at
        // severity: extreme + certainty: observed -> rødt farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Observed", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.RED, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelyModerate() {
        // Forventer at
        // severity: moderate + certainty: likely -> gult farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelySevere() {
        // Forventer at
        // severity: severe + certainty: likely -> oransje farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorLikelyExtreme() {
        // Forventer at
        // severity: extreme + certainty: likely -> rødt farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Likely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.RED, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleModerate() {
        // Forventer at
        // severity: moderate + certainty: possible -> ikke gir farevarsel (returnerer UNKNOWN-type)
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleSevere() {
        // Forventer at
        // severity: severe + certainty: possible -> gult farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorPossibleExtreme() {
        // Forventer at
        // severity: extreme + certainty: possible -> oransje farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Possible", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.ORANGE, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelyModerate() {
        // Forventer at
        // severity: moderate + certainty: unlikely -> ikke gir farevarsel (returnerer UNKNOWN-type)
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelySevere() {
        // Forventer at
        // severity: severe + certainty: unlikely -> ikke gir farevarsel (returnerer UNKNOWN-type)
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Severe", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertColorUnlikelyExtreme() {
        // Forventer at
        // severity: extreme + certainty: unlikely -> gult farevarsel
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Extreme", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.YELLOW, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertInvalidCertainty() {
        // Ugyldige beskrivelse i certainty.
        // Forventer UNKNOWN. Heller kaste Exception?
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "Moderate", "invalidValue", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertInvalidSeverity() {
        // Ugyldige beskrivelse i severity.
        // Forventer UNKNOWN. Heller kaste Exception?
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "invalidValue", "Unlikely", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertEmptySeverityAndEmptyCertainty() {
        // Både severity og certainty er tomme strenger.
        // Forventer UNKNOWN. Heller kaste exception?
        val area = Area(null, null)
        val infoItem = Info(null, null, null, "", "", null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }

    @Test
    fun testGetAlertNullValues() {
        // Dersom verdiene er null returneres UNKNOWN.
        // Kan muligens lede til feil, følg med. Bør istedenfor en Exception kastes?
        val area = Area(null, null)
        val infoItem = Info(null, null, null, null, null, null, area)
        val testAlert = Alert(null, null, null, null, infoItem, infoItem)
        assertEquals(AlertColors.UNKNOWN, testAlert.getAlertColor())
    }
}