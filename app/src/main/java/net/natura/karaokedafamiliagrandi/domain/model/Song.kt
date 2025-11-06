
package net.natura.karaokedafamiliagrandi.domain.model

enum class Source { YOUTUBE, LOCAL }

data class Song(
    val id: String,            // YouTube ID ou URI string (LOCAL)
    val title: String,
    val source: Source
)
