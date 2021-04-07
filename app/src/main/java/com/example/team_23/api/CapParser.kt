package com.example.team_23.api

import android.util.Log
import android.util.Xml
import com.example.team_23.api.dataclasses.Alert
import com.example.team_23.api.dataclasses.Area
import com.example.team_23.api.dataclasses.Info
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class CapParser {
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Alert {
        //val tag = "CapParser.parse"
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            //Log.d(tag, "Current XML-tag: ${parser.name}")
            return readCap(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCap(parser: XmlPullParser): Alert {
        val tag = "CapParser.readCap"

        // Under er midlertidige variabler som brukes når en instans av Alert opprettes.
        // Sikrer at Alert er ikke-muterbar (at den bruker val-deklarasjoner)
        var identifier: String? = null
        var sent: String? = null
        var status: String? = null
        var msgType: String? = null
        //var info = Info(null, null, null, Area(null, null))
        val infoItemsNo = mutableListOf<Info>() // Instansier tom liste for info-elementer på norsk
        val infoItemsEn = mutableListOf<Info>() // Instansier tom liste for info-elementer på engelsk

        parser.require(XmlPullParser.START_TAG, ns, "alert")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            when (parser.name) {
                "identifier" -> identifier = readText(parser)
                "sent" -> sent = readText(parser)
                "status" -> status = readText(parser)
                "msgType" -> msgType = readText(parser)
                // Les alle Info-elementer i Alert.
                "info" -> {
                    val info: Info = readInfo(parser)
                    when (info.lang) {
                        "no" -> infoItemsNo.add(info)
                        // "en" -> infoItemsEn.add(info)  // Trengs denne?
                        "en-GB" -> infoItemsEn.add(info)
                        else -> Log.w(tag, "Ukjent språk (lang) for info-element")  // Kommer forhåpentligvis aldri hit.
                    }
                }
                else -> skip(parser)
            }
        }
        // Gjør listene ikke-muterbare.
        return Alert(identifier, sent, status, msgType, infoItemsNo.toList(), infoItemsEn.toList())
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readInfo(parser: XmlPullParser): Info {
        //val tag = "CapParser.readInfo"

        // Under er midlertidige variabler som brukes når en instans av Info opprettes.
        // Sikrer at Info er ikke-muterbar (at den bruker val-deklarasjoner)
        var lang: String? = null
        var event: String? = null
        var responseType: String? = null
        // Disse kan inkluderes dersom vi trenger disse XML-elementene fra varselet.
        // Må defineres i dataklassen Info også.
        /*var urgency: String? = null
        var severity: String? = null
        var certainty: String? = null
        var effective: String? = null
        var onset: String? = null
        var expires: String? = null*/
        var instruction: String? = null
        var area = Area(null, null)

        parser.require(XmlPullParser.START_TAG, ns, "info")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            when (parser.name) {
                "language" -> lang = readText(parser)
                "event" -> event = readText(parser)
                "responseType" -> responseType = readText(parser)
                "instruction" -> instruction = readText(parser)
                "area" -> area = readArea(parser)
                else -> skip(parser)
            }
        }
        return Info(lang, event, responseType, instruction, area)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readArea(parser: XmlPullParser): Area {
        //val tag = "CapParser.readArea"

        // Below are temporary variables while creating instance of Area data class.
        // This is to keep Area non-mutable (using val-declarations)
        var areaDesc: String? = null
        var polygon: String? = null  // Now: string containing comma-separated tuples of values. Change to a Polygon data type?

        parser.require(XmlPullParser.START_TAG, ns, "area")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            when (parser.name) {
                "areaDesc" -> areaDesc = readText(parser)
                "polygon" -> polygon = readText(parser)
                else -> skip(parser)
            }
        }
        return Area(areaDesc, polygon)
    }

    // Add method to read parameters in info-block
    // Trengs til senere
    /*@Throws(XmlPullParserException::class, IOException::class)
    private fun readParameter(parser: XmlPullParser, type: Enum) {
        return param
    }*/

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        //return result // TODO convert HTML Entities to UTF-8 codepoints here? I.e "&#xE5;" to "ø"?
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        val tag = "CapParser.skip"
        if (parser.eventType != XmlPullParser.START_TAG) {
            Log.d(tag, "THROWING EXCEPTION")
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}