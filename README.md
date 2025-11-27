# Covoiturage Vaadin

**Projet universitaire — Application de covoiturage pour étudiants Dauphine**

## Description
Application de covoiturage développée avec Spring Boot et Vaadin, suivant une **architecture hexagonale** (Clean Architecture) avec système d'authentification complet.

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
- **Proposer un trajet** : Formulaire avec auto-assignation du conducteur
- **Rechercher des trajets** : Recherche par destination (insensible à la casse)
- Support des trajets réguliers (flag `isRegular`)

### ✅ Interface moderne
- Layout principal avec **sidebar navigation** (Vaadin AppLayout)
- **Section utilisateur** : Annuaire, Rechercher trajet, Proposer trajet
- **Section admin** : Créer étudiant, Whitelist, Étudiants en attente (visible uniquement pour ROLE_ADMIN)
- Navigation responsive avec drawer toggle
- Bouton de déconnexion dans la sidebar

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

3. **Configurer application.properties** (si nécessaire)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/covoiturage_db
spring.datasource.username=root
spring.datasource.password=
```

4. **Lancer l'application**
```bash
mvn spring-boot:run
```

5. **Accéder à l'application**
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
│   ├── Trip.java              # Trajet
│   └── AllowedStudentCode.java # Whitelist codes étudiants
├── application/
│   ├── ports/                 # Interfaces (contrats)
│   │   ├── IStudentRepositoryPort.java
│   │   ├── ITripRepositoryPort.java
│   │   └── IAllowedStudentCodeRepositoryPort.java
│   └── services/              # Services métier (cas d'usage)
│       ├── StudentService.java
│       ├── TripService.java
│       ├── SecurityContextService.java
│       ├── AllowedStudentCodeService.java
│       └── AuthenticationService.java
├── infrastructure/
│   ├── adapter/               # Implémentations JPA
│   │   ├── StudentJpaRepository + Adapter
│   │   ├── TripJpaRepository + Adapter
│   │   └── AllowedStudentCodeJpaRepository + Adapter
│   ├── security/              # UserDetailsService
│   │   └── UserDetailsServiceImpl.java
│   └── config/                # Configuration Security + Data
│       ├── VaadinSecurityConfiguration.java
│       └── DataInitializer.java
└── ui/
    ├── component/             # Composants réutilisables
    │   ├── MainLayout.java    # Layout principal + sidebar (sections user/admin)
    │   └── LogoutButton.java
    └── view/                  # Vues Vaadin
        ├── LoginView.java     # Authentification
        ├── RegisterView.java  # Inscription publique
        ├── StudentView.java   # Annuaire
        ├── TripCreationView.java
        ├── TripSearchView.java
        ├── AdminStudentCreationView.java # Admin: créer étudiant
        ├── AdminWhitelistView.java       # Admin: gérer whitelist
        └── PendingStudentsView.java      # Admin: valider étudiants
```

## Vues disponibles

| Route | Vue | Accès | Description |
|-------|-----|-------|-------------|
| `/login` | LoginView | Public | Authentification |
| `/register` | RegisterView | Public | Inscription publique |
| `/` | StudentView | Authentifié | Annuaire des étudiants |
| `/proposer-trajet` | TripCreationView | Authentifié | Formulaire de création de trajet |
| `/rechercher-trajet` | TripSearchView | Authentifié | Recherche de trajets |
| `/admin/create-student` | AdminStudentCreationView | Admin | Créer un étudiant manuellement |
| `/admin/whitelist` | AdminWhitelistView | Admin | Gérer les codes étudiants autorisés |
| `/admin/pending-students` | PendingStudentsView | Admin | Valider/rejeter les étudiants en attente |

## Fonctionnalités à implémenter

### 🔴 Phase 5 : Système de réservation
- [ ] Créer l'entité `Booking` (réservation)
- [ ] Port + Service `BookingService`
- [ ] Implémenter `TripService.bookTrip(tripId)`
- [ ] Ajouter bouton "Réserver" dans TripSearchView
- [ ] Vue "Mes réservations"

### 🟢 Améliorations futures
- [ ] Exploitation du flag `isRegular` (trajets réguliers)
- [ ] Filtres avancés de recherche (date, horaire)
- [ ] Profil utilisateur éditable
- [ ] Système de notifications/messages
- [ ] Validation côté client (Vaadin Binder)
- [ ] Tests unitaires (JUnit + Mockito)
- [ ] Documentation API (Swagger)
- [ ] Migration SSO école (optionnel)

## Base de données

### Tables principales
- `student` : Étudiants (avec champs auth : username, password, role, approved, enabled, etc.)
- `trip` : Trajets de covoiturage
- `allowed_student_code` : Whitelist des codes étudiants autorisés
- `spring_session` : Sessions utilisateurs (gérée par Spring Session JDBC)

### Accès à la base
Utilisez un client MySQL (MySQL Workbench, DBeaver, phpMyAdmin) :
- Host : `localhost:3306`
- Database : `covoiturage_db`
- User : `root`
- Password : (vide)

## Historique des développements

### Correction suppression étudiant (27/11/2025) ✅
- **Problème** : Impossible de supprimer un étudiant ayant utilisé un code whitelist
  - Erreur : `SQLIntegrityConstraintViolationException` (contrainte de clé étrangère)
  - Le code restait marqué comme "utilisé" même après suppression
- **Solution** :
  - Configuration `ON DELETE SET NULL` sur la relation `usedBy`
  - Libération automatique du code lors de la suppression (`used=false`)
  - Le code redevient disponible pour une nouvelle inscription
- **Migration requise** : Script SQL fourni pour modifier la contrainte FK
- **Fichiers modifiés** : AllowedStudentCode.java, StudentService.java, AllowedStudentCodeService.java

### Système d'inscription et whitelist (27/11/2025) ✅
- **Implémenté** : Phases 2, 3, et 4 complètes
- **Nouvelles fonctionnalités** :
  - Inscription publique avec validation par whitelist
  - Gestion admin de la whitelist (CRUD)
  - Validation admin des étudiants en attente
  - Champ `approved` dans l'entité Student
  - Section administration dans la sidebar (visible pour admins)
- **8 nouveaux fichiers** créés (entités, services, vues)
- **4 fichiers modifiés** (Student, MainLayout, LoginView, DataInitializer)

### LogoutButton NullPointerException (27/11/2025) ✅
- **Problème** : `UI.getCurrent()` retournait `null` après déconnexion
- **Solution** : Capture de la référence UI avant l'invalidation de session
- **Fichier** : `ui/component/LogoutButton.java`

## Documentation technique

Pour plus de détails sur l'architecture et les règles de code, consultez :
- **CLAUDE.md** : Guide complet pour le développement
- **plan.md** : Plan détaillé d'implémentation de l'authentification (phases 1-4)

## Auteurs

**Mehdi Tazerouti** et **Salim Bouskine**
Dauphine MIAGE SITN - Projet universitaire 2025
