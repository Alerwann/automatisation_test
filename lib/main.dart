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

  Future<void> _loadInstalledApps() async {
    setState(() {
      _isLoading = true;
    });

    // 💡 Appel du service au lieu du code natif direct
    final List<AppInfo> appList = await PlatformService().loadInstalledApps();

    setState(() {
      _allAvailableApps = appList;
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
              onPressed: _loadInstalledApps,
              child: Text("Tester le Canal"),
            ),
          ],
        ),
      ),
    );
  }
}
