# POC OpenTelemetry avec Spring Boot & Jaeger

POC d'implémentation d'un **Traçage distribué**entre deux microservices Spring Boot en utilisant le protocole **OTLP** via **gRPC**.

## Architecture

* **Service A (service-otlp-grpc)** : API Backend simulée.
* **Service B (service-client)** : Client orchestrateur qui appelle le Service A.
* **Jaeger** : Collecteur et Interface de visualisation des traces.

## Prérequis

* Docker & Docker Compose
* Java 21

## Démarrage rapide

1. Cloner le repository
2. Lancer le script de démarrage (compile et lance Docker) : `./start.sh`

## Accès
| Service |	URL	| Description |
|:---|:---|:---|
| Jaeger UI	| http://localhost:16686 | Visualisation des traces |
| Service Client | http://localhost:8081 | Interface pour générer du trafic |
| Service API | http://localhost:8080 | Backend (Appelé par le client) |

## Architecture
```mermaid
graph TD
    %% Définition des Styles


    %% Acteur Externe
    User(("Utilisateur /<br>Navigateur")):::browser

    %% Conteneur Docker (Frontière logique)
    subgraph DockerHost [Docker Compose Network]
        direction TB
        
        %% Services Java (Notez les guillemets ajoutés ci-dessous)
        Client["Service Client<br><i>(Service B)</i><br>Port: 8081"]:::javaService
        API["Service OTLP gRPC<br><i>(Service A)</i><br>Port: 8080"]:::javaService
        
        %% Infrastructure Jaeger
        Jaeger["Jaeger<br><i>(Collector + UI)</i>"]:::infra
    end

    %% FLUX MÉTIER (HTTP)
    User -- "1. Requête HTTP<br>(localhost:8081)" --> Client
    Client -- "2. Appel API RestTemplate<br>(http://service-otlp-grpc:8080)" --> API
    
    %% FLUX VISUALISATION
    User -. "4. Consultation UI<br>(localhost:16686)" .-> Jaeger

    %% FLUX TÉLÉMÉTRIE (OTLP)
    Client -- "3a. Envoi Trace (gRPC)" --> Jaeger
    API -- "3b. Envoi Trace (gRPC)" --> Jaeger

    %% Détails des liens
    linkStyle 0,1 stroke:#6366f1,stroke-width:3px;
    linkStyle 2 stroke:#f9f,stroke-width:2px,stroke-dasharray: 5 5;
    linkStyle 3,4 stroke:#22c55e,stroke-width:2px,stroke-dasharray: 3 3;
```