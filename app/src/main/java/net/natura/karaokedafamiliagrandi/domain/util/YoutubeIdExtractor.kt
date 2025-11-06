
package net.natura.karaokedafamiliagrandi.domain.util

object YoutubeIdExtractor {
    private val patterns = listOf(
        "v=([a-zA-Z0-9_-]{11})",
        "youtu.be/([a-zA-Z0-9_-]{11})",
        "shorts/([a-zA-Z0-9_-]{11})"
    ).map { Regex(it) }

    fun extractId(urlOrId: String): String? {
        val t = urlOrId.trim()
        if (t.matches(Regex("[a-zA-Z0-9_-]{11}"))) return t
        for (rx in patterns) rx.find(t)?.let { return it.groupValues[1] }
        return null
    }
}
