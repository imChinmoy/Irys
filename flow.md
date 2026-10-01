# Irys — Application and System Flow Specification

**Document Purpose:** Authoritative definition of user journeys, screen transitions, protocol sequences, and background data flows for Irys.

---

## 1. Application Launch and Lifecycle Flow

```mermaid
flowchart TD
    Launch[App Launch] --> Splash[Splash Screen]
    Splash --> CheckState{Evaluate Local State}
    
    CheckState -->|First Launch / Not Onboarded| Onboarding[Onboarding Flow]
    CheckState -->|Onboarded but Missing BLE Permissions| Permissions[Permissions Flow]
    CheckState -->|Onboarded & Permissions Granted| Home[Home Dashboard]
    
    Onboarding -->|Complete Onboarding| SaveOnboardState[Persist Onboarding Complete]
    SaveOnboardState --> Permissions
    
    Permissions -->|Permissions Granted| Home
    Permissions -->|Permissions Skipped/Denied| HomeWithWarning[Home Dashboard - Degraded State]
```

### 1.1 Splash Screen Sequence
1. **Initialize Core Modules**: Load Hilt graph, initialize Room database (`IrysDatabase`), bind dispatchers.
2. **Read Persistent Flags**: Query `AppSettingDao` for `onboarding_completed` and cryptographic identity existence.
3. **Inspect Runtime Permissions**: Check `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE` (Android 12+) or `ACCESS_FINE_LOCATION` (Android 11 and below).
4. **Route Selection**:
   - `Routes.ONBOARDING` if onboarding flag is false.
   - `Routes.PERMISSIONS` if required permissions are missing.
   - `Routes.HOME` if ready.

---

## 2. Navigation Architecture

```mermaid
flowchart LR
    Splash[Splash] --> Onboarding[Onboarding]
    Splash --> Permissions[Permissions]
    Splash --> MainGraph[Main App Container]
    
    subgraph BottomNav[Top-Level Bottom Navigation]
        Home[Home Dashboard]
        Nearby[Nearby Peers]
        Chats[Chats]
        Emergency[Emergency Hub]
        Settings[Settings]
    end
    
    MainGraph --> BottomNav
    
    Chats --> ChatDetail[Chat Screen /chat/{conversationId}]
    Emergency --> EmergencyDetail[Alert Detail /emergency/{alertId}]
    Home --> Profile[Profile /profile]
```

### 2.1 Route Definitions
- `Routes.SPLASH` (`"splash"`): Entry initialization point.
- `Routes.ONBOARDING` (`"onboarding"`): Offline-first mesh communication explainer and setup.
- `Routes.PERMISSIONS` (`"permissions"`): Explicit Bluetooth & location permission justification and request.
- `Routes.HOME` (`"home"`): High-level operational overview, network health, quick actions.
- `Routes.NEARBY` (`"nearby"`): Active scanner and discovered peer list.
- `Routes.CHATS` (`"chats"`): Offline conversation list and delivery states.
- `Routes.CHAT_DETAIL` (`"chat/{conversationId}"`): Message history and interactive peer messaging.
- `Routes.EMERGENCY` (`"emergency"`): High-priority broadcast center (SOS, alerts).
- `Routes.EMERGENCY_DETAIL` (`"emergency/{alertId}"`): Full emergency advisory inspection.
- `Routes.PROFILE` (`"profile"`): Node identity, public key fingerprint, node alias.
- `Routes.SETTINGS` (`"settings"`): Battery optimization, radio behavior, data cleanup.

---

## 3. Peer Discovery Flow (Phase 3+)

```mermaid
sequenceDiagram
    participant A as Node A (Scanner)
    participant BLE as BLE Radio
    participant B as Node B (Advertiser)
    participant Repo as PeerRepository

    B->>BLE: Start Advertising (Irys Service UUID + Compact Identity)
    A->>BLE: Start Scanning (Filter by Irys Service UUID)
    BLE-->>A: ScanResult(Device, RSSI, ScanRecord)
    A->>Repo: Ingest ScanResult (Filter non-Irys)
    Repo->>Repo: Update/Upsert Peer Record (RSSI, lastSeenTimestamp)
    Repo-->>A: Emit updated Flow<List<Peer>>
```

---

## 4. Connection, Handshake & Synchronization Flow (Phase 4-6)

```mermaid
sequenceDiagram
    participant A as Initiating Peer (GATT Client)
    participant B as Responding Peer (GATT Server)

    A->>B: Connect GATT
    A->>B: Discover Services (Irys Custom Service)
    A->>B: Enable Characteristic Notifications (Message TX / Control)
    
    Note over A,B: Protocol Handshake
    A->>B: HELLO (Version, Client NodeId)
    B->>A: HELLO_ACK (Version, Server NodeId, Nonce)
    
    Note over A,B: Mutual Authentication & Session Key Agreement
    A->>B: AUTH (Signed Token / Key Exchange)
    B->>A: AUTH_ACK (Session Established)
    
    Note over A,B: Message Summary Sync (Store-Carry-Forward)
    A->>B: SYNC_REQUEST (Known Message ID digests)
    B->>A: SYNC_RESPONSE (Required Message IDs)
    
    Note over A,B: Transfer Payloads
    A->>B: MESSAGE (Chunked packets)
    B->>A: ACK (Packet Received)
    B->>A: DELIVERY_ACK (If B is final destination)
```

---

## 5. Message Lifecycle & Store-Carry-Forward Flow (Phase 5-6)

```mermaid
stateDiagram-v2
    [*] --> CREATED: User composes text
    CREATED --> QUEUED: Persisted to Room & added to outbound queue
    QUEUED --> TRANSFERRING: Connected to viable peer
    TRANSFERRING --> STORED: Transfer acknowledged by peer (Hop + 1)
    TRANSFERRING --> FAILED: Connection dropped during transfer (Retry scheduled)
    STORED --> FORWARDED: Forwarded to next intermediate relay
    FORWARDED --> DELIVERED: End destination acknowledges receipt
    QUEUED --> EXPIRED: TTL elapsed without peer encounter
    STORED --> EXPIRED: TTL elapsed while stored on relay
    EXPIRED --> [*]: Purged by CleanupExpiredMessagesUseCase
    DELIVERED --> [*]
```

---

## 6. Emergency Broadcast Flow (Phase 8)

```mermaid
flowchart TD
    UserAlert[User triggers SOS / Broadcast] --> CreateAlert[Create Emergency Alert Entity]
    CreateAlert --> RoomPersist[Persist to Room with CRITICAL Priority]
    RoomPersist --> RadioBroadcast[Trigger High-Frequency BLE Advertising & Priority Relay]
    RadioBroadcast --> PeerEncounter{Peer Encountered?}
    PeerEncounter -->|Yes| ImmediateTransfer[Preempt normal queue & transmit Alert]
    ImmediateTransfer --> RelayForward[Relay node persists & re-broadcasts]
    PeerEncounter -->|No| AwaitPeer[Retain in persistent priority queue]
```
