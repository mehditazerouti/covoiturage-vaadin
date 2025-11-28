# Guide Claude - Covoiturage Vaadin

## Architecture du projet

**Architecture hexagonale** (Clean Architecture) avec Spring Boot + Vaadin + Spring Security

### Couches

1. **Domain** (`domain/model/`)
   - `Student` : id, name, email, **studentCode**, **username**, **password** (BCrypt), **role** (USER/ADMIN), enabled, **approved**, createdAt
   - `Trip` : id, departureAddress, destinationAddress, departureTime, totalSeats, availableSeats, isRegular, driver (ManyToOne → Student)
   - `Booking` : id, trip (ManyToOne → Trip), student (ManyToOne → Student), bookedAt, status (PENDING/CONFIRMED/CANCELLED)
   - `BookingStatus` : Enum (PENDING, CONFIRMED, CANCELLED)
   - `AllowedStudentCode` : id, studentCode (unique), used, createdAt, createdBy, usedBy (ManyToOne → Student)
   - Méthodes métier : `Trip.bookSeat()`, `Booking.cancel()`, `Booking.isActive()`, `AllowedStudentCode.markAsUsed(Student)`

2. **Application** (`application/`)
   - **Ports** : `IStudentRepositoryPort`, `ITripRepositoryPort`, `IBookingRepositoryPort`, `IAllowedStudentCodeRepositoryPort` (interfaces)
   - **Services** :
     - `StudentService` : Gestion étudiants
     - `TripService` : Gestion trajets (auto-assign driver via SecurityContext, update, delete, canEdit)
     - `BookingService` : Gestion réservations (create, cancel, getMyBookings, getBookingsByTrip)
     - `SecurityContextService` : Abstraction du SecurityContext
     - `AllowedStudentCodeService` : Gestion de la whitelist des codes étudiants
     - `AuthenticationService` : Gestion de l'inscription et approbation des étudiants
   - Services annotés avec `@Transactional(readOnly = true)` pour lectures, `@Transactional` pour écritures

3. **Infrastructure** (`infrastructure/`)
   - **Adapters** : `StudentRepositoryAdapter`, `TripRepositoryAdapter`, `BookingRepositoryAdapter`, `AllowedStudentCodeRepositoryAdapter` (implémentent les ports)
   - **JPA Repositories** : `StudentJpaRepository`, `TripJpaRepository`, `BookingJpaRepository`, `AllowedStudentCodeJpaRepository` (Spring Data)
   - **Security** :
     - `VaadinSecurityConfiguration` : Configuration Spring Security pour Vaadin
     - `UserDetailsServiceImpl` : Authentification via Student
     - BCrypt pour le hashing des mots de passe
   - **Config** :
     - `DataInitializer` : Compte admin par défaut + codes étudiants whitelistés (22405100, 22405101, 22405102)

4. **UI** (`ui/`)
   - **Layout** : `MainLayout` (AppLayout avec sidebar + header + logout)
     - Section navigation principale (tous utilisateurs)
     - Section administration (visible uniquement pour ROLE_ADMIN)
   - **Components** :
     - `LogoutButton` (✅ corrigé : capture UI avant logout)
     - `TripEditDialog` (✅ Dialog édition/suppression trajet avec validation)
   - **Views publiques** :
     - `LoginView` (`/login`) : Authentification [@AnonymousAllowed]
       - Lien vers RegisterView
       - Traduction française du formulaire
     - `RegisterView` (`/register`) : Inscription publique [@AnonymousAllowed]
       - Si code whitelisté → compte activé immédiatement
       - Si code non whitelisté → compte en attente de validation admin
   - **Views utilisateur** [@PermitAll] :
     - `TripSearchView` (`/`) : Recherche + Réservation + Modification trajets
       - Recherche par destination (insensible à la casse)
       - Bouton "Réserver" pour chaque trajet (avec validation)
       - Bouton "Modifier" visible pour conducteur OU admin
       - Texte grisé "—" pour les autres utilisateurs
     - `TripCreationView` (`/proposer-trajet`) : Formulaire création trajet
       - ⚠️ Pas de sélection conducteur : **auto-assigné** depuis SecurityContext
       - Checkbox pour trajets réguliers
     - `MyBookingsView` (`/mes-reservations`) : Mes réservations
       - Grid : Trajet, Date/Heure, Conducteur, Places dispo, Réservé le, Statut, Actions
       - Badge coloré par statut (vert/rouge/gris)
       - Bouton "Annuler" pour réservations actives
   - **Views admin** [@RolesAllowed("ADMIN")] :
     - `StudentView` (`/students`) : Annuaire étudiants
       - Colonne "Actions" (suppression) visible **uniquement pour ROLE_ADMIN**
       - Protection : impossible de se supprimer soi-même
       - Filtrage : n'affiche pas les comptes ADMIN
     - `AdminStudentCreationView` (`/admin/create-student`) : Création manuelle d'étudiant par admin
     - `AdminWhitelistView` (`/admin/whitelist`) : Gestion CRUD de la whitelist
       - Grid : code, utilisé, utilisé par, créé par, date, actions
       - Protection : impossible de supprimer un code utilisé
     - `PendingStudentsView` (`/admin/pending-students`) : Validation des étudiants en attente
       - Affiche les étudiants avec approved=false
       - Actions : Approuver (whitelist + activer) ou Rejeter (supprimer)

## Entités JPA

### Relations importantes
```java
// Trip.java
@ManyToOne(fetch = FetchType.EAGER)
private Student driver;

// Booking.java
@ManyToOne(fetch = FetchType.EAGER)
@OnDelete(action = OnDeleteAction.CASCADE)  // ⚠️ IMPORTANT : Cascade delete
private Trip trip;

@ManyToOne(fetch = FetchType.EAGER)
private Student student;
```
**EAGER nécessaire** pour éviter `LazyInitializationException` dans les vues Vaadin
**CASCADE** sur Booking → Trip pour supprimer automatiquement les réservations quand un trajet est supprimé

### Modèle d'authentification
- **Student** est le principal de sécurité (pas d'entité User séparée)
- `username` et `email` sont uniques
- `role` : "ROLE_USER" ou "ROLE_ADMIN"
- `password` : BCrypt avec force 10 (défaut)
- `approved` : true = compte validé, false = en attente de validation admin
- `enabled` : true = peut se connecter, false = compte désactivé

## Configuration

### Base de données (application.properties)
- **MySQL** : `jdbc:mysql://localhost:3306/covoiturage_db`
- DDL : `update` (préserve les données)
- Username : `root` / Password : (vide)
- ⚠️ Console H2 désactivée (migration vers MySQL)

### Session management
- `spring.session.store-type=jdbc` : Sessions persistées en base MySQL
- Tables créées automatiquement par spring-session

### Logging SQL
- `spring.jpa.show-sql=true` : affiche les requêtes SQL
- `logging.level.org.hibernate.SQL=DEBUG` : logs détaillés
- Utile en dev, à désactiver en prod

## Règles de code

1. **Ne jamais** injecter directement les JPA repositories dans les services
   - ✅ Services → Ports (interfaces)
   - ✅ Adapters → JPA Repositories

2. **Transactions**
   - Lectures : `@Transactional(readOnly = true)`
   - Écritures : `@Transactional`

3. **Vues Vaadin**
   - Injectent les Services (pas les repositories)
   - Routes : `@Route("chemin")` et `@PageTitle("Titre")`
   - Annotations sécurité : `@PermitAll`, `@AnonymousAllowed`, `@RolesAllowed("ADMIN")`

4. **Sécurité**
   - Toujours utiliser `SecurityContextService` pour accéder au contexte
   - Ne jamais manipuler directement `SecurityContextHolder` dans les services métier
   - `TripService.proposeTrip()` récupère automatiquement le conducteur connecté

## Authentification & Autorisation

### ✅ Phases 1 à 4 : Système complet d'inscription et whitelist (IMPLÉMENTÉ)

#### Phase 1 : Authentification de base
- ✅ Login/Logout fonctionnel (LoginView)
- ✅ BCrypt pour les mots de passe
- ✅ Rôles USER/ADMIN (via Student.role)
- ✅ Protection des routes par rôles (@PermitAll, @RolesAllowed)
- ✅ MainLayout avec navigation drawer
- ✅ LogoutButton corrigé (capture UI avant invalidation session)
- ✅ Compte admin par défaut (DataInitializer)

#### Phase 2 : Système de whitelist
- ✅ Entité `AllowedStudentCode` (whitelist codes étudiants)
- ✅ Port `IAllowedStudentCodeRepositoryPort` + Adapter JPA
- ✅ Service `AllowedStudentCodeService`
- ✅ DataInitializer : codes pré-autorisés (22405100, 22405101, 22405102)

#### Phase 3 : Interface admin whitelist
- ✅ Vue `AdminWhitelistView` (@RolesAllowed("ADMIN"))
- ✅ CRUD complet des codes autorisés
- ✅ Grid avec colonnes : code, utilisé, utilisé par, créé par, date
- ✅ Protection : impossible de supprimer un code utilisé

#### Phase 4 : Inscription étudiants
- ✅ Service `AuthenticationService.registerStudent()` et `approveStudent()`
- ✅ Vue `RegisterView` (formulaire inscription public)
- ✅ Validation code étudiant via whitelist :
  - Code whitelisté → approved=true, enabled=true (accès immédiat)
  - Code non whitelisté → approved=false, enabled=false (en attente)
- ✅ Lien inscription sur LoginView
- ✅ Vue `PendingStudentsView` pour valider/rejeter les étudiants en attente
- ✅ Champ `approved` ajouté à l'entité Student

### Compte admin par défaut
```
Username: admin
Password: admin123
Email: admin@dauphine.eu
Code: ADMIN001
```

### Codes étudiants whitelistés par défaut
```
22405100, 22405101, 22405102
```

## ✅ Phase 5 : Système de réservation (IMPLÉMENTÉ 28/11/2025)

### Entités créées
- ✅ `Booking` : Réservation avec statut
- ✅ `BookingStatus` : Enum (PENDING, CONFIRMED, CANCELLED)

### Services & Règles métier
- ✅ `BookingService.createBooking(tripId)` :
  - Vérifie qu'un étudiant ne réserve pas son propre trajet
  - Vérifie qu'il n'a pas déjà une réservation active
  - Vérifie les places disponibles
  - Appelle `Trip.bookSeat()` pour décrémenter
- ✅ `BookingService.cancelBooking(bookingId)` :
  - Vérifie permissions (propriétaire OU admin)
  - Re-incrémente les places
  - Marque le statut CANCELLED
- ✅ `BookingService.getMyBookings()` : Liste pour l'utilisateur connecté
- ✅ `BookingService.existsActiveBookingByTripIdAndStudentId()` : Ignorer réservations annulées

### Vues
- ✅ `TripSearchView` : Bouton "Réserver" fonctionnel
- ✅ `MyBookingsView` : Liste + Annulation

### Corrections
- ✅ Cascade DELETE sur Booking → Trip (ON DELETE CASCADE)
- ✅ Réservation après annulation (vérification des réservations actives uniquement)

## Améliorations futures

### 🎨 Architecture & Code
- **DTOs (Data Transfer Objects)** :
  - Séparer les entités JPA de l'API avec des DTOs
  - Mapper avec MapStruct ou ModelMapper
  - Exemples : TripDTO, BookingDTO, StudentDTO
- **Spécifications JPA** pour requêtes complexes
- **Validation JSR-303** sur les DTOs
- **Pagination** avec Spring Data Pageable

### 🎨 Interface utilisateur
- **Design System Neobrutalism** :
  - Couleurs vives (jaune, cyan, magenta)
  - Bordures épaisses (3-5px) en noir
  - Ombres décalées (box-shadow: 5px 5px 0px black)
  - Pas de border-radius
  - Typographie bold uppercase
- **Dialogs pour CRUD** :
  - ✅ TripEditDialog (fait)
  - StudentEditDialog
  - BookingCancelDialog
  - TripBookingDialog
  - WhitelistCodeDialog
- **Composants réutilisables** :
  - ConfirmDialog générique
  - FormDialog générique
  - StatusBadge
  - AvatarComponent

### 🚀 Fonctionnalités
- Exploitation du flag `isRegular` (trajets réguliers vs ponctuels)
- Filtres avancés de recherche (date, horaire, nombre de places)
- Système de messages (conducteur ↔ passagers)
- Système d'évaluation (Review avec note + commentaire)
- Profil utilisateur éditable (photo, préférences, historique)
- Notifications en temps réel (Vaadin Push / WebSocket)

### 🔧 Technique
- Tests unitaires (JUnit 5 + Mockito)
- Tests E2E (Vaadin TestBench)
- Mise en cache (Spring Cache)
- Performance (indexation MySQL, lazy loading)
- Sécurité (rate limiting, HTTPS)
- CI/CD (GitHub Actions)
- Conteneurisation (Docker + Docker Compose)
- Monitoring (Actuator + Prometheus + Grafana)

## Technologies

- Spring Boot 3.1.0
- Vaadin 24.2.0
- Spring Security 6.1.0
- Hibernate/JPA
- MySQL 8.0 (XAMPP/local)
- Spring Session JDBC
- Maven

## Historique des développements

### Phase 5 : Système de réservation (✅ 28/11/2025)
- **Implémenté** : Système complet de réservation
- **Nouvelles entités** : Booking, BookingStatus
- **Nouveau service** : BookingService avec règles métier
- **Nouvelles vues** : MyBookingsView
- **Modifications** : TripSearchView (bouton Réserver), TripService (auto-assign driver)
- **6 nouveaux fichiers**, 2 fichiers modifiés

### Édition/Suppression trajets (✅ 28/11/2025)
- **Implémenté** : Système d'édition et suppression de trajets
- **Nouveau composant** : TripEditDialog (formulaire + validation)
- **Nouveaux services** : TripService.updateTrip(), deleteTrip(), canEditTrip()
- **Cascade delete** : Suppression trajet = suppression réservations
- **1 nouveau fichier** (TripEditDialog), 3 fichiers modifiés

### Corrections critiques (✅ 28/11/2025)
- **Problème 1** : Contrainte FK bloquait suppression trajets avec réservations
  - Solution : @OnDelete CASCADE sur Booking → Trip
  - Migration SQL : ALTER TABLE booking ... ON DELETE CASCADE
- **Problème 2** : Impossible de réserver après annulation
  - Solution : existsActiveBookingByTripIdAndStudentId() ignore CANCELLED
  - 4 fichiers modifiés

### Correction suppression étudiant avec code whitelist (✅ 27/11/2025 20:00)
- **Problème** : Erreur de contrainte de clé étrangère lors de la suppression d'un étudiant ayant utilisé un code whitelist
  - `SQLIntegrityConstraintViolationException`: Cannot delete or update a parent row
  - Le code restait marqué comme "utilisé" après suppression
- **Solution** :
  1. Ajout `@OnDelete(action = OnDeleteAction.SET_NULL)` sur `AllowedStudentCode.usedBy` (ligne 37)
  2. Modification `StudentService.deleteStudent()` pour libérer automatiquement le code (ligne 45-60)
  3. Ajout méthode `AllowedStudentCodeService.saveCode()` (ligne 95)
- **Migration SQL requise** :
  ```sql
  ALTER TABLE allowed_student_code DROP FOREIGN KEY FKb6y4t1fmdirvxv4ny3otlku8k;
  ALTER TABLE allowed_student_code ADD CONSTRAINT FKb6y4t1fmdirvxv4ny3otlku8k
  FOREIGN KEY (used_by_id) REFERENCES student(id) ON DELETE SET NULL;
  ```
- **Fichiers modifiés** :
  - `domain/model/AllowedStudentCode.java:37` : Annotation OnDelete
  - `application/services/StudentService.java:45-60` : Logique de libération du code
  - `application/services/AllowedStudentCodeService.java:95` : Méthode saveCode

### Système d'inscription et whitelist (✅ 27/11/2025 18:00)
- **Implémenté** : Phases 2, 3, et 4 complètes
- **Fichiers ajoutés** :
  - `domain/model/AllowedStudentCode.java` : Entité whitelist
  - `application/services/AuthenticationService.java` : Service inscription
  - `application/services/AllowedStudentCodeService.java` : Service whitelist
  - `ui/view/RegisterView.java` : Formulaire inscription public
  - `ui/view/AdminWhitelistView.java` : Gestion admin de la whitelist
  - `ui/view/PendingStudentsView.java` : Validation des étudiants en attente
  - Ports et adapters correspondants
- **Fichiers modifiés** :
  - `domain/model/Student.java` : Ajout champ `approved`
  - `ui/component/MainLayout.java` : Section admin avec 3 nouveaux liens
  - `ui/view/LoginView.java` : Lien vers RegisterView
  - `infrastructure/config/DataInitializer.java` : Ajout codes whitelistés

### LogoutButton NullPointerException (✅ 27/11/2025 16:00)
- **Problème** : `UI.getCurrent()` retournait `null` après `SecurityContextLogoutHandler.logout()`
- **Solution** : Capturer la référence UI **avant** l'invalidation de session
- **Fichier** : `ui/component/LogoutButton.java:22`
