package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AtlasCatalogLoader
import com.example.data.local.AtlasDatabase
import com.example.data.local.BookmarkRepository
import com.example.ui.AtlasScreen
import com.example.ui.AtlasViewModel
import com.example.ui.theme.HumanAtlasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val database = AtlasDatabase.getInstance(applicationContext)
        val repository = BookmarkRepository(database.bookmarkDao())
        val catalogLoader = AtlasCatalogLoader(applicationContext)

        setContent {
            HumanAtlasTheme {
                val atlasViewModel: AtlasViewModel = viewModel(
                    factory = AtlasViewModel.Factory(
                        catalogLoader = catalogLoader,
                        bookmarkRepository = repository,
                    ),
                )
                AtlasScreen(viewModel = atlasViewModel)
            }
        }
    }
}
