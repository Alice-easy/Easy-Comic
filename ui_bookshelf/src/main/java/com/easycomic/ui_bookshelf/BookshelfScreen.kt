package com.easycomic.ui_bookshelf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycomic.domain.model.Manga
import com.easycomic.ui_bookshelf.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = koinViewModel(),
    onNavigateToReader: (String) -> Unit
) {
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                // The path from the document tree is complex, need to handle it properly.
                // For now, we assume it can be converted to a direct file path, which might not always be true.
                // A more robust solution would involve ContentResolver.
                val path = uri.path 
                if (path != null) {
                    // This is a simplification. Real world usage needs careful path handling.
                    // Example: converting content URI to file path.
                    viewModel.importComic(path)
                }
            }
        }
    )
    val mangas by viewModel.comics.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.bookshelf_title)) },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: Implement sort */ }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openDocumentLauncher.launch(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Import Manga")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {
            items(mangas) { manga ->
                MangaItem(manga = manga, onMangaClick = onNavigateToReader)
            }
        }
    }
}

@Composable
fun MangaItem(manga: Manga, onMangaClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onMangaClick(manga.filePath) }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // TODO: Add image loading for the cover
            Text(text = manga.title)
        }
    }
}