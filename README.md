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
| Tracing Dependency | | Dépendance de gestion des Spans

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

## Tracing Management Dependency

```mermaid
sequenceDiagram
    participant User as Utilisateur
    participant Proxy as Proxy Spring (AOP)
    participant Aspect as TracingAspect
    participant Real as Controller
    participant OTel as OpenTelemetry

    User->>Proxy: Appel GET /api/process
    Note over Proxy: Spring intercepte l'appel
    Proxy->>Aspect: "Annotation @Monitored"
    
    rect rgb(240, 248, 255)
        Note right of Aspect: AVANT
        Aspect->>OTel: startSpan("traitement_standard")
    end
    
    Aspect->>Real: joinPoint.proceed() (Exécute le code)
    
    activate Real
    Real-->>Real: Thread.sleep()...
    Real-->>Aspect: Retourne "Traitement réussi"
    deactivate Real

    rect rgb(255, 240, 240)
        Note right of Aspect: APRÈS
        Aspect->>OTel: span.end()
    end
    
    Aspect-->>Proxy: Retourne le résultat
    Proxy-->>User: Réponse HTTP 200
```

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant Client as Service Client (@Monitored)
    participant Rest as RestTemplate
    participant API as Service API

    U->>Client: GET /api/chain
    activate Client
    Note right of Client: Span "orchestration_client"<br>TraceID: 123
    
    Client->>Rest: Appel HTTP vers Service A
    
    Note right of Rest: Injection Headers:<br>traceparent: 00-123-...
    
    Rest->>API: GET /api/process
    activate API
    Note right of API: Span "traitement_standard"<br>TraceID: 123 (Le même !)
    
    API-->>Rest: Réponse 200 OK
    deactivate API
    
    Rest-->>Client: Retour String
    deactivate Client
```