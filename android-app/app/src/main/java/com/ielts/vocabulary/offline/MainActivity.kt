package com.ielts.vocabulary.offline

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        volumeControlStream = AudioManager.STREAM_MUSIC

        webView = WebView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.rgb(255, 253, 248))
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                mediaPlaybackRequiresUserGesture = true
                loadsImagesAutomatically = true
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
            }
        }

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                ASSET_PATH,
                WebViewAssetLoader.AssetsPathHandler(this)
            )
            .build()

        webView.webViewClient = OfflineWebViewClient(assetLoader)

        val rootView = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(255, 253, 248))
            addView(
                webView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            WindowInsetsCompat.CONSUMED
        }

        setContentView(rootView)

        val restored = savedInstanceState != null && webView.restoreState(savedInstanceState) != null
        if (!restored) {
            webView.loadUrl(APP_URL)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!::webView.isInitialized) {
                    finishAfterTransition()
                    return
                }
                webView.evaluateJavascript(BACK_HANDLER_SCRIPT) { handled ->
                    if (handled != "true" && !isFinishing) {
                        finishAfterTransition()
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript(
                "window.IeltsAndroid && window.IeltsAndroid.pauseForBackground && " +
                    "window.IeltsAndroid.pauseForBackground()",
                null
            )
            webView.onPause()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }

    private class OfflineWebViewClient(
        private val assetLoader: WebViewAssetLoader
    ) : WebViewClientCompat() {
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean = !isAppAsset(request.url)

        private fun isAppAsset(uri: Uri): Boolean =
            uri.scheme == "https" &&
                uri.host == WebViewAssetLoader.DEFAULT_DOMAIN &&
                uri.path?.startsWith(ASSET_PATH) == true
    }

    private companion object {
        const val ASSET_PATH = "/assets/"
        const val APP_URL = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/assets/index.html"
        const val BACK_HANDLER_SCRIPT =
            "!!(window.IeltsAndroid && window.IeltsAndroid.handleBack && " +
                "window.IeltsAndroid.handleBack())"
    }
}
