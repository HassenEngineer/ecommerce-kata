# My Fullstack App — Backend

API REST e-commerce développée avec **Spring Boot**, conteneurisée avec **Docker** et **PostgreSQL**.

## Stack technique

| Couche             | Technologie                      |
|--------------------|----------------------------------|
| Backend            | Spring Boot 3.5, Java 21, Maven  |
| Base de données    | PostgreSQL 16 (prod), H2 (dev)   |
| Migrations         | Flyway (prod), data.sql (dev)    |
| Conteneurisation   | Docker, Docker Compose           |

## Architecture

```
┌─────────────┐       ┌─────────────┐
│   Backend   │──────▶│ PostgreSQL  │
│ (Boot :8080)│  JDBC │   (:5432)   │
└─────────────┘       └─────────────┘
```

Le backend expose une API REST sur le port 8080 et communique avec PostgreSQL via JPA/Hibernate.

## Fonctionnalités

### Produits (`/api/products`)
- CRUD complet (création, lecture, mise à jour, suppression)
- Gestion du stock et du prix (BigDecimal)

### Catégories (`/api/categories`)
- Catégories hiérarchiques (parent/enfant)
- Association many-to-many avec les produits
- Gestion des sous-catégories

### Panier (`/api/carts`)
- Création de paniers multiples
- Ajout/suppression d'articles avec gestion des quantités
- Calcul automatique du total et des sous-totaux
- Horodatage de création et de mise à jour

## Lancement avec Docker (production)

### Prérequis

- [Docker](https://docs.docker.com/get-docker/) et Docker Compose installés

### Démarrage

```bash
docker compose up --build
```

Cela démarre 2 services :

| Service    | Description                          | Port exposé |
|------------|--------------------------------------|-------------|
| `db`       | PostgreSQL 16 avec volume persistant | aucun       |
| `backend`  | Spring Boot avec profil `prod`       | **8080**    |

Le backend attend que PostgreSQL soit sain (healthcheck) avant de démarrer. Le schéma et les données initiales sont créés par **Flyway** (migrations `V001` et `V002`).

### Accès

- **API REST** : http://localhost:8080/api/products

### Arrêt

```bash
docker compose down
```

Pour supprimer également le volume de données PostgreSQL :

```bash
docker compose down -v
```

## Développement local

```bash
cd backend
./mvnw spring-boot:run
```

Le profil `dev` est actif par défaut :
- Base **H2 en mémoire** avec schéma auto-généré (`ddl-auto: create-drop`)
- Données initiales chargées via `data.sql` (6 produits, 5 catégories)
- Console H2 : http://localhost:8080/api/h2-console
- Flyway désactivé

## Gestion du schéma

| Environnement | Stratégie              | Détail                                          |
|---------------|------------------------|-------------------------------------------------|
| **dev**       | `data.sql` + Hibernate | Schéma auto-généré, données via `data.sql`      |
| **prod**      | Flyway                 | `V001__create_schema.sql` + `V002__insert_sample_data.sql` |

Les migrations Flyway se trouvent dans `backend/src/main/resources/db/migration/`.

## Collection Postman

Le fichier `postman-collection.json` à la racine du projet contient tous les endpoints prêts à tester.

### Import dans Postman

1. Ouvrir Postman
2. **File > Import** (ou Ctrl+O)
3. Sélectionner le fichier `postman-collection.json`
4. La variable `{{baseUrl}}` est pré-configurée sur `http://localhost:8080/api`

### Requêtes disponibles

| Dossier      | Requêtes                                                                 |
|--------------|--------------------------------------------------------------------------|
| **Products** | Lister, Obtenir, Créer, Modifier, Supprimer                             |
| **Categories** | Lister, Obtenir, Créer, Créer sous-catégorie, Modifier, Associer/Dissocier produit, Ajouter/Retirer sous-catégorie, Supprimer |
| **Carts**    | Créer panier, Obtenir, Ajouter article, Modifier quantité, Retirer article, Supprimer panier |

## API REST

### Produits

| Méthode  | Endpoint              | Description              |
|----------|-----------------------|--------------------------|
| `GET`    | `/api/products`       | Lister tous les produits |
| `GET`    | `/api/products/{id}`  | Obtenir un produit       |
| `POST`   | `/api/products`       | Créer un produit         |
| `PUT`    | `/api/products/{id}`  | Modifier un produit      |
| `DELETE` | `/api/products/{id}`  | Supprimer un produit     |

### Catégories

| Méthode  | Endpoint                                       | Description                    |
|----------|------------------------------------------------|--------------------------------|
| `GET`    | `/api/categories`                              | Lister les catégories racines  |
| `GET`    | `/api/categories/{id}`                         | Obtenir une catégorie          |
| `POST`   | `/api/categories`                              | Créer une catégorie            |
| `PUT`    | `/api/categories/{id}`                         | Modifier une catégorie         |
| `DELETE` | `/api/categories/{id}`                         | Supprimer une catégorie        |
| `PUT`    | `/api/categories/{catId}/products/{prodId}`    | Associer un produit            |
| `DELETE` | `/api/categories/{catId}/products/{prodId}`    | Dissocier un produit           |
| `PUT`    | `/api/categories/{catId}/subcategories/{subId}`| Ajouter une sous-catégorie     |
| `DELETE` | `/api/categories/{catId}/subcategories/{subId}`| Retirer une sous-catégorie     |

### Panier

| Méthode  | Endpoint                              | Description          |
|----------|---------------------------------------|----------------------|
| `POST`   | `/api/carts`                          | Créer un panier      |
| `GET`    | `/api/carts/{cartId}`                 | Obtenir un panier    |
| `POST`   | `/api/carts/{cartId}/items`           | Ajouter un article   |
| `PUT`    | `/api/carts/{cartId}/items/{itemId}`  | Modifier la quantité |
| `DELETE` | `/api/carts/{cartId}/items/{itemId}`  | Retirer un article   |
| `DELETE` | `/api/carts/{cartId}`                 | Supprimer un panier  |

## Structure du projet

```
my-fullstack-app/
├── docker-compose.yml
├── postman-collection.json
└── backend/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/example/backend/
        │   ├── controller/    # REST controllers
        │   ├── service/       # Logique métier
        │   ├── model/         # Entités JPA
        │   ├── dto/           # Objets de transfert
        │   ├── repository/    # Repositories Spring Data
        │   └── exception/     # Gestion des erreurs
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-prod.yml
            ├── data.sql
            └── db/migration/
                ├── V001__create_schema.sql
                └── V002__insert_sample_data.sql
```
