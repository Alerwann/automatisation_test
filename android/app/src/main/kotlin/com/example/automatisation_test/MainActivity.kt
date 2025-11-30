package com.alerwann.automatisation_test

import android.content.pm.PackageManager

import io.flutter.embedding.android.FlutterActivity

import androidx.annotation.NonNull

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    private val CHANNEL = "com.alerwann/screen_automation" 

    private fun getInstalledApps(): List<Map<String, String>> {
        val packageManager = applicationContext.packageManager
        
        // Liste des paquets que nous allons ignorer pour ne pas surcharger la liste (applications système)
        val excludedPackages = setOf("com.google.android.inputmethod.latin", "com.android.settings", "com.google.android.gms")
        
        // Récupérer la liste des applications
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val appList = mutableListOf<Map<String, String>>()
        for (app in apps) {
            // Filtrer les applications système (celles qui n'ont pas de launcher) et celles à exclure
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null && !excludedPackages.contains(app.packageName)) {
                
                // Récupérer le nom affiché de l'application
                val appName = packageManager.getApplicationLabel(app).toString()
                
                // On stocke le nom affiché et le Package Name (l'identifiant unique)
                appList.add(mapOf(
                    "name" to appName,
                    "packageId" to app.packageName
                ))
            }
        }
        // Trier la liste par ordre alphabétique du nom affiché
        return appList.sortedBy { it["name"] }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            
            when (call.method) {
                "startScreenCapture" -> {
            
                    result.success(true) // On renvoie une réponse rapide pour l'instant
                }
                "getAppList" -> {
                    // TODO: Étape 5 - Logique pour récupérer la liste des applications installées
                    result.success(getInstalledApps())
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}

