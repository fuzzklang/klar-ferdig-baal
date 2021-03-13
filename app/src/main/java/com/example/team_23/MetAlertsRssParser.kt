package com.example.team_23

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

// Based on Android Developer tutorial: https://developer.android.com/training/basics/network-ops/xml
//
// Returns list of RSS-items:
// data class RssItem (
//    val title: String?,
//    val description: String?,
//    val link: String?
// )
//
// Currently vulnerable for wrongly formatted XML-files
// Throws no exceptions in case of missing end-tags.
// TODO: Should add test checking for valid XML-formatting.
class MetAlertsRssParser {

    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<RssItem> {
        val tag = "RssParser.parse"
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            //Log.d(tag, "Current XML-tag: ${parser.name}")
            return readRssFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRssFeed(parser: XmlPullParser): List<RssItem> {
        val tag = "RssParser.readRssFeed"
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                //Log.d(tag, "Current XML-tag: ${parser.name}")
                continue
            }
            if (parser.name == "channel") {
                // Currently assumes only one channel in RSS-feed!
                // Double check requirements for CAP-alerts from MET
                break
                // readChannel(parser)  // Read each channel. Requires collecting of sorts
            }
        }
        return readChannel(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChannel(parser: XmlPullParser): List<RssItem> {
        val tag = "RssParser.readChannel"
        val entries = mutableListOf<RssItem>()
        parser.require(XmlPullParser.START_TAG, ns, "channel")
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
        val tag = "RssParser.readRssItem"
        parser.require(XmlPullParser.START_TAG, ns, "item")
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
        //Log.d(tag, "RSS-ITEM:\n          title: $title\n          description: $description\n          link: $link")
        return RssItem(title, description, link)
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
        val tag = "RssParser.skip"
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