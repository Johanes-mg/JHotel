# 🏨 JHotel - Application de gestion hôtelière

Application Android de gestion hôtelière développée avec **Java** pour gérer facilement les chambres, réservations, séjours et occupations.

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)
![SQLite](https://img.shields.io/badge/SQLite-Database-blue.svg)

---

## 📱 Fonctionnalités

- 🏠 **Gestion des chambres** : Ajoutez, modifiez et supprimez des chambres
- 👤 **Gestion des clients** : Gérez les réservations, séjours et occupations
- 📅 **Réservations** : Créez et annulez des réservations avec envoi d'email
- 🛏️ **Séjours** : Enregistrez des séjours avec calcul automatique du prix
- 🔑 **Occupations** : Gérez l'occupation des chambres
- 📊 **Statistiques** : 
  - Solde actuel
  - Gains par mois, semaine et jour
  - Réinitialisation du solde
- 📄 **PDF** : Générez des reçus PDF pour les séjours
- ✉️ **Email** : Envoi automatique de confirmation par email
- 🔍 **Recherche** : Trouvez les chambres libres par date

---

## 📸 Captures d'écran

### Navigation dans l'application

| Écran | Description |
|-------|-------------|
| <img src="captures/1.jpg" width="200"/> | **Icône de l'application** - Logo JHotel |
| <img src="captures/2.jpg" width="200"/> | **Écran principal** - Menu avec solde actuel |
| <img src="captures/3.jpg" width="200"/> | **Gestion des chambres** - Ajout, modification et liste des chambres |
| <img src="captures/4.jpg" width="200"/> | **Gestion des clients** - Réservations, Occupations, Séjours |
| <img src="captures/5.jpg" width="200"/> | **Statistiques** - Gains par mois, semaine et jour |
| <img src="captures/6.jpg" width="200"/> | **Recherche** - Chambres libres par date |

---

## 🛠️ Prérequis

Avant de commencer, assurez-vous d'avoir installé :

| Outil | Version minimale | Téléchargement |
|-------|------------------|----------------|
| **Android Studio** | Hedgehog (2023.1.1) | [Télécharger](https://developer.android.com/studio) |
| **JDK** | 17 ou supérieur | [Télécharger](https://adoptium.net/) |

---

## 📥 Installation
---
### 1️⃣ Cloner le projet


### Via HTTPS
git clone https://github.com/Johanes-mg/JHotel.git


### Accéder au dossier
cd JHotel

---

### 2️⃣ Ouvrir dans Android Studio

## Android Studio
Lancer le programme

Sélectionnez le dossier JHotel et faites vos modifs

## 📦 Génération de l'apk

### Nettoyer le projet et debug 

## Sur Windows (PowerShell ou CMD)
gradlew clean
gradlew assembleDebug

## Sur Linux
./gradlew clean
./gradlew assembleDebug

---
#### 📂 Où trouver l'APK Debug ?

~JHotel\app\build\outputs\apk\debug\app-debug.apk
---
## Auteur

Johanès Falitiana

## Licence

MIT
