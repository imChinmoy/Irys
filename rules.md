# Irys — Engineering Rules & Development Guidelines

**Purpose:** This document is the engineering contract for implementing Irys with Antigravity.

---

# 1. General Standard

Build Irys like a production-quality Android application.

Optimize for:

- Correctness.
- Simplicity.
- Maintainability.
- Testability.
- Clear architecture.
- Android lifecycle correctness.
- Reliable offline behavior.
- Security.
- Battery efficiency.

Think like a senior Android developer before implementing anything.

---

# 2. Platform

Irys is **Android Native**.

Use:

```text
Kotlin
Jetpack Compose
Android SDK
```

Do not use Flutter, Dart, React Native, or other UI stacks unless explicitly requested later.

---

# 3. Architecture

Use:

```text
MVVM
+
layered / clean architecture principles
```

Required top-level structure:

```text
core/
data/
domain/
feature/
navigation/
```

---

# 4. Canonical Package Structure

```text
core/
├── bluetooth/
├── database/
├── security/
├── network/
├── background/
├── common/
└── ui/

data/
├── model/
├── repo/
├── local/
└── remote/

domain/
├── model/
├── repo/
└── usecases/

feature/
└── some_feature/
    ├── SomeFeatureScreen.kt
    ├── SomeFeatureUiState.kt
    ├── SomeFeatureViewModel.kt
    └── components/

navigation/
```

Do not restructure this arbitrarily.

---

# 5. Dependency Direction

Preferred flow:

```text
feature
   ↓
domain
   ↓
repository interfaces

data
   ↓
domain interfaces
```

Domain must not depend on:

- Compose.
- Activity/Fragment.
- BluetoothGatt.
- Room DAO.
- Retrofit.
- Android Context.

Framework-specific implementation belongs outside domain.

---

# 6. UI Rules

Compose screens should:

- Render state.
- Emit user events.
- Handle local UI concerns.
- Delegate business actions.

Do not put database, networking, BLE, or business logic in composables.

Preferred:

```text
Composable
    ↓
ViewModel
    ↓
UseCase
```

---

# 7. ViewModel Rules

ViewModels own:

- UI state.
- User events.
- Use-case calls.
- UI-level error mapping.
- State combination.

ViewModels must not directly use:

```text
Room DAO
Retrofit service
BluetoothGatt
BLE callbacks
```

---

# 8. UI State

Every non-trivial feature should have an explicit immutable UI state.

Example:

```kotlin
data class ChatUiState(
    val messages: List<MessageUiModel> = emptyList(),
    val isSending: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val error: String? = null
)
```

Expose state using `StateFlow`.

Flow:

```text
UI → ViewModel → UseCase
Repository → UseCase → ViewModel → UI
```

---

# 9. Domain Rules

The domain layer owns application behavior.

Use cases should have meaningful responsibilities:

```text
SendMessageUseCase
ForwardMessageUseCase
DiscoverPeersUseCase
SendEmergencyUseCase
SyncMessagesUseCase
CleanupExpiredMessagesUseCase
```

Do not create meaningless classes merely to increase abstraction.

---

# 10. Repository Rules

Repository interfaces belong in domain:

```text
domain/repo/
```

Implementations belong in:

```text
data/repo/
```

Repositories coordinate data sources but must not become god classes.

---

# 11. Room Rules

Use Room for persistent local state.

Room should persist data required across:

- Process death.
- App restart.
- Connection loss.
- Network partitions.

Use:

```text
Entity
DAO
Database
Repository
```

Keep Room-specific models separate from domain models where the boundary is useful.

---

# 12. Retrofit Rules

Retrofit is only for online/backend communication.

Never make core BLE communication depend on Retrofit.

Core offline operation must work with:

```text
Internet = unavailable
```

Retrofit failure must not break local BLE communication.

---

# 13. Hilt Rules

Use Hilt for dependency injection.

Provide:

- Room.
- DAOs.
- Retrofit.
- OkHttp.
- Repositories.
- BLE infrastructure.
- Security infrastructure.

Prefer constructor injection.

Do not manually build complex dependency graphs in Activities/ViewModels.

---

# 14. BLE Rules

BLE is infrastructure.

Do not expose raw Android Bluetooth objects to UI/domain.

Keep separate responsibilities:

```text
BleScanner
BleAdvertiser
BleConnectionManager
GattClient
GattServer
BleMessageTransport
Protocol classes
```

`BleManager` may orchestrate these components but must not become a god class.

---

# 15. GATT Rules

GATT callbacks belong to BLE infrastructure.

Do not leak:

```text
BluetoothGatt
BluetoothGattCallback
BluetoothGattCharacteristic
BluetoothDevice
```

into domain or feature packages.

Convert platform callbacks into application-level events/states.

---

# 16. Connection State

Do not use scattered booleans:

```text
isConnected
isConnecting
isReady
isTransferring
```

Use a coherent state machine:

```text
DISCONNECTED
CONNECTING
CONNECTED
DISCOVERING_SERVICES
READY
TRANSFERRING
DISCONNECTING
ERROR
```

---

# 17. BLE vs Protocol

Keep these concepts separate:

```text
BLE
=
transport

GATT
=
BLE data interface

Irys Protocol
=
application communication protocol

Routing
=
forwarding decision
```

Do not place routing logic inside GATT code.

---

# 18. Protocol Rules

Minimum protocol packet types:

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

Incoming packets are untrusted and must be validated.

---

# 19. Message Rules

Every message requires a unique ID.

Relevant metadata:

```text
messageId
sourceId
destinationId
conversationId
createdAt
expiresAt
priority
hopCount
maxHops
status
```

Do not use UI IDs as network identity.

---

# 20. Store-Carry-Forward

Store-Carry-Forward is domain/networking behavior.

Do not implement it inside:

```text
GattClient
GattServer
BleScanner
BleManager
```

Instead:

```text
Routing
 ↓
decides what to forward

BLE Transport
 ↓
transfers it
```

---

# 21. Deduplication

Deduplication belongs below UI.

Use `messageId`.

```text
Already processed?
YES → ignore
NO  → process
```

Never rely on the UI for duplicate prevention.

---

# 22. TTL

Always check expiration before forwarding.

Expired messages:

- Must not be forwarded.
- Should be cleaned from persistent storage.
- Must not occupy forwarding capacity.

---

# 23. Hop Limit

Use maximum hop count.

```text
hopCount += 1
```

Stop propagation once the configured limit is reached.

---

# 24. Priority

Use:

```text
CRITICAL
HIGH
NORMAL
LOW
```

Priority affects queue ordering.

It must not bypass:

- Security.
- TTL.
- Hop limit.
- Rate limits.
- Message size limits.

---

# 25. Security

Never implement cryptography from scratch.

Use established cryptographic primitives and Android security facilities.

Never log:

- Private keys.
- Session secrets.
- Tokens.
- Sensitive plaintext.
- Authentication material.

Incoming peers and packets are untrusted until validated.

---

# 26. Error Handling

Never silently swallow exceptions.

Avoid:

```kotlin
try {
    ...
} catch (e: Exception) {
}
```

Handle expected failures explicitly.

Convert technical errors into meaningful application/domain errors.

Never show raw exceptions to users.

---

# 27. Coroutines

Use Kotlin Coroutines for asynchronous operations.

Never block the main thread.

Avoid `GlobalScope`.

Respect structured concurrency and lifecycle-aware scopes.

---

# 28. Flow

Use Flow for reactive data:

- Messages.
- Peers.
- Connection states.
- Network state.
- Emergency alerts.

Use StateFlow for UI state.

Avoid unnecessary duplicated state.

---

# 29. Android Lifecycle

Design for:

- Process death.
- Backgrounding.
- Screen lock.
- Bluetooth disabled.
- Permission revocation.
- Battery saver.
- OS background restrictions.

Do not assume the app process remains alive indefinitely.

Important pending state must be persisted.

---

# 30. Background Work

Use Android-supported mechanisms:

```text
Foreground Service
WorkManager
BLE callbacks
```

Only introduce a foreground service when the actual product behavior requires it.

Do not assume unlimited background BLE execution.

---

# 31. Battery

Avoid:

- Continuous scanning when unnecessary.
- Repeated GATT connections.
- Polling.
- Duplicate synchronization.
- Redundant transfers.
- Excessive logging.

Prefer:

```text
event-driven behavior
adaptive scanning
batching
persistent queues
```

---

# 32. Naming

Use precise Kotlin names.

Good:

```text
ChatViewModel
MessageRepository
BleConnectionManager
SendMessageUseCase
```

Avoid vague names:

```text
Manager2
Helper
Utils2
DataHandler
Stuff
```

Names should communicate responsibility.

---

# 33. File/Class Size

Do not create giant classes.

Split classes when responsibility becomes unrelated.

Do not split code into tiny classes purely for architectural aesthetics.

Responsibility is the criterion.

---

# 34. Comments — IMPORTANT

**Do not blindly add comments.**

Comments are not required for normal code.

Do NOT write comments that merely describe obvious code:

```kotlin
// Send message
sendMessage(message)
```

Do NOT comment every function.

Do NOT generate long explanatory comment blocks inside ordinary implementation code.

Prefer code that explains itself through:

- Naming.
- Structure.
- Types.
- Small functions.
- Clear boundaries.

Comments are appropriate only when explaining something non-obvious, especially:

- Why an unusual implementation is required.
- Android/BLE platform behavior.
- OS/device-specific workarounds.
- Security decisions.
- Complex routing algorithms.
- Important lifecycle constraints.
- Architectural trade-offs.

**Comments should primarily explain WHY, not WHAT.**

---

# 35. Senior Developer Rule

Before writing code, determine:

1. Which layer owns the responsibility?
2. What is the source of truth?
3. What abstraction is actually required?
4. What is the lifecycle?
5. What happens when it fails?
6. What happens offline?
7. Does it require persistence?
8. Does it introduce coupling?
9. Can it be tested?
10. Is the abstraction justified?

Think before coding.

---

# 36. No Premature Abstraction

Do not create interfaces/classes just because a pattern suggests them.

Create abstractions when they provide:

- Separation.
- Testability.
- Replaceability.
- Platform isolation.
- Domain boundaries.

Do not abstract for abstraction's sake.

---

# 37. No God Classes

Avoid:

```text
IrysManager
AppManager
NetworkManager
BluetoothManager
DataManager
```

when they contain unrelated responsibilities.

Prefer:

```text
BleScanner
BleAdvertiser
BleConnectionManager
MessageRepository
PeerRepository
RoutingEngine
CryptoManager
```

---

# 38. No God ViewModels

Avoid a single `MainViewModel` for the whole app.

Use:

```text
HomeViewModel
ChatViewModel
NearbyViewModel
EmergencyViewModel
SettingsViewModel
ProfileViewModel
```

Each ViewModel belongs to one feature.

---

# 39. Source of Truth

For persistent local state:

```text
Room
```

should generally be the source of truth.

BLE/network events update local state.

UI observes domain/local state.

Avoid maintaining unnecessary duplicate state in BLE, repositories, ViewModels and UI.

---

# 40. Offline-First

Never make core functionality depend on Internet.

Correct model:

```text
Internet available
    ↓
optional synchronization

Internet unavailable
    ↓
BLE + Room
```

---

# 41. Failure Is Normal

Design for:

```text
BLE unavailable
Peer disappears
Connection drops
Permission revoked
Bluetooth disabled
Message expires
Storage full
Duplicate message
Malformed packet
Authentication failure
Internet unavailable
Backend unavailable
```

These are normal operating conditions for Irys.

---

# 42. Data Validation

Validate at boundaries:

```text
BLE packet
API response
User input
Database mapping
Navigation arguments
```

Do not allow malformed data to propagate into deeper layers.

---

# 43. Concurrency

BLE operations are asynchronous and stateful.

Do not assume:

```text
connect()
write()
disconnect()
```

complete synchronously.

Coordinate GATT operations through a defined mechanism.

Avoid random delays as synchronization.

---

# 44. Resource Management

Release BLE resources deliberately.

Conceptually:

```text
start scan
 ↓
use
 ↓
stop scan
```

and:

```text
connect
 ↓
use
 ↓
disconnect
```

Do not accidentally keep callbacks/connections alive.

---

# 45. UI Quality

UI should be:

- Minimal.
- Modern.
- Consistent.
- Dark-theme friendly.
- Emergency-oriented.
- Accessible.
- Responsive.

Expose user-friendly states:

```text
Connected
Waiting for nearby device
Message queued
Delivered
Expired
```

Do not expose raw technical error codes as the primary UX.

---

# 46. Compose Rules

Prefer:

- State hoisting.
- Immutable UI state.
- Small meaningful composables.
- Reusable components.
- Previews where useful.

Do not extract every tiny piece into a component.

Do not create enormous screen composables.

---

# 47. Navigation

Centralize navigation:

```text
Routes
NavGraph
BottomNavigation
```

Prefer passing stable IDs rather than entire domain objects.

Example:

```text
chat/{conversationId}
```

---

# 48. Testing

Unit-test:

- Routing.
- TTL.
- Hop limits.
- Deduplication.
- Priority.
- Message states.
- Use cases.
- Serialization.
- Repository behavior.

Integration/physical-device testing:

- BLE.
- GATT.
- Advertising.
- Scanning.
- Lifecycle.
- Multi-hop behavior.

---

# 49. Physical BLE Testing

BLE functionality must be tested on real devices.

Minimum topology:

```text
A → B
A → B → C
A → B → C → D
```

Test:

- Screen off.
- App backgrounded.
- Bluetooth disabled/enabled.
- Permission revoked.
- Connection dropped.
- App restart.
- Device restart.
- Duplicate packets.
- Expired messages.
- Storage pressure.
- Battery saver.

---

# 50. Generated Code

Do not generate huge amounts of boilerplate just to make the project look complete.

Implement only what the current feature requires.

Do not create unused:

- Interfaces.
- Repositories.
- Use cases.
- Managers.
- Helpers.
- Models.

Avoid hypothetical architecture.

---

# 51. Feature Workflow

For each feature:

```text
1. Understand requirement
2. Identify domain behavior
3. Define model/interface if needed
4. Define repository boundary
5. Implement data source
6. Implement repository
7. Implement use case
8. Implement UiState
9. Implement ViewModel
10. Implement Compose UI
11. Add navigation
12. Add tests
13. Review lifecycle/error/security behavior
```

---

# 52. BLE Feature Workflow

```text
1. Define behavior
2. Define protocol operation
3. Define connection/state requirements
4. Implement scanner/advertiser/GATT
5. Convert callbacks into app events
6. Implement transport
7. Connect transport to repository
8. Connect repository to use case
9. Connect use case to ViewModel
10. Expose state in Compose
11. Test on physical devices
```

---

# 53. Important Irys Boundary

Never confuse:

```text
BLE
=
Transport

GATT
=
BLE data interface

Irys Protocol
=
Communication protocol

Routing
=
Forwarding decision

Room
=
Persistence

Repository
=
Data abstraction

UseCase
=
Business operation

ViewModel
=
UI state coordination

Compose
=
UI
```

These boundaries must remain clear.

---

# 54. Code Review Checklist

## Architecture

- [ ] Correct layer.
- [ ] Correct package.
- [ ] No dependency violations.
- [ ] No UI-to-data direct access.
- [ ] Domain does not depend on Android.

## Quality

- [ ] Clear naming.
- [ ] Focused classes.
- [ ] No unnecessary abstraction.
- [ ] No duplicated logic.
- [ ] No dead code.

## BLE

- [ ] Permissions handled.
- [ ] Connection state explicit.
- [ ] GATT operations coordinated.
- [ ] Resources released.
- [ ] Incoming packets validated.

## Data

- [ ] Persistence works.
- [ ] Mapping is correct.
- [ ] Offline behavior works.
- [ ] Errors are handled.

## Security

- [ ] No secrets logged.
- [ ] Established crypto only.
- [ ] Incoming input validated.
- [ ] Peer trust is explicit.

## UI

- [ ] UI observes state.
- [ ] No business logic in composables.
- [ ] Loading/error/empty states handled.
- [ ] Navigation centralized.

## Testing

- [ ] Relevant unit tests.
- [ ] Integration tests where needed.
- [ ] Physical BLE tests where needed.

---

# 55. Golden Rule

> **Think first, design the responsibility boundary, then write the smallest amount of production-quality code that correctly solves the problem.**

Do not blindly generate code.

Do not blindly add comments.

Do not blindly introduce abstractions.

Do not explain obvious code through comments.

Do not sacrifice architecture for speed.

When uncertain, reason about the system boundary and data flow before implementation.
