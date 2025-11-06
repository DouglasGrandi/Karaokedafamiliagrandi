
package net.natura.karaokedafamiliagrandi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.natura.karaokedafamiliagrandi.audio.LiveAnalyzer
import net.natura.karaokedafamiliagrandi.domain.Scoring
import net.natura.karaokedafamiliagrandi.domain.model.Song
import net.natura.karaokedafamiliagrandi.domain.model.Source
import net.natura.karaokedafamiliagrandi.domain.util.YoutubeIdExtractor
import net.natura.karaokedafamiliagrandi.playback.YouTubeIntentPlayer
import net.natura.karaokedafamiliagrandi.ui.LocalPlayerActivity

class MainActivity : ComponentActivity() {
    private lateinit var yt: YouTubeIntentPlayer
    private var analyzer: LiveAnalyzer? = null
    private val scoring = Scoring()
    private val queue = mutableStateListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        yt = YouTubeIntentPlayer(this)
        setContent { AppUI() }
    }

    @Composable
    private fun AppUI() {
        var rms by remember { mutableStateOf(0.0) }
        var energy by remember { mutableStateOf(0) }
        var stability by remember { mutableStateOf(0) }
        var countdown by remember { mutableStateOf<Int?>(null) }

        fun ensureMicPermission(onGranted: () -> Unit) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), 1001)
            }
        }

        fun startMic() {
            analyzer = LiveAnalyzer { r, p ->
                rms = r
                val (e, s) = scoring.update(r, p)
                energy = e; stability = s
            }.also { it.start() }
        }

        val pickFolder = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                val tree = DocumentFile.fromTreeUri(this@MainActivity, uri)
                tree?.listFiles()?.forEach { f ->
                    val name = f.name ?: "Vídeo local"
                    if (f.isFile && (name.endsWith(".mp4", true) || name.endsWith(".mkv", true))) {
                        queue.add(Song(id = f.uri.toString(), title = name, source = Source.LOCAL))
                    }
                }
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {}
            }
        }

        MaterialTheme {
            Box(Modifier.fillMaxSize()) {
                HomeScreen(
                    youtubeInputHint = "Cole 1 ou vários links/IDs do YouTube (1 por linha)",
                    queue = queue,
                    onAddYouTube = { multi ->
                        multi.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { inLine ->
                            YoutubeIdExtractor.extractId(inLine)?.let { id ->
                                queue.add(Song(id = id, title = "YouTube $id", source = Source.YOUTUBE))
                            }
                        }
                    },
                    onPickLocalFolder = { pickFolder.launch(null) },
                    onPlay = { song ->
                        countdown = 3
                        lifecycleScope.launch {
                            for (i in 3 downTo 1) { countdown = i; delay(1000) }
                            countdown = null
                            ensureMicPermission { startMic() }
                            when (song.source) {
                                Source.YOUTUBE -> yt.play(song.id)
                                Source.LOCAL -> {
                                    val intent = Intent(this@MainActivity, LocalPlayerActivity::class.java)
                                    intent.putExtra("uri", song.id)
                                    startActivity(intent)
                                }
                            }
                        }
                    },
                    onRemove = { song -> queue.remove(song) }
                )

                val partial = (energy + stability).coerceIn(0, 100)
                ShowOverlay(rms = rms, partialScore = partial, countdown = countdown)
            }
        }
    }
}

@Composable
fun HomeScreen(
    youtubeInputHint: String,
    queue: List<Song>,
    onAddYouTube: (String) -> Unit,
    onPickLocalFolder: () -> Unit,
    onPlay: (Song) -> Unit,
    onRemove: (Song) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Karaokedafamiliagrandi", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        Text("Adicionar do YouTube", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(youtubeInputHint) },
            minLines = 3
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = { onAddYouTube(text); text = "" }) { Text("Adicionar YouTube (multi)") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onPickLocalFolder) { Text("Adicionar vídeos locais (pasta)") }
        }

        Spacer(Modifier.height(24.dp))
        Text("Fila", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        queue.forEach { song ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${song.title}  •  ${song.source}", Modifier.weight(1f))
                TextButton(onClick = { onPlay(song) }) { Text("Reproduzir") }
                TextButton(onClick = { onRemove(song) }) { Text("Remover") }
            }
            Divider()
        }
    }
}

@Composable
fun ShowOverlay(rms: Double, partialScore: Int, countdown: Int?) {
    Box(Modifier.fillMaxSize()) {
        if (countdown != null && countdown > 0) {
            Text(countdown.toString(), style = MaterialTheme.typography.displayLarge, modifier = Modifier.align(Alignment.Center))
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
            LinearProgressIndicator(progress = ((rms / 4000.0).coerceIn(0.0, 1.0)).toFloat())
            Spacer(Modifier.height(8.dp))
            Text("Pontuação parcial: $partialScore")
        }
    }
}
