# NH Tiles & Marble Room Designer
**Package:** `com.nh.tilesmarbledesigner` · **Branding:** NH Developer · **100% offline** (the manifest declares no INTERNET permission)

Kotlin · Jetpack Compose · Material 3 · MVVM + Repository · Room · DataStore · Photo Picker · Android PdfDocument · custom Canvas perspective renderer.

## Build & install
1. Install Android Studio (Koala or newer) with Android SDK 34 and JDK 17 (bundled).
2. `File > Open` and choose this folder. Let Gradle sync (needs internet **once** to download Gradle 8.7 and the dependencies; the app itself never uses the network).
3. If Android Studio asks to create/update the Gradle wrapper, accept. (The wrapper JAR could not be bundled; `gradle/wrapper/gradle-wrapper.properties` pins Gradle 8.7.)
4. Run on a device/emulator (API 26+), or `Build > Build APK(s)`.
   Command line (after the wrapper exists): `./gradlew assembleDebug` -> `app/build/outputs/apk/debug/`.
Release builds are signed with the debug key so they install directly; use your own keystore for Play Store.

## Architecture
| Package | Contents |
|---|---|
| `data` | `Models.kt` (project model + JSON), `Db.kt` (Room entities/DAOs/Repository), `SettingsStore.kt` (DataStore) |
| `engine` | `Calc` (tile/box/slab maths + validation), `Materials` (offline procedural textures), `PatternComposer` (straight/diagonal/brick/herringbone/grid/slab + grout), `RoomRenderer` + `Scene` (perspective mesh renderer, lighting, shadows), `CropEngine` (import, auto-detect, perspective warp), `Exporter` (PNG, PDF, share) |
| `ui` | Theme, components, `AppViewModel`, 18 screens in `Screens1-4.kt` |

## Calculation rules
`Tiles = ceil(Area / TileArea x (1 + wastage/100)) + spare` · `Boxes = ceil(Tiles / TilesPerBox)` · `Extra = Boxes x TilesPerBox - Tiles`.
Check: 10 ft x 10 ft, 1 ft tile, 8 per box, 0 % -> 100 tiles, **13 boxes**, 104 purchased, 4 extra.
Wall area = sum(length x height) minus doors/windows. Irregular rooms = extra rectangular floor sections.

## Notes
* Built-in materials are generated on-device (procedural marble, granite, wood, etc.) so no photo assets are needed; import real photos through the Atomic Crop Tool.
* 3D preview shows the main rectangular room as a cut-away view; extra floor sections count in calculations only.
* Herringbone snaps tile length to a whole multiple of tile width.

## What is new in v2.0.0
* **Doors & windows** are now drawn on the walls in the 3D room (frame, panels, handle, glass, sill). Add them from Wall Measurement (size, count, position along the wall, sill height) or with the quick "+ Door / + Window" buttons in the Wall Design Studio. They are still deducted from wall tile area.
* **5 room templates with furniture**: Living Room, Bedroom, Kitchen, Bathroom, Dining Room. Load one, then change floor/wall tiles, marble, patterns and grout in the studios.
* **Furniture editor**: add, move (left-right / front-back), rotate, remove pieces; show/hide furniture. 20 furniture types built from 3D boxes.
* **Full screen 3D view**: immersive (system bars hidden), drag to rotate, pinch to zoom, auto-rotate, before/after, furniture toggle, tap to hide controls.
* Furniture and openings are saved with projects and included in PNG/PDF exports and thumbnails.

## What is new in v3.0.0
* **3D model export**: Export Design > "3D model" creates `<project>_3D.zip` with `room.obj`, `room.mtl`, PNG textures (floor, walls, ceiling, with doors/windows painted on) and furniture as coloured boxes. Units are metres, Y up. Save it to storage or share it.
* **WhatsApp sharing**: send the 3D design image, the PDF report or the 3D model zip directly to WhatsApp (or WhatsApp Business); if neither is installed the normal share menu opens. The Full Screen 3D view has a WhatsApp button that sends the exact angle you are looking at.

## v3.1.0
* **3D view images**: save a high-resolution (1920x1440) image of the 3D room straight to the phone gallery (Pictures/NH Tiles & Marble) from the 3D Preview, the Full Screen view (current angle) or the Export screen, which also has 5 ready angles (Front, Left corner, Right corner, Top view, Back) and "Save all 5".

## v3.2.0 - premium theme UI
* **New design system**: soft pastel backgrounds, lighter rounded cards, deep-colour buttons/switches/selection rings, large bold titles.
* **7 colour themes** (Settings > Theme): Forest Whisper (default), Lavender Dream, Ocean Waves, Sunset Glow, Rose Petal, Misty Slate and Midnight Gold (dark). The choice is saved and applied to every screen, the status/navigation bars, the 3D stage and the splash.
* **Layout pass on all pages**: consistent headers, content centred with a max width on tablets/landscape, wrapped long labels, safer overlay controls in Full Screen 3D, new Home with hero, stats and a 12-tile grid (Material Library and Export & Share now reachable from Home; Library also from the Design Studio).
* No functions were removed.

## v3.3.0 - layout polish
* One uniform size system: compact type scale (body 14 sp, labels 13 sp, captions 11 sp), 48 dp buttons, 112 dp Home tiles, 20 dp card corners, 16 dp page margins, 10 dp spacing.
* Clearer text: deeper text colour on every theme, high-contrast accent for titles and icons, buttons and tiles wrap text instead of cutting it off, long rows no longer push buttons off screen.
* Smaller swatches, headers and hero so more fits on one screen.

## v3.4.0 - shadows and colour roles
* **Soft shadows** tinted with the active theme on cards, Home tiles, stat pills, hero image, buttons, back button, swatches, 3D stage, project cards, template thumbnails and the bottom bar (neutral black shadows on the dark theme).
* **Text colour roles from the theme palette** instead of one colour: deep accent for titles and tonal-button text, theme ink for body text, a muted theme-tinted colour for labels and sub-text, a strong highlight colour for key numbers and stat values, plus a small accent bar before every section title.
