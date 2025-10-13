package com.vs18.tvdemoapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.request.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs18.tvdemoapp.core.model.Movie
import com.vs18.tvdemoapp.core.model.MovieList
import kotlinx.coroutines.launch
import coil.compose.*

@Composable
fun VideoDetailsScreen(
    movie: Movie?,
    isOfflineMode: Boolean,
    imageLoader: ImageLoader,
    onNavigateToPlayer: (Movie) -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val crashlytics = FirebaseCrashlytics.getInstance()
    val scope = rememberCoroutineScope()

    if (movie == null) {
        crashlytics.log("❌ No movie data provided in VideoDetailsScreen")
        Toast.makeText(context, "Error: No movie data", Toast.LENGTH_LONG).show()
        onNavigateToMain()
        return
    }

    crashlytics.log("🎬 Opened details for: ${movie.title}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val imageUrl = movie.backgroundImageUrl ?: movie.cardImageUrl
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .listener(
                    onStart = {
                        crashlytics.log("🟡 Coil: start loading image $imageUrl")
                    },
                    onSuccess = { _, result ->
                        crashlytics.log("✅ Coil: image loaded successfully — ${result.dataSource}")
                    },
                    onError = { _, error ->
                        crashlytics.recordException(
                            Exception("❌ Coil failed to load image: $imageUrl", error.throwable)
                        )
                        Toast.makeText(context, "Image load error", Toast.LENGTH_SHORT).show()
                    }
                )
                .build(),
            imageLoader = imageLoader
        )

        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Text("⏳ Loading image...", modifier = Modifier.padding(8.dp))
            }
            is AsyncImagePainter.State.Error -> {
                Text("⚠️ Error loading image", modifier = Modifier.padding(8.dp))
            }
            else -> Unit
        }

        Image(
            painter = painter,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
        )

        Spacer(Modifier.height(16.dp))

        Text(movie.title ?: "Untitled", style = MaterialTheme.typography.h6)
        Text(
            movie.description ?: "No description available.",
            style = MaterialTheme.typography.body2,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                when {
                    isOfflineMode -> {
                        crashlytics.log("📴 Offline playback attempt: ${movie.title}")
                        Toast.makeText(context, "Offline mode", Toast.LENGTH_LONG).show()
                    }
                    movie.videoUrl.isNullOrEmpty() -> {
                        crashlytics.log("⚠️ No video URL for: ${movie.title}")
                        Toast.makeText(context, "No video URL", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        crashlytics.log("▶️ Playing movie: ${movie.title}")
                        onNavigateToPlayer(movie)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Watch")
        }

        Spacer(Modifier.height(24.dp))

        Text("Related Movies", style = MaterialTheme.typography.h6)
        val relatedMovies = remember { MovieList.list.shuffled() }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(relatedMovies) { related ->
                RelatedMovieCard(related, imageLoader) {
                    crashlytics.log("📺 Open related: ${related.title}")
                    onNavigateToPlayer(related)
                }
            }
        }
    }
}

@Composable
fun RelatedMovieCard(movie: Movie, imageLoader: ImageLoader, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        val painter = rememberAsyncImagePainter(
            model = movie.backgroundImageUrl,
            imageLoader = imageLoader
        )

        Image(
            painter = painter,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            movie.title ?: "Untitled",
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
