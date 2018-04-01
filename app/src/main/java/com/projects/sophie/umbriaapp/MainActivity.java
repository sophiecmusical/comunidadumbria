package com.projects.sophie.umbriaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.projects.sophie.umbriaapp.R;

import org.w3c.dom.Text;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    DrawerLayout drawerLayout;
    NavigationView navView;
    Toolbar toolbar;

    String user = "";
    String pass = "";

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);

        setUpToolbar();
        initNavMenu();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        user = sharedPref.getString("user", "");
        pass = sharedPref.getString("pass", "");
        Boolean ckeditor = sharedPref.getBoolean("ckeditor", false);

        progressBar = findViewById(R.id.progressBar);
        setWebViewConfig(ckeditor);
    }

    private void setWebViewConfig(Boolean ckeditor) {
        webView = findViewById(R.id.partidasJugador);
        webView.setInitialScale(1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);

        if (ckeditor) {
            webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        }

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        // Add a WebViewClient
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                // Inject CSS when page is done loading
                injectCSS();
                super.onPageFinished(view, url);
            }
        });

        webView.loadUrl("https://www.comunidadumbria.com/usuario/novedades/");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                //Required functionality here
                return super.onJsAlert(view, url, message, result);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Init navigation drawer
     */
    private void initNavMenu() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.navview);
        navView.setCheckedItem(R.id.partidas);

        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.partidas:
                                Log.d("MENU", "Home");
                                webView.loadUrl("https://www.comunidadumbria.com/usuario/novedades/");
                                break;
                            case R.id.profile:
                                Log.d("MENU", "Profile");
                                webView.loadUrl("https://www.comunidadumbria.com/usuario/perfil");
                                break;
                            case R.id.messages:
                                Log.d("MENU", "Messages");
                                webView.loadUrl("https://www.comunidadumbria.com/usuario/mensajes");
                                break;
                            case R.id.settings:
                                Log.d("MENU", "Settings");
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.sign_out:
                                Log.d("MENU", "SignOut");
                                webView.loadUrl("https://www.comunidadumbria.com/logout");
                                break;
                        }

                        drawerLayout.closeDrawers();

                        return true;
                    }
                });
    }

    // Inject CSS method: read style.css from assets folder
    // Append stylesheet to document head
    private void injectCSS() {
        try {
            InputStream inputStream = getAssets().open("style.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");

            //loading conditionally
            webView.loadUrl("javascript:(function() { " +
                    "if(document.URL.indexOf(\"usuario/novedades/\") >= 0){ \n" +
                    "document.getElementById('datosUsuario').style.display = \"block\";" +
                    "}})()");

            webView.loadUrl("javascript:(function() { " +
                    "if (document.getElementsByTagName(\"textarea\").length>1){" +
                        "document.getElementById('TEXTO').onfocus = function(){ " +
                        "if (this.value != null && this.value.trim().length<=0 ) this.value= \"\"; " +
                        "var inVar = document.getElementById('NOTAS').value;" +
                        "if (inVar != null && inVar.trim().length<=0 ) document.getElementById('NOTAS').value= \"\";}; " +
                    "}})()");


            webView.loadUrl("javascript:(function() { " +
                    "if(document.getElementsByName('ACCESO').length>0){" +
                    "document.getElementsByName('ACCESO')[0].value= '" + user + "';}" +
                    "if(document.getElementsByName('CLAVE').length>0){" +
                    "document.getElementsByName('CLAVE')[0].value= '" + pass + "';}" +
                    "})()"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_hamburguer);
    }
}
