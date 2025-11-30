import 'package:automatisation_test/model/app_model.dart';
import 'package:flutter/services.dart';

class PlatformService {
  static const MethodChannel platform = const MethodChannel(
    'com.alerwann/screen_automation',
  );

  Future<List<AppInfo>> loadInstalledApps() async {
    try {
      final List<dynamic>? appsMap = await platform.invokeMethod('getAppList');

      if (appsMap != null) {
        // Convertir la liste Kotlin (List<Map<String, String>>) en List<AppInfo>
        List<AppInfo> appList = appsMap.map((map) {
          return AppInfo(
            name: map['name'] as String,
            packageId: map['packageId'] as String,
          );
        }).toList();

        // 🚨 TODO: Utilisez cette liste pour afficher le sélecteur d'applications à l'utilisateur
        print("Liste d'applications récupérée : ${appList.length}");
      }
    } on PlatformException catch (e) {
      print("Erreur lors de la récupération des applications: ${e.message}");
    }
  }
}