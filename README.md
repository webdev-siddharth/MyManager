# MyManager

An all-in-one Android business management app for small business owners and freelancers.

## Features

- **Product Catalog** — Organize products into categories with image upload
- **Client Management** — Store client contacts and view transaction history
- **Order Tracking** — Create orders, track payments, generate PDF invoices
- **Shopping Cart** — Add products to cart, checkout, share summaries as PDF
- **Dashboard** — Revenue stats, pending balances, order counts
- **Business Profile** — Business card generation, company info
- **Cloud Sync** — Full Firestore sync with offline-first Room database
- **Multi-currency** — 30 currencies with locale-aware formatting
- **Theme** — Light/dark mode with system detection

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose (Material 3) |
| Local DB | Room 2.7.1 |
| Remote DB | Firebase Firestore |
| Auth | Firebase Auth + Google Sign-In |
| Image Loading | Coil 3 |
| Image Upload | Cloudinary |
| Architecture | MVVM (ViewModel + StateFlow + Repository) |

## Setup

1. Clone the repository
2. Add your `google-services.json` to `app/` directory
3. Create `local.properties` with:
   ```
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_UPLOAD_PRESET=your_upload_preset
   ```
4. Open in Android Studio and sync Gradle
5. Run on device or emulator (min SDK 26)

## Cloud Functions

The `functions/` directory contains Firebase Cloud Functions for Cloudinary image cleanup on product deletion.

```bash
cd functions
npm install
firebase deploy
```

## Build

Debug build:

```bash
./gradlew assembleDebug
```

Release build (signed with `app/mymanager-release.jks`):

```bash
# Windows PowerShell
$env:KEYSTORE_PASSWORD = "<keystore password>"
./gradlew assembleRelease
```

The build fails fast with a clear error if `KEYSTORE_PASSWORD` is not set, or if
`CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_UPLOAD_PRESET` are missing from
`local.properties` when building a release.

## License

This project is licensed under the [All Rights Reserved License](LICENSE).
