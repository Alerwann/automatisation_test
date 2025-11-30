package com.alerwann.automatisation_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Looper // 💡 NÉCESSAIRE pour le Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files // NÉCESSAIRE pour la création de répertoire
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter // Pour le nommage de fichier
import android.os.Handler 
import android.os.HandlerThread
import android.app.Activity


class TestSequenceService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Variables MediaProjection
    private lateinit var projectionIntent: Intent
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    // Variables de Capture
    private var imageReader: ImageReader? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var packages: List<String>
    
    // Chemin de sauvegarde
    private val captureDirName = "AutomatisationCaptures"
    private lateinit var captureDirectory: File
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 1. Initialiser le gestionnaire de projection
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        // 2. Déterminer les dimensions de l'écran
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        
        // 3. Définir le répertoire de sauvegarde
        captureDirectory = File(getExternalFilesDir(null), captureDirName)
        if (!captureDirectory.exists()) {
             captureDirectory.mkdirs()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        // --- 1. RÉCUPÉRATION DES DONNÉES ---
        packages = intent?.getStringArrayListExtra("PACKAGES_LIST") ?: emptyList()
        // L'Intent de permission MediaProjection, nécessaire pour démarrer la capture
        projectionIntent = intent?.getParcelableExtra("PROJECTION_INTENT") ?: return START_NOT_STICKY

        // --- 2. DÉMARRAGE DU SERVICE DE PREMIER PLAN (Notification) ---
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "test_channel_id")
            .setContentTitle("Test d'Automatisation en Cours")
            .setContentText("Exécution de la séquence de ${packages.size} applications...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
        
        // --- 3. DÉMARRER LA SÉQUENCE ---
        serviceScope.launch {
            // Le corps de la séquence s'exécute dans un thread Coroutine
            runTestSequence()
        }

        return START_NOT_STICKY
    }

    private suspend fun runTestSequence() {
        // 💡 Créer l'objet MediaProjection : LA clé de la capture
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, projectionIntent)
        
        // 💡 Créer l'ImageReader et VirtualDisplay une seule fois
        setupImageReaderAndVirtualDisplay()

        for (packageName in packages) {
            try {
                // Lancer l'application cible
                launchApp(packageName)
                
                // Délai de stabilisation étendu (15 secondes)
                delay(15000) 
                
                // Prendre la capture d'écran
                val success = takeScreenshot(packageName)
                if (success) {
                    // Mettre à jour la notification
                    updateNotification("Capture réussie pour $packageName")
                } else {
                    updateNotification("Échec capture pour $packageName")
                }
                
                // Délai d'attente restant (45 secondes)
                delay(45000) 
                
            } catch (e: Exception) {
                // Gérer les erreurs de lancement ou de capture
                updateNotification("Erreur critique sur $packageName: ${e.message}")
            }
        }
        
        // Fin de la séquence : Nettoyage
        stopCapture()
        stopSelf()
    }
    
    private fun setupImageReaderAndVirtualDisplay() {
        // 1. Créer l'ImageReader : où les données de l'écran seront stockées
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888, // Format d'image
            2 // Nombre maximal d'images que nous pouvons acquérir
        )
        
        // 2. Créer le VirtualDisplay (la "fenêtre virtuelle" de capture)
        mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            resources.displayMetrics.densityDpi,
            0,
            imageReader!!.surface, // Le VirtualDisplay écrit sur la surface de l'ImageReader
            null,
            null
        )
    }
    
    private fun takeScreenshot(packageName: String): Boolean {
        // L'accès à ImageReader.acquireLatestImage() doit se faire sur un thread dédié
        val image = imageReader?.acquireLatestImage() ?: return false
        
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            // Créer le Bitmap
            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            
            // Créer le nom de fichier
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "${packageName}_${timestamp}.png"
            val file = File(captureDirectory, fileName)
            
            // Sauvegarder l'image
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            image.close() // 🚨 Très important : toujours fermer l'objet Image
        }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) 
        startActivity(launchIntent)
    }

    private fun updateNotification(content: String) {
        // Mettre à jour la notification du Foreground Service
        val notification = NotificationCompat.Builder(this, "test_channel_id")
            .setContentTitle("Test en cours")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification)
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
    
    private fun stopCapture() {
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    override fun onDestroy() {
        stopCapture() // Assurer un nettoyage même si la boucle est interrompue
        serviceJob.cancel()
        super.onDestroy()
    }
}