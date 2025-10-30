package com.vs18.tvdemoapp.ui.screens

import android.widget.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil.*
import coil.compose.*
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*

@Composable
fun VideoDetailsScreen(
    movie: Movie?,
    isOfflineMode: Boolean,
    imageLoader: ImageLoader,
    onNavigateToPlayer: (Movie) -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val crashlytics = remember { FirebaseCrashlytics.getInstance() }
    val scrollState = rememberScrollState()
    val lazyRowState = rememberLazyListState()

    val relatedMovies by remember {
        derivedStateOf { MovieList.list.shuffled().take(10) }
    }

    if (movie == null) {
        LaunchedEffect(Unit) {
            crashlytics.log("No movie data in VideoDetailsScreen")
            Toast.makeText(context, "Error: No movie data", Toast.LENGTH_LONG).show()
            onNavigateToMain()
        }
        return
    }

    LaunchedEffect(movie.id) {
        crashlytics.log("Opened details: ${movie.title}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        val imageUrl = movie.backgroundImageUrl ?: movie.cardImageUrl

        val painter = rememberAsyncImagePainter(
            model = imageUrl,
            imageLoader = imageLoader,
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> crashlytics.log("Coil loading: $imageUrl")
                    is AsyncImagePainter.State.Success -> crashlytics.log("Coil Success")
                    is AsyncImagePainter.State.Error -> {
                        crashlytics.recordException(state.result.throwable)
                        Toast.makeText(context, "Image load error", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        )

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
            movie.description ?: "No description.",
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
                        crashlytics.log("Offline mode blocked: ${movie.title}")
                        Toast.makeText(context, "Offline mode", Toast.LENGTH_LONG).show()
                    }
                    movie.videoUrl.isNullOrEmpty() -> {
                        crashlytics.log("No video URL: ${movie.title}")
                        Toast.makeText(context, "No video URL", Toast.LENGTH_LONG).show()
                    } else -> {
                        crashlytics.log("Playing: ${movie.title}")
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

        LazyRow(
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = relatedMovies,
                key = { it.id }
            ) { related ->
                RelatedMovieCard(
                    movie = related,
                    imageLoader = imageLoader,
                    onClick = {
                        crashlytics.log("Related clicked: ${related.title}")
                        onNavigateToPlayer(related)
                    }
                )
            }
        }
    }
}

@Composable
fun RelatedMovieCard(
    movie: Movie,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
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
