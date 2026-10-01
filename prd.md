# Irys — Product Requirements Document (PRD)

**Platform:** Android Native  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**Architecture:** MVVM + layered/clean architecture  
**DI:** Hilt  
**Database:** Room  
**Networking:** Retrofit + OkHttp  
**Async:** Kotlin Coroutines + Flow/StateFlow  
**Primary Communication:** Bluetooth Low Energy (BLE)  
**Networking Model:** Opportunistic / Store-Carry-Forward

---

# 1. Product Overview

Irys is an offline-first, disaster-resilient communication application for Android.

Its purpose is to allow users to exchange messages and emergency information when cellular networks, Wi-Fi, or Internet connectivity are unavailable or unreliable.

Nearby smartphones act as temporary communication nodes. A message can travel through intermediate devices:

```text
A → B → C → D
```

B and C can store and forward the message even when A and D never have a direct connection.

The fundamental networking principle is:

```text
Store → Carry → Forward
```

---

# 2. Vision

> When conventional communication infrastructure fails, nearby smartphones should be able to temporarily become the communication infrastructure.

Irys is an **offline-first communication system**, not a normal online messaging application with a secondary offline mode.

---

# 3. Problem Statement

During disasters and infrastructure failures:

- Cellular networks may become unavailable.
- Internet connectivity may disappear.
- Wi-Fi infrastructure may be inaccessible.
- Nearby smartphones may still communicate through Bluetooth.
- People and devices naturally move through affected areas.

Irys provides a decentralized, opportunistic communication layer above nearby-device connectivity.

---

# 4. Goals

## Primary

1. Discover nearby Irys devices.
2. Establish device-to-device BLE communication.
3. Send direct messages.
4. Persist messages locally.
5. Forward messages through intermediate devices.
6. Implement Store-Carry-Forward.
7. Detect duplicates.
8. Support TTL and expiration.
9. Support emergency broadcasts.
10. Provide delivery states.
11. Protect communication with authentication and encryption.
12. Operate without Internet.
13. Synchronize when Internet returns.

## Secondary

- Show network status clearly.
- Minimize battery usage.
- Handle unstable BLE connections.
- Support message priorities.
- Provide optional online synchronization.
- Keep networking independent from UI.

## Non-Goals for MVP

- Cellular-network replacement.
- Guaranteed delivery.
- High-bandwidth video.
- Video calls.
- Unlimited file sharing.
- Carrier-grade reliability.
- Perfect geographic routing.
- Permanent Bluetooth mesh infrastructure.

---

# 5. Users

- General users needing local communication.
- Disaster-affected users.
- Volunteers carrying messages between disconnected areas.
- Rescue/emergency teams.

---

# 6. Core Concepts

### Peer
Another Android device running Irys.

### Relay
A peer temporarily storing and forwarding a message.

### Store-Carry-Forward
A node stores a message, carries it while moving, then forwards it when another suitable peer becomes available.

### Opportunistic Network
A network where connectivity appears intermittently as devices encounter one another.

### TTL
Time-To-Live; how long a message remains eligible for forwarding.

### Hop
One forwarding operation between peers.

---

# 7. Core Flow

```text
User A
  ↓
Create message
  ↓
Encrypt
  ↓
Persist in Room
  ↓
Queue
  ↓
Discover peer
  ↓
BLE connection
  ↓
Handshake
  ↓
Message synchronization
  ↓
Transfer
  ↓
Peer B stores message
  ↓
B encounters C
  ↓
C stores/forwards
  ↓
Destination receives
  ↓
Delivery acknowledgement
```

---

# 8. Screens

```text
Splash
Onboarding
Permissions
Home
Chats
Chat
Nearby
Emergency
Emergency Detail
Profile
Settings
```

## Splash

Responsibilities:

- Initialize dependencies.
- Check local identity.
- Check onboarding state.
- Check permissions.
- Initialize local state.
- Determine initial route.

```text
Splash
 ├── first launch → Onboarding
 ├── permissions missing → Permissions
 └── ready → Home
```

## Onboarding

Explain:

1. What Irys is.
2. Communication without Internet.
3. Nearby devices as relays.
4. Privacy/security.
5. Required Bluetooth permissions.

Persist completion locally.

## Permissions

Handle Android runtime Bluetooth permissions as required by the target SDK and actual features, including applicable:

```text
BLUETOOTH_SCAN
BLUETOOTH_CONNECT
BLUETOOTH_ADVERTISE
```

Show permission state, reason, request and retry actions.

## Home

Show:

- Network status.
- Bluetooth status.
- Internet status.
- Nearby peer count.
- Pending messages.
- Recent chats.
- Emergency action.
- Local identity/status.

## Nearby

Features:

- Scan.
- Stop scan.
- List Irys peers.
- Signal strength where useful.
- Last seen.
- Connection state.
- Connect/disconnect.
- Protocol compatibility.

States:

```text
DISCOVERED
CONNECTING
CONNECTED
READY
DISCONNECTED
ERROR
```

## Chats

Show:

- Conversations.
- Last message.
- Timestamp.
- Unread count.
- Delivery state.
- Peer status.

Must work offline.

## Chat

Features:

- Message history.
- Text messages.
- Message status.
- Retry.
- Queued state.
- Delivered state.
- Failed/expired state.
- Connection state.

Message states:

```text
CREATED
QUEUED
STORED
TRANSFERRING
RECEIVED
FORWARDED
DELIVERED
FAILED
EXPIRED
```

## Emergency

Emergency types:

```text
SOS
MEDICAL
DANGER
FIRE
FLOOD
SHELTER
FOOD
WATER
MISSING_PERSON
GENERAL
```

Features:

- SOS.
- Create alert.
- Select type.
- Description.
- Priority.
- Broadcast.
- View received alerts.
- View details.

## Emergency Detail

Show type, priority, message, source information appropriate for privacy, timestamp, relevant propagation metadata, status and expiration.

## Profile

Show:

- Display name.
- Irys identity.
- Device identifier where appropriate.
- Public identity information.
- Security status.

Never expose private keys.

## Settings

Sections:

- Communication.
- Privacy.
- Battery.
- Security.
- Application/about.

---

# 9. Architecture

Required top-level structure:

```text
core/
data/
domain/
feature/
navigation/
```

Canonical structure:

```text
com.irys.app/

├── core/
│   ├── bluetooth/
│   ├── database/
│   ├── security/
│   ├── network/
│   ├── background/
│   ├── common/
│   └── ui/
│
├── data/
│   ├── model/
│   ├── repo/
│   ├── local/
│   └── remote/
│
├── domain/
│   ├── model/
│   ├── repo/
│   └── usecases/
│
├── feature/
│   ├── splash/
│   ├── onboarding/
│   ├── permissions/
│   ├── home/
│   ├── chats/
│   ├── chat/
│   ├── nearby/
│   ├── emergency/
│   ├── emergency_detail/
│   ├── profile/
│   └── settings/
│
└── navigation/
    ├── Routes.kt
    ├── IrysNavGraph.kt
    └── BottomNavigation.kt
```

Every substantial feature follows:

```text
feature/
└── some_feature/
    ├── SomeFeatureScreen.kt
    ├── SomeFeatureUiState.kt
    ├── SomeFeatureViewModel.kt
    └── components/
```

---

# 10. MVVM Responsibilities

```text
Compose Screen
      ↓
ViewModel
      ↓
UseCase
      ↓
Repository
      ↓
Data Sources
```

### Screen

Rendering and user events only.

### ViewModel

UI state, events, use-case calls, UI-level error handling.

### Use Case

One meaningful business operation.

### Repository

Abstraction over local, BLE and remote data sources.

---

# 11. Domain Layer

Domain models:

```text
Message
Peer
Conversation
Emergency
DeliveryStatus
ConnectionState
```

Repository interfaces:

```text
MessageRepository
PeerRepository
ConversationRepository
EmergencyRepository
```

Use cases:

```text
message/
    SendMessageUseCase
    ReceiveMessageUseCase
    ForwardMessageUseCase
    GetMessagesUseCase
    RetryMessageUseCase

peer/
    DiscoverPeersUseCase
    GetNearbyPeersUseCase
    ConnectPeerUseCase

emergency/
    SendEmergencyUseCase
    BroadcastEmergencyUseCase
    GetEmergencyMessagesUseCase

sync/
    SyncMessagesUseCase
    CleanupExpiredMessagesUseCase
```

---

# 12. Data Layer

Required:

```text
data/
├── model/
├── repo/
├── local/
└── remote/
```

### local

Room DAOs and local data sources.

### remote

Retrofit APIs and remote data sources.

### repo

Implementations of domain repository interfaces.

---

# 13. Core Layer

```text
core/
├── bluetooth/
├── database/
├── security/
├── network/
├── background/
├── common/
└── ui/
```

Bluetooth should be split into:

```text
bluetooth/
├── scanner/
├── advertiser/
├── connection/
├── protocol/
└── transport/
```

---

# 14. BLE System

BLE is the primary offline transport.

Components:

```text
BleManager

scanner/
    BleScanner
    ScanState

advertiser/
    BleAdvertiser

connection/
    BleConnectionManager
    GattClient
    GattServer
    ConnectionState

protocol/
    BlePacket
    BlePacketType
    PacketEncoder
    PacketDecoder
    PacketFragmenter
    PacketAssembler

transport/
    BleMessageTransport
    MessageSender
    MessageReceiver
```

`BleManager` orchestrates; it must not become a god class.

---

# 15. BLE Discovery

Every Irys device should:

- Advertise Irys service information.
- Scan for other Irys devices.
- Filter non-Irys devices.
- Create/update peer records.
- Track last seen.

Flow:

```text
BLE Scanner
 ↓
ScanResult
 ↓
Irys service detected
 ↓
Peer mapper
 ↓
Peer
 ↓
Peer registry
```

Advertisements must contain only minimal discovery information.

Do not put message content or unnecessary private information into advertisements.

---

# 16. GATT

Conceptual custom Irys service:

```text
Irys Service
│
├── Identity Characteristic
├── Control Characteristic
├── Message RX Characteristic
└── Message TX Characteristic
```

The exact UUIDs must be centralized.

### GATT Client

Responsible for:

- Connecting.
- Service discovery.
- Finding characteristics.
- Enabling notifications.
- Reading/writing.
- Handling callbacks.
- Closing connections.

### GATT Server

Responsible for:

- Hosting Irys service.
- Accepting characteristic interactions.
- Handling writes.
- Sending notifications.
- Managing subscriptions.

Raw Android BLE classes must not leak into domain/UI.

---

# 17. Connection State Machine

```text
DISCONNECTED
      ↓
CONNECTING
      ↓
CONNECTED
      ↓
DISCOVERING_SERVICES
      ↓
READY
      ↓
TRANSFERRING
      ↓
READY
      ↓
DISCONNECTING
      ↓
DISCONNECTED
```

Failures transition to `ERROR` and then retry/disconnect as appropriate.

Avoid scattered booleans such as `isConnected`, `isReady`, etc.

---

# 18. Irys Protocol

BLE is only transport. Irys needs an application-level protocol.

Minimum packet types:

```text
HELLO
HELLO_ACK
AUTH
AUTH_ACK
SYNC_REQUEST
SYNC_RESPONSE
MESSAGE
ACK
DELIVERY_ACK
ERROR
```

Protocol must be versioned.

Handshake:

```text
A → B : HELLO
B → A : HELLO_ACK
A ↔ B : authentication
A → B : SYNC_REQUEST
B → A : SYNC_RESPONSE
A ↔ B : message exchange
```

---

# 19. Message Model

Conceptually:

```text
messageId
sourceId
destinationId
conversationId
type
payload
priority
createdAt
expiresAt
hopCount
maxHops
status
```

Messages must have globally unique IDs within the Irys protocol.

---

# 20. Message Synchronization

Peers should exchange message summaries before payloads.

Example:

```text
A: M1 M2 M3 M4
B: M2 M4 M5

A → B: M1 M3
B → A: M5
```

Avoid blindly sending all stored messages on every connection.

---

# 21. Store-Carry-Forward

Example:

```text
A → B

B stores M.

B moves.

B → C

C stores M.

C → D
```

Messages must survive temporary connection loss, peer disappearance and application restart as long as they remain valid and storage is available.

---

# 22. Routing

Use opportunistic forwarding initially.

On peer encounter:

1. Authenticate/handshake.
2. Exchange message summaries.
3. Determine transferable messages.
4. Apply TTL/hop/priority rules.
5. Transfer.
6. Persist transfer state.
7. Process acknowledgements.

Routing logic must not be embedded in GATT classes.

---

# 23. Deduplication

Every message has a unique ID.

```text
message already processed?
    YES → ignore
    NO  → process
```

Deduplication belongs to data/network logic, not UI.

---

# 24. TTL and Hop Limit

Every propagating message must have expiration information.

Expired messages:

- Must not be forwarded.
- Must be removed eventually.
- Must not consume queue capacity.

Messages should also have a maximum hop count.

```text
hopCount += 1
```

Once the maximum is reached, forwarding stops.

---

# 25. Priority

```text
CRITICAL
HIGH
NORMAL
LOW
```

Priority affects forwarding order but cannot bypass security, TTL, hop limits, rate limits or size limits.

---

# 26. Message Queue

Persistent forwarding queue should consider:

1. Expiration.
2. Priority.
3. Destination relevance.
4. Hop limit.
5. Peer availability.
6. Replication policy.
7. Storage/bandwidth constraints.

---

# 27. Acknowledgements

Support clear acknowledgement semantics:

```text
RECEIVED
STORED
FORWARDED
DELIVERED
```

Transport acknowledgement and destination delivery acknowledgement are different concepts.

---

# 28. Security

Use established cryptographic primitives and Android security facilities.

Do not invent cryptography.

Each installation should have a cryptographic identity:

```text
Private Key → secure device storage
Public Key  → identity/authentication
```

End-to-end message encryption should prevent relay peers from reading forwarded plaintext where practical.

Incoming packets are untrusted and must be validated.

---

# 29. Room

Room is the persistent local source of truth.

Core entities:

```text
MessageEntity
ConversationEntity
PeerEntity
EmergencyEntity
```

Potential:

```text
DeliveryEntity
TransferEntity
```

Only add entities when actually required.

Persist messages and state that must survive app/process restarts.

---

# 30. Retrofit

Retrofit is only for online/backend functionality.

Possible uses:

- Device/account synchronization.
- Cloud synchronization.
- Backend registration.
- Emergency backend synchronization.

Core offline communication must never depend on Retrofit.

---

# 31. Online/Offline Modes

```text
Offline:
Device → BLE → Peer

Online:
Device → Retrofit → Backend

Hybrid:
Device → BLE → Peer
       → Retrofit → Backend
```

If Internet fails, local Irys functionality must continue.

---

# 32. Hilt

Use Hilt for:

- Room.
- DAOs.
- Retrofit.
- OkHttp.
- Repositories.
- BLE managers.
- Security managers.
- Application-scoped infrastructure.

Prefer constructor injection.

---

# 33. UI State

Use immutable `UiState` objects and `StateFlow`.

Example:

```kotlin
data class ChatUiState(
    val messages: List<MessageUiModel> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val isSending: Boolean = false,
    val error: String? = null
)
```

State:

```text
Repository → UseCase → ViewModel → UI
```

Events:

```text
UI → ViewModel → UseCase
```

---

# 34. Error Handling

Model meaningful errors:

```text
BluetoothUnavailable
PermissionDenied
PeerUnavailable
ConnectionFailed
ServiceDiscoveryFailed
TransferFailed
MessageExpired
StorageError
AuthenticationFailed
EncryptionError
NetworkUnavailable
ServerError
```

Never silently swallow exceptions.

---

# 35. Battery

BLE is battery-sensitive.

Requirements:

- Avoid unnecessary continuous scanning.
- Use appropriate scan modes.
- Stop scanning when unnecessary.
- Avoid unnecessary connections.
- Batch transfers where appropriate.
- Avoid duplicate synchronization.
- Minimize high-frequency logging.

Emergency mode may use different discovery behavior but must respect Android system constraints.

---

# 36. Background Execution

Design for Android lifecycle and system restrictions.

Possible components:

```text
Foreground Service
WorkManager
BLE callbacks
```

Do not assume unlimited background BLE execution.

Test:

- Screen off.
- Device locked.
- App backgrounded.
- Battery saver.
- Process death.

---

# 37. Testing

## Unit

Test:

- Use cases.
- Routing.
- TTL.
- Hop limits.
- Deduplication.
- Priority.
- State transitions.
- Serialization.
- Repository logic.

## Integration

Test:

- Room.
- Repository.
- BLE transport.
- Protocol.
- Synchronization.
- ViewModel state.

## Physical Devices

Use multiple real Android devices:

```text
A → B
A → B → C
A → B → C → D
```

Also test Bluetooth disabled, permission revoked, dropped connections, restarts, duplicates, expiration, storage pressure, battery saver and background behavior.

---

# 38. MVP

The first working MVP must demonstrate:

1. Native Android.
2. Kotlin.
3. Jetpack Compose.
4. MVVM.
5. Hilt.
6. Room.
7. BLE advertising.
8. BLE scanning.
9. Peer discovery.
10. GATT client/server.
11. Direct text messaging.
12. Persistent messages.
13. Message IDs.
14. Deduplication.
15. TTL.
16. Basic Store-Carry-Forward.
17. Basic multi-hop.

Demo:

```text
Phone A → Phone B → Phone C
```

A sends to C while B acts as relay.

---

# 39. Development Phases

## Phase 1
Project foundation: Kotlin, Compose, MVVM, Hilt, navigation, theme.

## Phase 2
Room, entities, DAOs, repositories.

## Phase 3
Bluetooth permissions, advertising, scanning, peer discovery.

## Phase 4
GATT client/server, service, characteristics, connection state machine, handshake.

## Phase 5
Message protocol, serialization, transfer, ACK, persistence.

## Phase 6
Store-Carry-Forward, synchronization, TTL, deduplication, routing.

## Phase 7
Identity, authentication, encryption, replay protection.

## Phase 8
Emergency system.

## Phase 9
Retrofit/backend synchronization.

## Phase 10
Battery, lifecycle, reliability and performance optimization.

---

# 40. Definition of Done

A feature is complete only when:

- Architecture is respected.
- State is correctly modeled.
- Business logic is in domain.
- Data access is abstracted.
- Errors are handled.
- Required persistence works.
- Relevant tests exist.
- Lifecycle behavior is considered.
- Offline behavior works where applicable.
- No unnecessary technical debt is introduced.

---

# 41. Final Architecture

```text
Compose UI
    ↓
ViewModel
    ↓
UseCase
    ↓
Domain Repository
    ↓
Data Repository
    ├── Room
    ├── BLE
    └── Retrofit
```

Conceptual system:

```text
                         IRYS
                          │
              ┌───────────┴───────────┐
              │                       │
           Offline                  Online
              │                       │
             BLE                   Retrofit
              │                       │
        Peer Discovery             Backend
              │
             GATT
              │
       Irys Protocol
              │
          Routing
              │
    Store-Carry-Forward
              │
       Local Room State
```

---

# 42. Final Product Definition

Irys is a native Android, offline-first communication system built with Kotlin and Jetpack Compose.

BLE provides transport. GATT provides the BLE data interface. The Irys protocol defines communication. Routing decides forwarding. Room provides persistence. Repositories abstract data. Use cases implement business operations. ViewModels coordinate UI state. Retrofit provides optional online synchronization.

The intended separation is:

```text
UI
does not know BLE.

ViewModel
does not know Room.

Domain
does not know Android framework details.

Repository
abstracts data access.

BLE
provides transport.

Routing
decides forwarding.

Room
provides persistence.

Retrofit
provides optional online synchronization.
```
