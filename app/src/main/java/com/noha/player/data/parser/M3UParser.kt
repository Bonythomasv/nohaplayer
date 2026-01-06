package com.noha.player.data.parser

import com.noha.player.data.model.Channel
import java.util.regex.Pattern

object M3UParser {
    
    private val EXTINF_PATTERN = Pattern.compile(
        "#EXTINF:(-?\\d+)\\s*(.*?),(.*)"
    )
    
    private val ATTRIBUTE_PATTERN = Pattern.compile(
        "([a-zA-Z-]+)=\"([^\"]+)\""
    )
    
    fun parse(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = m3uContent.lines()
        
        var currentExtInf: String? = null
        var currentAttributes: Map<String, String>? = null
        
        for (i in lines.indices) {
            val line = lines[i].trim()
            
            if (line.isEmpty()) continue
            
            if (line.startsWith("#EXTM3U")) {
                // Playlist header, skip
                continue
            }
            
            if (line.startsWith("#EXTINF:")) {
                // Parse EXTINF line
                currentExtInf = line
                currentAttributes = parseAttributes(line)
            } else if (!line.startsWith("#") && line.isNotEmpty()) {
                // This is a URL line
                if (currentExtInf != null) {
                    val channel = createChannel(
                        extInfLine = currentExtInf,
                        attributes = currentAttributes ?: emptyMap(),
                        streamUrl = line
                    )
                    channels.add(channel)
                    
                    // Reset for next channel
                    currentExtInf = null
                    currentAttributes = null
                }
            }
        }
        
        return channels
    }
    
    private fun parseAttributes(extInfLine: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        val matcher = ATTRIBUTE_PATTERN.matcher(extInfLine)
        
        while (matcher.find()) {
            val key = matcher.group(1)
            val value = matcher.group(2)
            if (key != null && value != null) {
                attributes[key] = value
            }
        }
        
        return attributes
    }
    
    private fun createChannel(
        extInfLine: String,
        attributes: Map<String, String>,
        streamUrl: String
    ): Channel {
        // Extract channel name (after the comma)
        val commaIndex = extInfLine.lastIndexOf(',')
        val channelName = if (commaIndex != -1 && commaIndex < extInfLine.length - 1) {
            extInfLine.substring(commaIndex + 1).trim()
        } else {
            attributes["tvg-name"] ?: "Unknown Channel"
        }
        
        return Channel(
            name = channelName,
            streamUrl = streamUrl,
            logoUrl = attributes["tvg-logo"],
            groupTitle = attributes["group-title"],
            country = attributes["tvg-country"],
            language = attributes["tvg-language"],
            tvgId = attributes["tvg-id"],
            tvgName = attributes["tvg-name"] ?: channelName
        )
    }
}

