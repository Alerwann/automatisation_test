package com.alerwann.automatisation_test

import io.flutter.embedding.android.FlutterActivity

import androidx.annotation.NonNull

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    private val CHANNEL = "com.alerwann/screen_automation" 

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            
            when (call.method) {
                "startScreenCapture" -> {
                    // TODO: Étape 4 - Logique de capture d'écran (MediaProjection)
                    result.success(true) // On renvoie une réponse rapide pour l'instant
                }
                "getAppList" -> {
                    // TODO: Étape 5 - Logique pour récupérer la liste des applications installées
                    result.success(emptyList<String>())
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
