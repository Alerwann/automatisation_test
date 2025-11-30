package com.alerwann.automatisation_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.* // 💡 Coroutines pour gérer les délais
import android.media.projection.MediaProjection // NÉCESSAIRE pour la capture

// 🚨 Vous devrez ajouter la dépendance Kotlin Coroutines dans build.gradle.kts plus tard.

class TestSequenceService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    // VARIABLES REÇUES DE MAINACTIVITY
    private lateinit var packages: List<String>
    private lateinit var projectionIntent: Intent
    
    // État de la capture MediaProjection
    private var mediaProjection: MediaProjection? = null 

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        // --- 1. RÉCUPÉRATION DES DONNÉES ---
        packages = intent?.getStringArrayListExtra("PACKAGES_LIST") ?: emptyList()
        projectionIntent = intent?.getParcelableExtra("PROJECTION_INTENT") ?: return START_NOT_STICKY

        // --- 2. DÉMARRAGE DU SERVICE DE PREMIER PLAN (Notification Obligatoire) ---
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "test_channel_id")
            .setContentTitle("Test d'Automatisation en Cours")
            .setContentText("Exécution de la séquence de capture d'écran...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
        
        // --- 3. DÉMARRER LA SÉQUENCE ---
        serviceScope.launch {
            runTestSequence()
        }

        return START_NOT_STICKY // Le service ne redémarre pas s'il est tué
    }

    // 💡 LOGIQUE PRINCIPALE DE LA SÉQUENCE
    private suspend fun runTestSequence() {
        // TODO: Initialiser MediaProjectionManager ici
        
        for (packageName in packages) {
            // Lancer l'application et attendre 15 secondes
            launchApp(packageName)
            delay(15000) // 15 secondes de stabilisation
            
            // TODO: Prendre la capture d'écran ici
            // takeScreenshot()
            
            delay(45000) // 45 secondes restantes pour atteindre 1 minute
        }
        
        // Une fois que tout est fini, arrêter le service
        stopSelf()
    }
    
    // Fonction utilitaire pour lancer une application
    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Obligatoire pour lancer depuis un Service
        startActivity(launchIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel_id",
                "Séquence de Test",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        // Nettoyage en cas d'arrêt du service
        serviceJob.cancel()
        super.onDestroy()
    }
}