import 'package:automatisation_test/model/app_model.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PlatformService {
  static const MethodChannel _platform = MethodChannel(
    'com.alerwann/screen_automation',
  );
static const String _appKey = 'selectedAppPackages';

Future<List<AppInfo>> loadInstalledApps() async {
    try {
      final List<dynamic>? appsMap = await _platform.invokeMethod('getAppList');

      if (appsMap != null) {
        // La conversion doit être renvoyée
        return appsMap.map((map) {
          return AppInfo(
            name: map['name'] as String,
            packageId: map['packageId'] as String,
          );
        }).toList();
      }
      return []; // Retourne une liste vide si appsMap est null
    } on PlatformException catch (e) {
      print("Erreur lors de la récupération des applications: ${e.message}");
      // Retourne une liste vide en cas d'erreur pour que l'UI puisse continuer
      return [];
    }
  }

  Future<String> startTestSequence(List<String> packageIds) async {
    try {
      // 💡 Appel de la méthode 'startTestSequence' en Kotlin
      final String result = await _platform.invokeMethod(
        'startTestSequence',
        {'packages': packageIds}, // Passage de la liste des PackageId à Kotlin
      );
      return result;
    } on PlatformException catch (e) {
      return "Erreur lors du lancement de la séquence : ${e.message}";
    }
  }

Future<void> saveSelectedApps(List<AppInfo> apps) async {
    final prefs = await SharedPreferences.getInstance();
    // Nous stockons uniquement le packageId, car c'est l'identifiant unique
    final List<String> packageIds = apps.map((app) => app.packageId).toList();
    await prefs.setStringList(_appKey, packageIds);
    print("Liste de ${packageIds.length} packages enregistrée.");
  }

  Future<List<String>> loadSavedPackageIds() async {
    final prefs = await SharedPreferences.getInstance();
    // Récupère la liste des packageIds sauvegardés
    return prefs.getStringList(_appKey) ?? [];
  }
}
