# Man's Best Friend - NeoForge 1.21.1 Minecraft Mod

A Minecraft mod that introduces the Friendship Collar, Ocarina, and Pet Roster system to protect, summon, and manage tamed pets.

## Project Info

- **Mod ID**: `mans_best_friend`
- **Package**: `com.example.mansbestfriend`
- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.x
- **Java**: 21

## Building

```bash
./gradlew build
```

Output JAR will be in `build/libs/`.

## Running

```bash
./gradlew runClient  # Run client
./gradlew runServer  # Run server
```

## Project Structure

```
src/main/java/com/example/mansbestfriend/
├── MansBestFriend.java           # Main mod class
├── attachment/                   # Entity/player data attachments
│   ├── ModAttachments.java       # Attachment registration
│   ├── ForeverPetData.java       # Pet data (forever status, home, owner)
│   └── PlayerPetRosterData.java  # Player's pet roster
├── component/                    # Item data components
│   ├── ModDataComponents.java    # Component registration
│   ├── CollarData.java           # Collar home location
│   └── OcarinaData.java          # Bound pet UUID
├── item/                         # Custom items
│   ├── ModItems.java             # Item + creative tab registration
│   ├── CollarItem.java           # Friendship Collar
│   ├── OcarinaItem.java          # Ocarina
│   └── RosterItem.java           # Pet Roster book
├── entity/ai/goal/
│   └── WanderAroundHomeGoal.java # AI for pets to wander near home
├── event/
│   ├── ModGameEvents.java        # Damage prevention, AI injection
│   └── ModClientEvents.java      # Keybinds (P for roster)
├── network/
│   ├── ModPayloads.java          # Packet registration + handlers
│   └── payload/                  # Network packets
│       ├── SummonPetPayload.java
│       ├── SendPetHomePayload.java
│       ├── SetDefaultHomePayload.java
│       ├── SyncPetRosterPayload.java
│       └── OpenRosterPayload.java
├── screen/
│   └── RosterScreen.java         # Pet roster GUI
└── util/
    ├── HomeLocation.java         # Dimension + BlockPos record
    └── TeleportHelper.java       # Cross-dimension teleportation
```

## Key Features

### Collar
- Right-click tamed pet to make it a "Forever Pet" (invulnerable)
- Sneak+Right-click block to set home location on collar before applying
- Forever Pets wander around their home instead of sitting

### Ocarina
- Sneak+Right-click Forever Pet to bind
- Right-click to summon bound pet (works across dimensions)
- Sneak+Right-click air to send bound pet home

### Roster
- Right-click to open pet management GUI
- Sneak+Right-click block to set default home for all pets
- Press P (keybind) to open GUI without item
- View all pets, summon them, or send them home

## Technical Notes

### Data Attachments
- `ForeverPetData`: Attached to TamableAnimal entities
- `PlayerPetRosterData`: Attached to players (uses `copyOnDeath()`)

### Data Components
- `CollarData`: Stores home location on collar items
- `OcarinaData`: Stores bound pet UUID

### Event Buses
- MOD bus: Registration events, keybinds
- GAME bus (NeoForge.EVENT_BUS): Damage prevention, entity join, player events

### Cross-Dimension Teleport
Uses `Entity.changeDimension(DimensionTransition)` with attachment data preservation.

## Textures Needed

The following textures need to be added to `src/main/resources/assets/mans_best_friend/textures/item/`:
- `collar.png` (16x16)
- `roster.png` (16x16)
- `ocarina.png` (16x16)

## Testing Checklist

1. Craft collar, set home, apply to tamed wolf
2. Verify wolf is invulnerable (except void/creative)
3. Verify wolf wanders near home
4. Craft ocarina, bind to wolf, summon and send home
5. Test cross-dimension teleport (Nether)
6. Press P to open roster, test summon/send home buttons
7. Test roster item sneak+click to set default home
