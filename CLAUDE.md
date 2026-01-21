# Man's Best Friend - NeoForge 1.21.1 Minecraft Mod

A Minecraft mod that introduces the Friendship Collar, Ocarina, and Pet Roster system to protect, summon, and manage tamed pets.

## Project Info

- **Mod ID**: `mans_best_friend`
- **Package**: `com.example.mansbestfriend`
- **Minecraft**: 1.21.11
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
├── attachment/        # Entity/player data attachments (ForeverPetData, PlayerPetRosterData)
├── component/         # Item data components (CollarData, OcarinaData)
├── item/              # Custom items (CollarItem, OcarinaItem, RosterItem)
├── entity/ai/goal/    # Custom AI goals for Forever Pets
├── event/             # Event handlers (damage prevention, AI injection, keybinds)
├── network/           # Network packets and payload registration
├── screen/            # Client-side GUI screens
└── util/              # Helper utilities (teleportation, dimension handling)
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

## Testing Checklist

1. Craft collar, set home, apply to tamed wolf
2. Verify wolf is invulnerable (except void/creative)
3. Verify wolf wanders near home
4. Craft ocarina, bind to wolf, summon and send home
5. Test cross-dimension teleport (Nether)
6. Press P to open roster, test summon/send home buttons
7. Test roster item sneak+click to set default home
