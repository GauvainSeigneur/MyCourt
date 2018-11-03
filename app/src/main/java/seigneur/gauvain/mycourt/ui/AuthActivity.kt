package seigneur.gauvain.mycourt.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

import butterknife.BindView
import butterknife.ButterKnife
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.api.AuthUtils

class AuthActivity : AppCompatActivity() {

    @BindView(R.id.progress_bar)
    internal lateinit var progressBar: ProgressBar

    @BindView(R.id.webview)
    internal lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        ButterKnife.bind(this)

        progressBar.max = 100

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, webRessource: WebResourceRequest): Boolean {
                if (webRessource.url.toString().startsWith(AuthUtils.REDIRECT_URI)) {
                    val uri = webRessource.url
                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_CODE, uri.getQueryParameter(KEY_CODE))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

                return super.shouldOverrideUrlLoading(view, webRessource)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }

        val url = intent.getStringExtra(KEY_URL)
        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        val KEY_URL = "url"
        val KEY_CODE = "code"
    }


}
