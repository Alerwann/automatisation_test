package com.alerwann.automatisation_test

import android.content.pm.PackageManager

import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager 
import android.content.Context
import android.os.Build

class MainActivity: FlutterActivity() {

    private val CHANNEL = "com.alerwann/screen_automation" 

    companion object {
        private const val REQUEST_MEDIA_PROJECTION = 1001 
    }

    private var pendingResult: MethodChannel.Result? = null
    private var packagesToTest: List<String> = emptyList()

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
                "startTestSequence" -> {
                    // 1. Stocker le résultat Flutter et les paquets
                    pendingResult = result 
                    packagesToTest = call.argument<List<String>>("packages") ?: emptyList()
                    
                    // 2. Déclencher la demande de permission MediaProjection
                    requestMediaProjectionPermission() 
                }
                "getAppList" -> {
                    result.success(getInstalledApps())
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
    private fun requestMediaProjectionPermission() {
        val mediaProjectionManager = 
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        // Lance l'Intent système pour obtenir la permission de l'utilisateur
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION 
        )
    }


   // MainActivity.kt (Remplacement de la fonction onActivityResult entière)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Succès : L'utilisateur a autorisé la capture.
                
                val serviceIntent = Intent(this, TestSequenceService::class.java).apply {
                    // 1. Passer la liste des packages
                    putStringArrayListExtra("PACKAGES_LIST", ArrayList(packagesToTest))
                    // 2. Passer l'Intent de permission MediaProjection (crucial)
                    putExtra("PROJECTION_INTENT", data) 
                }
                
                // Démarrer le service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                
                pendingResult?.success("Séquence de test démarrée.")
                
            } else {
                // Échec : L'utilisateur a refusé ou l'Intent a échoué.
                pendingResult?.error("PERMISSION_DENIED", "L'utilisateur a refusé la capture d'écran.", null)
            }
            pendingResult = null // Réinitialiser le résultat, UNIQUEMENT à la fin
        }
    }
}

