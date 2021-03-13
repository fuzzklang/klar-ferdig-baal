package com.example.team_23

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class CapParser {
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Alert {
        val tag = "RssParser.parse"
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            Log.d(tag, "Current XML-tag: ${parser.name}")
            return Alert("identifier")
            //return readCap(parser)
        }
    }

    // TODO: Complete parsing of CAP alerts
}