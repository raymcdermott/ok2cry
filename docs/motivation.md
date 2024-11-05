```mermaid
sequenceDiagram
User->>CLJS SPA: Clicks "Fetch Secret"
CLJ Service->>Third-Party-REST-API: Fetch secret
Note left of CLJ Service: TLS
Note left of Third-Party-REST-API: TLS
Third-Party-REST-API->>CLJ Service: secret
Note over CLJ Service: Clear text
CLJ Service->>CLJS SPA: secret
CLJS SPA->>User: Show secret
```
