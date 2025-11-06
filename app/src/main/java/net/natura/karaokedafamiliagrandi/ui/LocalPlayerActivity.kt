
package net.natura.karaokedafamiliagrandi.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import net.natura.karaokedafamiliagrandi.R
import androidx.media3.ui.PlayerView

class LocalPlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_player)

        val uriStr = intent.getStringExtra("uri") ?: return
        val playerView = findViewById<PlayerView>(R.id.player_view)

        player = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p
            p.setMediaItem(MediaItem.fromUri(Uri.parse(uriStr)))
            p.prepare()
            p.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
