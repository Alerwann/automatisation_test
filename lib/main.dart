import 'package:automatisation_test/model/app_model.dart';
import 'package:automatisation_test/services/platform_service.dart';
import 'package:automatisation_test/widget/show_selection_dial.dart';
import 'package:flutter/material.dart';

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
    _initialLoad();
  }

  // Fonction de chargement qui gère à la fois les apps installées et les apps sauvegardées
  Future<void> _initialLoad() async {
    setState(() {
      _isLoading = true;
    });

    // 1. Charger les packageIds enregistrés
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

  // 💡 TÂCHE 8.1: Gestion de l'état du dialogue (Callback)
  void _handleAppSelection(AppInfo app, bool isAdding) {
    setState(() {
      if (isAdding) {
        if (!_selectedTestApps.any((e) => e.packageId == app.packageId)) {
          _selectedTestApps.add(app);
        }
      } else {
        _selectedTestApps.removeWhere((e) => e.packageId == app.packageId);
      }
    });
  }

  // 💡 TÂCHE 8.2: Affichage et Sauvegarde du Dialogue
  void _showAppSelectionDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return ShowSelectionDial(
          allAvailableApps: _allAvailableApps,
          selectedTestApps: _selectedTestApps,
          onAppSelectionChanged: _handleAppSelection,
        );
      },
    ).then((_) {
      // 💡 Sauvegarde déclenchée à la fermeture du dialogue
      _platformService.saveSelectedApps(_selectedTestApps);
    });
  }

  // 💡 TÂCHE 9.2: Déclenchement de la séquence de test
  Future<void> _startTestSequence() async {
    setState(() => _isLoading = true); // Afficher le loader pendant l'exécution

    final List<String> packageIds = _selectedTestApps
        .map((app) => app.packageId)
        .toList();

    // Appel du service natif (qui gère la boucle)
    final String result = await _platformService.startTestSequence(packageIds);

    setState(() => _isLoading = false);

    // Afficher le résultat (à adapter pour une notification utilisateur)
    print("Séquence terminée : $result");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            // Affichage de l'état
            if (_isLoading)
              const CircularProgressIndicator()
            else ...[
              Text("Applications sélectionnées : ${_selectedTestApps.length}"),
              const SizedBox(height: 20),

              // Bouton 1: Modification de la liste
              ElevatedButton(
                onPressed: _allAvailableApps.isNotEmpty
                    ? _showAppSelectionDialog
                    : null,
                child: const Text("Modifier la Liste de Test"),
              ),
              const SizedBox(height: 20),

              // Bouton 2: Lancement de la séquence (désactivé si aucune app sélectionnée)
              ElevatedButton(
                onPressed: _selectedTestApps.isNotEmpty
                    ? _startTestSequence
                    : null,

                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                ),
                child: const Text("LANCER LA SÉQUENCE"),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
