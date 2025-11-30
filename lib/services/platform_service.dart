import 'package:automatisation_test/model/app_model.dart';
import 'package:flutter/services.dart';

class PlatformService {
  static const MethodChannel _platform = const MethodChannel(
    'com.alerwann/screen_automation',
  );

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
}
