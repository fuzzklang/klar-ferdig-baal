package com.example.team_23

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Area
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Info
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream

/* Kjører tester på CapParseren.
 * Henter XML-testfiler fra assets-mappen (debug), og parser disse med parseren.
 * XML-testfilene er basert på CAP-varslene fra MetAlerts, og noen er gjort ugyldige/feilformatert.
 *
 * Testfilene har endingen .test kun for å unngå at Android Studio ga masse feilmeldinger under kompilering.
 * Med filendingen .xml tolket Android Studio testfilene som feilformatert XML og klager på dette.
 */
@RunWith(AndroidJUnit4::class)
class TestCapParser {
    //val tag = "TestCapParser"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen
    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testParseCapEmptyStream() {
        // Input: tom XML-fil.
        // Forventer: at XmlPullParserException kastes fra Parser.
        val input: InputStream = appContext.assets.open("testEmptyFile.xml.test")
        var exceptionThrown = false
        try {
            CapParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        Assert.assertTrue(exceptionThrown)
    }

    @Test
    fun testParseCapFeedMissingEndTag() {
        // Input: feilformatert XML, mangler end-tags.
        // Forventer: at XmlPullParserException kastes.
        val input: InputStream = appContext.assets.open("testCapAlertMissingEndTag.xml.test")
        var exceptionThrown = false
        try {
            CapParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        Assert.assertTrue(exceptionThrown)
    }

    @Test
    fun testParseCapFeedCheckAttributes() {
        // Input: gyldig XML (Cap-varsel)
        // Sjekker at parser setter varselets attributter korrekt ('identifier', 'sent', 'status' og 'msgType')
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        Assert.assertEquals(
            "test identifier 2.49.0.1.578.0.190521063816855.1909",
            capAlert.identifier
        )
        Assert.assertEquals("Test 2019-05-21T06:38:16+00:00", capAlert.sent)
        Assert.assertEquals("Test Actual", capAlert.status)
        Assert.assertEquals("Test Update", capAlert.msgType)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoNorwegian() {
        // Input: gyldig varsel.
        // Sjekker at attributtene i Info-elementet (norsk) settes korrekt av parser.
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoNo
        Assert.assertEquals("no", info.lang)
        Assert.assertEquals("Skogbrannfare", info.event)
        Assert.assertEquals("Monitor", info.responseType)
        Assert.assertEquals("Vær forsiktig med åpen ild.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoEnglish() {
        // Input: gyldig varsel.
        // Sjekker at attributtene i Info-elementet (engelsk) settes korrekt av parser.
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoEn
        Assert.assertEquals("en-GB", info.lang)
        Assert.assertEquals("Forest fire danger", info.event)
        Assert.assertEquals("Monitor", info.responseType)
        Assert.assertEquals("Be careful with open fire.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAreaAttributes() {
        // Input: gyldig varsel.
        // Sjekker at Area-attributter settes korrekt av parser.
        // Tester kun Area i Info-elementet som er på norsk nå.
        // Tester for øyeblikket ikke area.polygon (streng med koordinater)
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoNo
        val area: Area = info.area
        Assert.assertEquals("Hordaland", area.areaDesc)
    }
}