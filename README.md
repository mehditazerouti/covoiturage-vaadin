# Covoiturage Vaadin

**Projet universitaire — Application de covoiturage pour étudiants Dauphine**

## Description
Application de covoiturage développée avec Spring Boot et Vaadin, suivant une **architecture hexagonale** (Clean Architecture) avec système d'authentification complet.

## Fonctionnalités actuelles

### ✅ Authentification & Sécurité (Phase 1)
- **Login/Logout** : Authentification sécurisée avec BCrypt
- **Rôles** : Système USER/ADMIN avec contrôle d'accès
- **Session management** : Sessions persistées en base MySQL
- **Compte admin** : Créé automatiquement au démarrage (admin/admin123)

### ✅ Gestion des étudiants
- Annuaire des étudiants (avec avatars Vaadin)
- Suppression d'étudiants (réservée aux admins)
- Protection contre l'auto-suppression
- Filtrage des comptes ADMIN dans l'annuaire

### ✅ Gestion des trajets
- **Proposer un trajet** : Formulaire avec auto-assignation du conducteur
- **Rechercher des trajets** : Recherche par destination (insensible à la casse)
- Support des trajets réguliers (flag `isRegular`)

### ✅ Interface moderne
- Layout principal avec **sidebar navigation** (Vaadin AppLayout)
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
```

> **Note** : Pour l'instant, seul le compte admin existe. Le système d'inscription avec whitelist (Phases 2-4) n'est pas encore implémenté.

## Architecture

Structure hexagonale (ports & adapters) avec séparation stricte des couches :

```
src/main/java/com/example/covoiturage_vaadin/
├── domain/model/              # Entités métier
│   ├── Student.java           # Étudiant (avec champs auth)
│   └── Trip.java              # Trajet
├── application/
│   ├── ports/                 # Interfaces (contrats)
│   │   ├── IStudentRepositoryPort.java
│   │   └── ITripRepositoryPort.java
│   └── services/              # Services métier (cas d'usage)
│       ├── StudentService.java
│       ├── TripService.java
│       └── SecurityContextService.java
├── infrastructure/
│   ├── adapter/               # Implémentations JPA
│   │   ├── StudentJpaRepository + Adapter
│   │   └── TripJpaRepository + Adapter
│   ├── security/              # UserDetailsService
│   │   └── UserDetailsServiceImpl.java
│   └── config/                # Configuration Security + Data
│       ├── VaadinSecurityConfiguration.java
│       └── DataInitializer.java
└── ui/
    ├── component/             # Composants réutilisables
    │   ├── MainLayout.java    # Layout principal + sidebar
    │   └── LogoutButton.java
    └── view/                  # Vues Vaadin
        ├── LoginView.java     # Authentification
        ├── StudentView.java   # Annuaire
        ├── TripCreationView.java
        └── TripSearchView.java
```

## Vues disponibles

| Route | Vue | Accès | Description |
|-------|-----|-------|-------------|
| `/login` | LoginView | Public | Authentification |
| `/` | StudentView | Authentifié | Annuaire des étudiants |
| `/proposer-trajet` | TripCreationView | Authentifié | Formulaire de création de trajet |
| `/rechercher-trajet` | TripSearchView | Authentifié | Recherche de trajets |

## Fonctionnalités à implémenter

### 🔴 Phase 2 : Système de whitelist (selon plan.md)
- [ ] Créer l'entité `AllowedStudentCode`
- [ ] Port `IAllowedStudentCodeRepositoryPort` + Adapter JPA
- [ ] Service `AllowedStudentCodeService`
- [ ] DataInitializer : ajouter codes pré-autorisés (20240001, 20240002, 20240003)

### 🔴 Phase 3 : Interface admin whitelist
- [ ] Vue `AdminWhitelistView` (@RolesAllowed("ADMIN"))
- [ ] CRUD des codes autorisés
- [ ] Grid avec colonnes : code, utilisé, créé par, date
- [ ] Lien dans la sidebar (admin uniquement)

### 🔴 Phase 4 : Inscription étudiants
- [ ] Service `AuthenticationService.registerStudent()`
- [ ] Vue `RegisterView` (formulaire inscription)
- [ ] Validation code étudiant via whitelist
- [ ] Lien inscription sur LoginView
- [ ] Modifier `TripCreationView` : retirer sélection conducteur

### 🟡 Phase 5 : Système de réservation
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
- `student` : Étudiants (avec champs auth : username, password, role, etc.)
- `trip` : Trajets de covoiturage
- `spring_session` : Sessions utilisateurs (gérée par Spring Session JDBC)

### Accès à la base
Utilisez un client MySQL (MySQL Workbench, DBeaver, phpMyAdmin) :
- Host : `localhost:3306`
- Database : `covoiturage_db`
- User : `root`
- Password : (vide)

## Bugs corrigés

### LogoutButton NullPointerException (27/11/2024) ✅
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
