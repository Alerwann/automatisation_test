import 'package:automatisation_test/model/app_model.dart';
import 'package:flutter/material.dart';

class ShowSelectionDial extends StatelessWidget {
  final List<AppInfo> allAvailableApps;
  final List<AppInfo> selectedTestApps;
  final Function(AppInfo app, bool isAdding) onAppSelectionChanged;
  const ShowSelectionDial({
    super.key,
    required this.allAvailableApps,
    required this.selectedTestApps,
    required this.onAppSelectionChanged,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text("Sélectionner les applications de test"),
      content: SizedBox(
        width: double.maxFinite,
        child: StatefulBuilder(
          // 👈 Ceci permet de mettre à jour le dialogue
          builder: (context, setInnerState) {
            return ListView.builder(
              itemCount: allAvailableApps.length,
              itemBuilder: (context, index) {
                final app = allAvailableApps[index];
                // Vérifie si l'application est déjà sélectionnée
                final isSelected = selectedTestApps.any(
                  (e) => e.packageId == app.packageId,
                );

                return CheckboxListTile(
                  title: Text(app.name),
                  subtitle: Text(app.packageId),
                  value: isSelected,
                  onChanged: (bool? value) {
                   onAppSelectionChanged(app, value == true);
                   setInnerState(() { });
                    });
                  },
                );
              },
            )
          
        ),

      actions: <Widget>[
        TextButton(
          child: Text("Fermer et Enregistrer"),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
      ],
    );
  }
}
