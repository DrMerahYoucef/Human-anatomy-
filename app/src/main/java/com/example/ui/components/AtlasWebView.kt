package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

private const val ATLAS_HOST = "appassets.androidplatform.net"
private const val ATLAS_ENTRY_URL = "https://$ATLAS_HOST/android.html"

private class AtlasJsBridge(
    private val mainHandler: Handler,
    private val onProgress: (Int) -> Unit,
    private val onReady: () -> Unit,
    private val onPartSelected: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    @JavascriptInterface
    fun onProgress(percent: Int) {
        mainHandler.post { onProgress.invoke(percent) }
    }

    @JavascriptInterface
    fun onReady() {
        mainHandler.post { onReady.invoke() }
    }

    @JavascriptInterface
    fun onPartSelected(partId: String) {
        mainHandler.post { onPartSelected.invoke(partId) }
    }

    @JavascriptInterface
    fun onError(message: String) {
        mainHandler.post { onError.invoke(message) }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AtlasWebView(
    sceneStateJson: String,
    reloadToken: Int,
    onProgress: (Int) -> Unit,
    onReady: () -> Unit,
    onPartSelected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnProgress by rememberUpdatedState(onProgress)
    val latestOnReady by rememberUpdatedState(onReady)
    val latestOnPartSelected by rememberUpdatedState(onPartSelected)
    val latestOnError by rememberUpdatedState(onError)
    val latestStateJson by rememberUpdatedState(sceneStateJson)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageLoaded by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                removeJavascriptInterface("AndroidBridge")
                stopLoading()
                destroy()
            }
            webViewRef = null
        }
    }

    LaunchedEffect(sceneStateJson, pageLoaded, webViewRef) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (!pageLoaded) return@LaunchedEffect
        pushStateToWebView(wv, sceneStateJson)
    }

    LaunchedEffect(reloadToken) {
        if (reloadToken > 0) {
            pageLoaded = false
            webViewRef?.loadUrl(ATLAS_ENTRY_URL)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(Color.parseColor("#080C12"))
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }

                addJavascriptInterface(
                    AtlasJsBridge(
                        mainHandler = mainHandler,
                        onProgress = { latestOnProgress(it) },
                        onReady = { latestOnReady() },
                        onPartSelected = { latestOnPartSelected(it) },
                        onError = { latestOnError(it) },
                    ),
                    "AndroidBridge",
                )

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? {
                        val url = request?.url ?: return null
                        if (url.host != ATLAS_HOST) return null
                        return serveBundledAsset(context, url.path.orEmpty())
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        pageLoaded = true
                        view?.let { pushStateToWebView(it, latestStateJson) }
                    }
                }

                webViewRef = this
                loadUrl(ATLAS_ENTRY_URL)
            }
        },
        update = { /* State updates are pushed via LaunchedEffect */ },
    )
}

private fun pushStateToWebView(webView: WebView, sceneStateJson: String) {
    val script = """
        (function() {
          var s = $sceneStateJson;
          window.__pendingAtlasState = s;
          if (window.humanAtlas && window.humanAtlas.setState) {
            window.humanAtlas.setState(s);
          }
        })();
    """.trimIndent()
    webView.evaluateJavascript(script, null)
}

internal fun openBundledAtlasAsset(context: Context, cleanPath: String): java.io.InputStream {
    val assetPath = "atlas/$cleanPath"
    return try {
        context.assets.open(assetPath)
    } catch (_: Exception) {
        when {
            cleanPath.endsWith(".gz") -> {
                // AAPT2 strips .gz and stores the uncompressed asset under the base filename.
                context.assets.open("atlas/${cleanPath.removeSuffix(".gz")}")
            }
            cleanPath.endsWith(".bin") -> {
                GZIPInputStream(context.assets.open("$assetPath.gz"))
            }
            else -> throw IllegalArgumentException("Missing asset: $assetPath")
        }
    }
}

private fun serveBundledAsset(context: Context, rawPath: String): WebResourceResponse {
    val cleanPath = rawPath.trimStart('/').ifEmpty { "android.html" }
    val mimeType = when {
        cleanPath.endsWith(".html") -> "text/html"
        cleanPath.endsWith(".js") || cleanPath.endsWith(".mjs") -> "application/javascript"
        cleanPath.endsWith(".css") -> "text/css"
        cleanPath.endsWith(".json") -> "application/json"
        cleanPath.endsWith(".svg") -> "image/svg+xml"
        else -> "application/octet-stream"
    }

    return try {
        val stream = openBundledAtlasAsset(context, cleanPath)
        WebResourceResponse(
            mimeType,
            if (mimeType.startsWith("text/") || mimeType == "application/javascript" || mimeType == "application/json") "UTF-8" else null,
            200,
            "OK",
            mapOf("Access-Control-Allow-Origin" to "*"),
            stream,
        )
    } catch (_: Exception) {
        WebResourceResponse(
            "text/plain",
            "UTF-8",
            404,
            "Not Found",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0)),
        )
    }
}
