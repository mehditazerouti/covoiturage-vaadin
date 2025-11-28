# Covoiturage Vaadin

**Projet universitaire — Application de covoiturage pour étudiants Dauphine**

## Description
Application de covoiturage développée avec Spring Boot et Vaadin, suivant une **architecture hexagonale** (Clean Architecture) avec système d'authentification et de réservation complets.

## Fonctionnalités actuelles

### ✅ Authentification & Sécurité (Phases 1-4 complètes)
- **Login/Logout** : Authentification sécurisée avec BCrypt
- **Inscription publique** : Formulaire d'inscription accessible à tous
  - Code whitelisté → activation immédiate
  - Code non whitelisté → validation admin requise
- **Rôles** : Système USER/ADMIN avec contrôle d'accès
- **Session management** : Sessions persistées en base MySQL
- **Compte admin** : Créé automatiquement au démarrage (admin/admin123)
- **Codes whitelistés** : 3 codes pré-autorisés (22405100, 22405101, 22405102)

### ✅ Gestion des étudiants
- **Annuaire** : Liste des étudiants avec avatars Vaadin
- **Suppression** : Réservée aux admins (impossible de se supprimer soi-même)
- **Filtrage** : N'affiche pas les comptes ADMIN
- **Validation** : Interface admin pour approuver/rejeter les étudiants en attente

### ✅ Administration (réservé aux ADMIN)
- **Whitelist** : Gestion CRUD des codes étudiants autorisés
  - Ajout/suppression de codes
  - Visualisation des codes utilisés et leur attribution
  - Protection : impossible de supprimer un code déjà utilisé
- **Étudiants en attente** : Validation des inscriptions
  - Approuver : whitelist le code + active le compte
  - Rejeter : supprime le compte
- **Création manuelle** : Ajout d'étudiants par l'admin

### ✅ Gestion des trajets
- **Proposer un trajet** : Formulaire avec auto-assignation du conducteur connecté
- **Recherche avancée de trajets** :
  - **4 filtres combinables** : destination, date minimum, places minimum, type de trajet (Tous/Réguliers/Ponctuels)
  - Recherche en temps réel (mise à jour automatique à chaque modification de filtre)
  - Badge visuel "Régulier" (vert) / "Ponctuel" (gris) pour chaque trajet
  - Dialog de confirmation avec récapitulatif complet avant réservation
- **Modifier/Supprimer un trajet** : Réservé au conducteur OU admin
  - Dialog d'édition avec validation (impossible de réduire les places en dessous des réservations)
  - Suppression avec cascade automatique des réservations associées
- **Support des trajets réguliers** : Flag `isRegular` pleinement exploité avec badge et filtre

### ✅ Système de réservation (Phase 5 complète)
- **Réserver un trajet** : Bouton "Réserver" dans la recherche de trajets
  - Dialog de confirmation avec récapitulatif : trajet, date, conducteur, places, type
  - Vérification automatique : pas son propre trajet, pas de double réservation active, places disponibles
  - Décrémentation automatique des places disponibles
- **Mes réservations** : Vue dédiée avec liste complète
  - Affichage : Trajet, Date/Heure, Conducteur, Places disponibles, Type, Date de réservation, Statut
  - Badge coloré par statut (vert=Confirmée, rouge=Annulée, gris=En attente)
  - Badge type de trajet (vert=Régulier, gris=Ponctuel)
  - Dialog de confirmation avant annulation avec détails du trajet
  - Action "Annuler" pour réservations actives uniquement
- **Annulation** : Re-incrémentation automatique des places + possibilité de re-réserver
- **Règles métier** :
  - Un étudiant ne peut pas réserver son propre trajet
  - Un étudiant ne peut avoir qu'une seule réservation active par trajet
  - Les réservations annulées ne bloquent pas une nouvelle réservation

### ✅ Interface moderne & Composants réutilisables
- Layout principal avec **sidebar navigation** (Vaadin AppLayout)
- **Section utilisateur** : Rechercher trajet, Proposer trajet, Mes réservations
- **Section admin** : Annuaire étudiants, Créer étudiant, Codes étudiants, Étudiants en attente
- Navigation responsive avec drawer toggle
- Bouton de déconnexion dans la sidebar
- **Composants réutilisables** :
  - `StatusBadge` : Badge coloré pour statuts de réservation
  - `TripTypeBadge` : Badge pour type de trajet (Régulier/Ponctuel)
  - `ConfirmDeleteDialog` : Dialog générique de confirmation de suppression
  - `BookingCancelDialog` : Dialog d'annulation avec détails
  - `TripBookingDialog` : Dialog de réservation avec récapitulatif
  - `WhitelistCodeDialog` : Dialog d'ajout de code avec validation
  - `TripEditDialog` : Dialog d'édition/suppression de trajet
- **Performance** : Scroll infini Vaadin (chargement progressif automatique)

## Stack technique
- **Frontend** : Vaadin 24.2.0
- **Backend** : Spring Boot 3.1.0 + Spring Data JPA
- **Sécurité** : Spring Security 6.1.0 + BCrypt
- **Base de données** : MySQL 8.0 (XAMPP/local)
- **Session** : Spring Session JDBC
- **Build** : Maven

## Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.6+
- MySQL 8.0 (XAMPP ou serveur local)

### Installation

1. **Cloner le projet**
```bash
git clone <url-du-repo>
cd preprod-covoiturage-vaadin
```

2. **Créer la base de données MySQL**
```sql
CREATE DATABASE covoiturage_db;
```

3. **Appliquer les migrations SQL** (voir section Migrations)

4. **Configurer application.properties** (si nécessaire)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/covoiturage_db
spring.datasource.username=root
spring.datasource.password=
```

5. **Lancer l'application**
```bash
mvn spring-boot:run
```

6. **Accéder à l'application**
- URL : `http://localhost:8080`
- Redirection automatique vers `/login`

## Identifiants par défaut

### Compte administrateur
```
Username: admin
Password: admin123
Email: admin@dauphine.eu
Code: ADMIN001
```

### Codes étudiants whitelistés (pour inscription rapide)
```
22405100
22405101
22405102
```

> **Note** : Vous pouvez vous inscrire avec l'un de ces codes pour un accès immédiat, ou utiliser un autre code qui nécessitera une validation admin.

## Architecture

Structure hexagonale (ports & adapters) avec séparation stricte des couches :

```
src/main/java/com/example/covoiturage_vaadin/
├── domain/model/              # Entités métier
│   ├── Student.java           # Étudiant (avec champs auth + approved)
│   ├── Trip.java              # Trajet (avec méthode bookSeat())
│   ├── Booking.java           # Réservation (avec méthodes cancel(), isActive())
│   ├── BookingStatus.java     # Enum (PENDING, CONFIRMED, CANCELLED)
│   └── AllowedStudentCode.java # Whitelist codes étudiants
├── application/
│   ├── ports/                 # Interfaces (contrats)
│   │   ├── IStudentRepositoryPort.java
│   │   ├── ITripRepositoryPort.java
│   │   ├── IBookingRepositoryPort.java
│   │   └── IAllowedStudentCodeRepositoryPort.java
│   └── services/              # Services métier (cas d'usage)
│       ├── StudentService.java
│       ├── TripService.java
│       ├── BookingService.java
│       ├── SecurityContextService.java
│       ├── AllowedStudentCodeService.java
│       └── AuthenticationService.java
├── infrastructure/
│   ├── adapter/               # Implémentations JPA
│   │   ├── StudentJpaRepository + Adapter
│   │   ├── TripJpaRepository + Adapter
│   │   ├── BookingJpaRepository + Adapter
│   │   └── AllowedStudentCodeJpaRepository + Adapter
│   ├── security/              # UserDetailsService
│   │   └── UserDetailsServiceImpl.java
│   └── config/                # Configuration Security + Data
│       ├── VaadinSecurityConfiguration.java
│       └── DataInitializer.java
└── ui/
    ├── component/             # Composants réutilisables
    │   ├── MainLayout.java    # Layout principal + sidebar
    │   ├── TripEditDialog.java # Dialog édition/suppression trajet
    │   └── LogoutButton.java
    └── view/                  # Vues Vaadin
        ├── LoginView.java     # Authentification
        ├── RegisterView.java  # Inscription publique
        ├── StudentView.java   # Annuaire
        ├── TripCreationView.java
        ├── TripSearchView.java  # Recherche + Réservation + Modification
        ├── MyBookingsView.java  # Mes réservations
        ├── AdminStudentCreationView.java
        ├── AdminWhitelistView.java
        └── PendingStudentsView.java
```

## Vues disponibles

| Route | Vue | Accès | Description |
|-------|-----|-------|-------------|
| `/login` | LoginView | Public | Authentification |
| `/register` | RegisterView | Public | Inscription publique |
| `/` | TripSearchView | Authentifié | Recherche + Réservation de trajets |
| `/proposer-trajet` | TripCreationView | Authentifié | Formulaire de création de trajet |
| `/mes-reservations` | MyBookingsView | Authentifié | Liste des réservations + Annulation |
| `/admin/create-student` | AdminStudentCreationView | Admin | Créer un étudiant manuellement |
| `/admin/whitelist` | AdminWhitelistView | Admin | Gérer les codes étudiants autorisés |
| `/admin/pending-students` | PendingStudentsView | Admin | Valider/rejeter les étudiants en attente |
| `/students` | StudentView | Admin | Annuaire des étudiants |

## Base de données

### Tables principales
- `student` : Étudiants (avec champs auth : username, password, role, approved, enabled, etc.)
- `trip` : Trajets de covoiturage
- `booking` : Réservations (avec cascade delete sur trip)
- `allowed_student_code` : Whitelist des codes étudiants autorisés
- `spring_session` : Sessions utilisateurs (gérée par Spring Session JDBC)

### Accès à la base
Utilisez un client MySQL (MySQL Workbench, DBeaver, phpMyAdmin) :
- Host : `localhost:3306`
- Database : `covoiturage_db`
- User : `root`
- Password : (vide)

### Migrations SQL requises

#### 1. Contrainte ON DELETE SET NULL pour AllowedStudentCode
```sql
ALTER TABLE allowed_student_code DROP FOREIGN KEY FKb6y4t1fmdirvxv4ny3otlku8k;
ALTER TABLE allowed_student_code
ADD CONSTRAINT FKb6y4t1fmdirvxv4ny3otlku8k
FOREIGN KEY (used_by_id) REFERENCES student(id) ON DELETE SET NULL;
```

#### 2. Contrainte ON DELETE CASCADE pour Booking
```sql
ALTER TABLE booking DROP FOREIGN KEY FKkp5ujmgvd2pmsehwpu2vyjkwb;
ALTER TABLE booking
ADD CONSTRAINT FKkp5ujmgvd2pmsehwpu2vyjkwb
FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE;
```

## Historique des développements

### Composants réutilisables + Filtres avancés + Badges (28/11/2025 15:40) ✅
- **Implémenté** : Refactorisation majeure pour améliorer la maintenabilité et l'UX
- **7 nouveaux composants réutilisables** :
  - `StatusBadge` : Badge coloré pour statuts de réservation (Confirmée/Annulée/En attente)
  - `TripTypeBadge` : Badge pour type de trajet (Régulier/Ponctuel)
  - `ConfirmDeleteDialog` : Dialog générique de confirmation de suppression avec gestion d'erreurs automatique
  - `BookingCancelDialog` : Dialog avec détails complets du trajet avant annulation
  - `TripBookingDialog` : Dialog avec récapitulatif (trajet, date, conducteur, places, type) avant réservation
  - `WhitelistCodeDialog` : Dialog formulaire pour ajout de code avec validation (min 5 caractères, support touche ENTER)
  - `TripEditDialog` : Dialog d'édition/suppression de trajet (déjà existant)
- **Recherche avancée de trajets** :
  - Nouveau service `TripService.searchTripsAdvanced()` avec 4 filtres combinables
  - Filtres : destination (insensible à la casse), date minimum (DateTimePicker), places minimum (IntegerField), type de trajet (Select: Tous/Réguliers/Ponctuels)
  - Recherche en temps réel : ValueChangeListener sur tous les filtres
  - Interface horizontale avec tous les filtres alignés + bouton "Rechercher"
- **Vues refactorisées** (5 fichiers modifiés) :
  - `TripSearchView` : Utilise TripTypeBadge + TripBookingDialog + filtres avancés
  - `MyBookingsView` : Utilise StatusBadge + TripTypeBadge + BookingCancelDialog
  - `AdminStudentView` : Utilise ConfirmDeleteDialog (renommé de StudentView, route changée)
  - `AdminWhitelistView` : Utilise ConfirmDeleteDialog + WhitelistCodeDialog (code simplifié de 40 à 3 lignes)
- **Performance** : Scroll infini Vaadin (pas de pagination manuelle, chargement progressif automatique)
- **Code quality** : Suppression de code dupliqué (méthodes getStatusLabel/Badge dans MyBookingsView)

### Phase 5 : Système de réservation (28/11/2025) ✅
- **Implémenté** : Système complet de réservation de trajets
- **Nouvelles entités** :
  - `Booking` : Réservation avec statut (PENDING, CONFIRMED, CANCELLED)
  - `BookingStatus` : Enum pour les statuts
- **Nouveau service** : `BookingService` avec règles métier
  - Création de réservation avec validations
  - Annulation avec re-incrémentation des places
  - Récupération des réservations par étudiant/trajet
- **Nouvelles vues** :
  - `MyBookingsView` : Liste des réservations avec annulation
- **Modifications** :
  - `TripSearchView` : Bouton "Réserver" fonctionnel
  - `TripService` : Auto-assignation du conducteur connecté
  - `Trip` : Méthode `bookSeat()` pour décrémenter les places
- **6 nouveaux fichiers** créés (entité, enum, port, service, repositories, vue)
- **2 fichiers modifiés** (TripSearchView, MainLayout)

### Édition/Suppression de trajets (28/11/2025) ✅
- **Implémenté** : Système complet d'édition et suppression de trajets
- **Nouveau composant** : `TripEditDialog` (Vaadin Dialog)
  - Formulaire pré-rempli avec validation
  - Boutons : Valider, Supprimer (avec confirmation), Annuler
  - Protection : impossible de réduire les places en dessous des réservations
- **Nouveau service** : `TripService.updateTrip()`, `deleteTrip()`, `canEditTrip()`
- **Modifications** :
  - `TripSearchView` : Colonne "Actions" avec bouton "Modifier" (visible pour conducteur/admin)
  - `ITripRepositoryPort` : Ajout méthode `deleteById()`
- **Cascade delete** : Suppression d'un trajet supprime automatiquement ses réservations

### Corrections critiques (28/11/2025) ✅
- **Problème 1** : Contrainte FK bloquait la suppression de trajets avec réservations
  - **Solution** : `@OnDelete(action = OnDeleteAction.CASCADE)` sur Booking → Trip
  - **Migration SQL** : Modifier la contrainte FK pour ON DELETE CASCADE
- **Problème 2** : Impossible de réserver après annulation
  - **Solution** : Nouvelle méthode `existsActiveBookingByTripIdAndStudentId()` qui ignore les réservations CANCELLED
  - **Fichiers modifiés** : IBookingRepositoryPort, BookingJpaRepository, BookingRepositoryAdapter, BookingService

### Correction suppression étudiant (27/11/2025) ✅
- **Problème** : Impossible de supprimer un étudiant ayant utilisé un code whitelist
- **Solution** : ON DELETE SET NULL + Libération automatique du code
- **Fichiers modifiés** : AllowedStudentCode.java, StudentService.java, AllowedStudentCodeService.java

### Système d'inscription et whitelist (27/11/2025) ✅
- **Implémenté** : Phases 2, 3, et 4 complètes
- **Fonctionnalités** : Inscription publique, Whitelist admin, Validation étudiants
- **8 nouveaux fichiers** créés

### LogoutButton NullPointerException (27/11/2025) ✅
- **Problème** : UI.getCurrent() retournait null après déconnexion
- **Solution** : Capture UI avant invalidation session

## 🎯 Prochaines étapes prioritaires

### 1. Vue Profil utilisateur (En cours)
- **Changement d'avatar** : Sélection parmi une liste prédéfinie d'avatars
- **Changement de mot de passe** : Formulaire sécurisé avec confirmation
- **Modification nom/email** : Édition des informations personnelles
- **Code étudiant** : Affichage uniquement (non modifiable)
- **Statistiques** : Nombre de trajets proposés, réservations effectuées

### 2. Design System Neobrutalism
- **Couleurs vives** : Jaune (#FFFF00), Cyan (#00FFFF), Magenta (#FF00FF)
- **Bordures épaisses** : 3-5px en noir
- **Ombres décalées** : `box-shadow: 5px 5px 0px black`
- **Typographie** : Bold et uppercase
- **Pas de border-radius** : Angles à 90°

### 3. Validation JSR-303
- **Bean Validation** sur les entités et DTOs
- Validation automatique côté serveur
- Messages d'erreur personnalisés en français
- Annotations : `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`, etc.

## Améliorations futures

### 🎨 Architecture & Qualité du code
- **DTO (Data Transfer Objects)** :
  - Créer des DTOs pour séparer les entités JPA de l'API
  - Exemples : `TripDTO`, `BookingDTO`, `StudentDTO`
  - Mapper avec MapStruct ou ModelMapper
  - Avantages : Sécurité (ne pas exposer les entités), Flexibilité (différentes représentations)

- **Pattern DAO/Repository amélioré** :
  - Ajouter des spécifications JPA pour requêtes complexes
  - Créer des query objects réutilisables

### 🎨 Interface utilisateur

- **Autres dialogs CRUD** :
  - `StudentEditDialog` : Éditer un étudiant (admin)
  - `StudentApprovalDialog` : Approuver/rejeter avec commentaire
  - `FormDialog` : Dialog générique avec formulaire

- **AvatarComponent personnalisé** : Avatar avec initiales et couleurs dynamiques

### 🚀 Fonctionnalités métier

- **Notifications en temps réel** :
  - Notification push quand une réservation est acceptée/annulée
  - Notification quand un nouveau trajet correspond aux critères
  - Utiliser Vaadin Push (WebSocket) ou Server-Sent Events

- **Système de messages** :
  - Messagerie entre conducteur et passagers
  - Entité `Message` avec relation ManyToOne vers Booking
  - Vue de conversation par réservation

- **Trajets réguliers** :
  - Exploiter le flag `isRegular`
  - Créer des trajets récurrents (ex: tous les lundis)
  - Entité `RecurringTrip` avec pattern (jours, horaire)
  - Génération automatique des instances de trajets

- **Système d'évaluation** :
  - Entité `Review` (note + commentaire)
  - Évaluation conducteur/passager après trajet
  - Affichage de la note moyenne dans le profil

- **Profil utilisateur** :
  - Photo de profil uploadable
  - Préférences (fumeur/non-fumeur, musique, etc.)
  - Historique des trajets proposés/réservés
  - Statistiques (km parcourus, CO2 économisé)

### 🔧 Technique

- **Tests** :
  - Tests unitaires : JUnit 5 + Mockito pour les services
  - Tests d'intégration : Spring Boot Test + TestContainers (MySQL)
  - Tests E2E : Vaadin TestBench (Selenium)
  - Couverture de code : JaCoCo (objectif 80%)

- **Performance** :
  - Mise en cache avec Spring Cache (@Cacheable)
  - Lazy loading pour les listes longues
  - Pagination avec Spring Data (Pageable)
  - Indexation MySQL sur les colonnes fréquemment recherchées

- **Sécurité** :
  - Rate limiting pour éviter les abus
  - Validation stricte des inputs (XSS, SQL injection)
  - HTTPS en production
  - Audit log des actions critiques (CRUD)

- **Documentation** :
  - Swagger/OpenAPI pour l'API REST (si ajoutée)
  - Diagrammes UML (classes, séquence) avec PlantUML
  - Guide d'installation détaillé
  - Vidéo de démonstration

### 🌐 Déploiement

- **Conteneurisation** :
  - Dockerfile pour l'application
  - Docker Compose avec MySQL + Spring Boot
  - Health checks et restart policies

- **CI/CD** :
  - GitHub Actions pour build + tests automatiques
  - Déploiement automatique sur Heroku/Railway/Render
  - Environnements dev/staging/prod

- **Monitoring** :
  - Spring Boot Actuator pour métriques
  - Prometheus + Grafana pour monitoring
  - Logs centralisés avec ELK Stack

## Documentation technique

Pour plus de détails sur l'architecture et les règles de code, consultez :
- **CLAUDE.md** : Guide complet pour le développement
- **plan.md** : Plan détaillé d'implémentation de l'authentification (phases 1-4)

## Auteurs

**Mehdi Tazerouti** et **Salim Bouskine**
Dauphine MIAGE SITN - Projet universitaire 2025
