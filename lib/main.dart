import 'package:automatisation_test/model/app_model.dart';
import 'package:automatisation_test/services/platform_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(colorScheme: .fromSeed(seedColor: Colors.deepPurple)),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool _isLoading = false;

  List<AppInfo> _allAvailableApps = [];
  List<AppInfo> _selectedTestApps = [];

  final _platformService = PlatformService();
@override
  void initState() {
    super.initState();
    _initialLoad(); // Lancement de la séquence de chargement
  }

  // Fonction de chargement qui gère à la fois les apps installées et les apps sauvegardées
  Future<void> _initialLoad() async {
    setState(() {
      _isLoading = true;
    });

    // 1. Charger les packageIds enregistrés (Ex: ['com.whatsapp', 'com.facebook'])
    final List<String> savedIds = await _platformService.loadSavedPackageIds();

    // 2. Charger toutes les applications installées (appel natif)
    final List<AppInfo> allApps = await _platformService.loadInstalledApps();

    // 3. Reconstruire la liste sélectionnée
    final List<AppInfo> restoredApps = allApps
        .where((app) => savedIds.contains(app.packageId))
        .toList();

    setState(() {
      _allAvailableApps = allApps;
      _selectedTestApps = restoredApps;
      _isLoading = false;
    });
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text("hello"),
            // --- BOUTON DE TEST ---
            ElevatedButton(
              onPressed: loadInstalledApps,
              child: Text("Tester le Canal"),
            ),
          ],
        ),
      ),
    );
  }
}
