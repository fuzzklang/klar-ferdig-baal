package com.example.team_23

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private val ns: String? = null

// Based on Android Developer tutorial: https://developer.android.com/training/basics/network-ops/xml
class MetAlertsRssXmlParser {
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<*> {
        println("Input stream available: ${inputStream.available()}")
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readRssFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRssFeed(parser: XmlPullParser): List<RssItem> {
        val entries = mutableListOf<RssItem>()

        parser.require(XmlPullParser.START_TAG, ns, "rss")
        // Ignores the first rss-tags (title, desc, lang, ...)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the item tag
            if (parser.name == "item") {
                entries.add(readRssItem(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    // Parses the contents of an item. If it encounters a title, description, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRssItem(parser: XmlPullParser): RssItem {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var title: String? = null
        var description: String? = null
        var link: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readText(parser)
                "description" -> description = readText(parser)
                "link" -> link = readText(parser)
                else -> skip(parser)
            }
        }
        return RssItem(title, description, link)
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        //return result // convert HTML Entities to UTF-8 codepoints here? I.e "&#xE5;" to "ø"
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
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