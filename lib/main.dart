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

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const MethodChannel platform = const MethodChannel(
    'com.alerwann/screen_automation',
  );

  String _testMessage = "Prêt à tester le canal.";

  Future<void> _startCaptureTest() async {
    bool? result; // Utilisation de bool? car le résultat peut être null ou bool

    try {
      // 1. Appel de la méthode 'startScreenCapture' en Kotlin
      final bool value = await platform.invokeMethod('startScreenCapture');
      result = value;
    } on PlatformException catch (e) {
      // 2. Gestion des erreurs (très important pour les channels)
      result = false;
      print("Erreur de Platform Channel: ${e.message}");
    }

    // 3. Mise à jour de l'UI avec le résultat
    setState(() {
      if (result == true) {
        _testMessage = "SUCCESS! Appel natif réussi.";
      } else {
        _testMessage = "ÉCHEC! Vérifiez le nom du canal ou les logs Kotlin.";
      }
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
            Text(_testMessage),
            // --- BOUTON DE TEST ---
            ElevatedButton(
              onPressed: _startCaptureTest,
              child: Text("Tester le Canal"),
            ),
          ],
        ),
      ),
    );
  }
}
