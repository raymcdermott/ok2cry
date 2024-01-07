# Client produces disposable key

## No data store required
Do not be tempted to store the keys in the device or the browser. Create a new one per call or after a failed call.

The ok2cry library takes care of this for you if you prefer not to roll your own.

## Service example

```mermaid
sequenceDiagram
User->>SPA: Clicks form "Send" button
SPA->>SPA: Produce temporary key pair
SPA->>Service Instance: Make data request, includes public key
Service Instance->> X-Y-Z: Do X-Y-Z to get data
Service Instance->>Service Instance: Encrypt data using public key
Service Instance->>SPA: Send encrypted data
SPA->>SPA: Decrypt using private key
SPA->>SPA: Destroy temporary key pair
SPA->>User: Show decrypted data
```

## Serverless example

```mermaid
sequenceDiagram
User->>SPA: Clicks form "Send" button
SPA->>SPA: Produce temporary key pair
SPA->>Lambda: Make data request, includes public key
Lambda->> X-Y-Z: Do X-Y-Z to get data
Lambda->>Lambda: Encrypt data using public key
Lambda->>SPA: Send encrypted data
SPA->>SPA: Decrypt using private key
SPA->>SPA: Destroy temporary key pair
SPA->>User: Show decrypted data
```
