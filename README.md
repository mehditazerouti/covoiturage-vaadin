# Covoiturage Vaadin

**Projet universitaire — Application de covoiturage pour étudiants**

## Description
Application de covoiturage développée avec Spring Boot et Vaadin, suivant une architecture hexagonale.

Fonctionnalités :
- Gestion des étudiants (CRUD)
- Proposer un trajet (départ, destination, horaire, places)
- Rechercher des trajets par destination
- Réserver une place (à venir)

## Stack technique
- **Frontend** : Vaadin 24.2.0
- **Backend** : Spring Boot 3.1.0 + Spring Data JPA
- **Base de données** : H2 (développement)
- **Build** : Maven

## Démarrage rapide

1. Cloner le projet
2. Lancer l'application :
```bash
mvn spring-boot:run
```
3. Accéder à l'application : `http://localhost:8080`

## Architecture

Structure hexagonale (ports & adapters) :

```
src/main/java/com/example/covoiturage_vaadin/
├── domain/model/           # Entités métier (Student, Trip)
├── application/
│   ├── ports/             # Interfaces (IStudentRepositoryPort, ITripRepositoryPort)
│   └── services/          # Services métier (StudentService, TripService)
├── infrastructure/adapter/ # Implémentations JPA
└── ui/view/               # Vues Vaadin
```

## Vues disponibles

- `/` : Gestion des étudiants
- `/proposer-trajet` : Proposer un trajet
- `/rechercher-trajet` : Rechercher des trajets

## Console H2

Accéder à la base de données : `http://localhost:8080/h2-console`
- JDBC URL : `jdbc:h2:mem:covoiturage`
- Username : `sa`
- Password : (vide)

## Fonctionnalités à implémenter

### Fonctionnel
- [ ] Authentification (SSO école)
- [ ] Code étudiant dans le profil
- [ ] Trajets réguliers (domicile → campus)
- [ ] Recherche par codes étudiants/emails
- [ ] Système de réservation complet
- [ ] Mise en relation (messages entre étudiants)

## Auteurs

Mehdi Tazerouti et Salim Bouskine
