package com.example.team_23

import android.util.Log
import android.util.Xml
import com.example.team_23.api.Alert
import com.example.team_23.api.Info
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class CapParser {
    /*init {
        var alert: Alert
        var info: Info
        var area: Area
    }*/
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Alert {
        val tag = "CapParser.parse"
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            Log.d(tag, "Current XML-tag: ${parser.name}")
            return readCap(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCap(parser: XmlPullParser): Alert {
        val tag = "CapParser.readCap"

        // Below are temporary variables while creating instance of Alert data class.
        // This is to keep Alert non-mutable (using val-declarations)
        var identifier: String? = null
        var sent: String? = null
        var status: String? = null
        var msgType: String? = null

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
                else -> skip(parser)
            }
        }
        parser.require(XmlPullParser.START_TAG, ns, "info")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            if (parser.name == "info") {
                // Currently only reads first info-block (Norwegian)
                // Double check the validity of doing it this way!
                break
                // readInfo(parser) // Option. Read each info block, check lang-attribute within
            }
        }
        val info = readInfo(parser)
        return Alert(identifier, sent, status, msgType, info)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readInfo(parser: XmlPullParser): Info {
        val tag = "CapParser.readInfo"

        // Below are temporary variables while creating instance of Info data class.
        // This is to keep Info non-mutable (using val-declarations)
        var event: String? = null
        var responseType: String? = null
        /*var urgency: String? = null
        var severity: String? = null
        var certainty: String? = null
        var effective: String? = null
        var onset: String? = null
        var expires: String? = null*/
        var instruction: String? = null

        parser.require(XmlPullParser.START_TAG, ns, "info")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            when (parser.name) {
                "event" -> event = readText(parser)
                "responseType" -> responseType = readText(parser)
                "instruction" -> instruction = readText(parser)
                else -> skip(parser)
            }
        }
        return Info(event, responseType, instruction)
    }

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