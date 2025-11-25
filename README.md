# covoiturage-vaadinoot)

**Projet universitaire — Module Programmation Back-end & Front-end**

## Description
Application de mise en relation pour le covoiturage étudiant (MVP) :
- Gestion des profils étudiants (nom, email, code étudiant)
- Proposer / rechercher des trajets (départ, destination, horaire, nb places)
- Mise en relation (affichage coordonnées / envoi message simulé)

## Stack technique (MVP)
- Frontend : Vaadin 24 (Java)
- Backend : Spring Boot 3 + Spring Data JPA (Hibernate)
- Base de données : PostgreSQL (prod) / H2 (local)
- Build : Maven
- Container : Docker (docker-compose)
- CI : GitHub Actions (pipeline build → tests → image)

## Structure du dépôt
```
/covoiturage-vaadin
 ├─ backend/             # Spring Boot app (Vaadin peut être ici)
 ├─ infra/               # docker-compose, manifests k8s (optionnel)
 ├─ docs/                # architecture, diagrammes, livrables
 ├─ .github/             # workflows CI
 └─ README.md
```

## How to run (dev)
1. Cloner le repo :
```bash
git clone https://github.com/<USER>/covoiturage-vaadin.git
cd covoiturage-vaadin/backend
```
2. Lancer en local (H2) :
```bash
./mvnw spring-boot:run
# ou via IDE (Eclipse / IntelliJ)
```

3. Avec Docker Compose (Postgres) — à venir : `infra/docker-compose.yml`

## Branching & workflow
- `main` : stable / livrable
- `dev` : intégration en cours
- `feature/<nom>` : nouvelles fonctionnalités
- PR obligatoire vers `dev`, puis `dev` → `main` après validation

## Livrables
- Code source (repo)
- Document d’architecture (docs/architecture.docx ou .pdf)
- Présentation (slides)

## Contribuer
- Créer une issue / assigner
- Ouvrir une PR propre (description + références issues)
- Respecter le style Java (formatting), ajouter tests unitaires

## Auteurs
- Mehdi Tazerouti et Salim Bouskine

---
