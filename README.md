# CommuteTrack

A daily commute tracking Android application built with **Clean Architecture**, **Jetpack Compose**, and **Kotlin Multiplatform (KMP)-ready architecture**.

## 🎯 Overview

CommuteTrack helps users track their daily commutes with features including:
- Active trip tracking with real-time timer
- Trip history with detailed session records
- Statistics overview with distance, duration, and transport mode analytics
- Customizable settings (transport modes, distance units, notifications)
- Saved locations (home/work) for quick trip entry
- Dark mode support

## 🏗️ Clean Architecture

This project follows **Uncle Bob's Clean Architecture** principles with strict layer separation:

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │ Feature Modules│  │    Compose UI  │  │  Navigation  │ │
│  │  - Dashboard   │  │  - Screens     │  │  - NavGraph  │ │
│  │  - Tracking    │  │  - Components  │  │  - BottomBar │ │
│  │  - History     │  │  - Theme       │  │              │ │
│  │  - Statistics  │  │                │  │              │ │
│  │  - Settings    │  │                │  │              │ │
│  └────────────────┘  └────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                         Domain Layer                         │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │     Models     │  │   Use Cases    │  │  Repositories│ │
│  │  - Session     │  │  - GetActive   │  │  (Interfaces)│ │
│  │  - Statistics  │  │  - StartTrip   │  │              │ │
│  │  - Settings    │  │  - EndTrip     │  │              │ │
│  │  - Enums       │  │  - GetHistory  │  │              │ │
│  └────────────────┘  └────────────────┘  └──────────────┘ │
│                    Pure Kotlin - No Android dependencies     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          Data Layer                          │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │  Repository    │  │    Data Sources│  │    Mappers   │ │
│  │  Impls         │  │  - Room (DB)   │  │  Entity↔Domain│ │
│  │                │  │  - DataStore   │  │              │ │
│  │                │  │  - Ktor (API)  │  │              │ │
│  └────────────────┘  └────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Key Architecture Principles

1. **Dependency Rule**: Dependencies point inward. Outer layers depend on inner layers, never the reverse.
2. **Domain Independence**: The domain layer has zero Android dependencies - pure Kotlin business logic.
3. **Single Responsibility**: Each use case handles one specific action.
4. **Interface Segregation**: Repository interfaces in domain, implementations in data.
5. **Testability**: Business logic can be tested without Android framework dependencies.

## 📦 Module Structure

```
commuteTrack/
├── app/                          # Application entry point
│   ├── MainActivity.kt          # Hilt entry point
│   ├── CommuteTrackApp.kt       # Application class
│   └── navigation/              # Navigation graph
│
├── core/
│   ├── common/                  # Pure Kotlin utilities
│   │   ├── extensions/          # DateTime, String extensions
│   │   └── Result.kt            # Wrapper type for error handling
│   │
│   ├── domain/                  # Business logic (platform-agnostic)
│   │   ├── model/               # Domain models
│   │   ├── repository/          # Repository interfaces
│   │   └── usecase/             # Use cases (interactors)
│   │
│   ├── data/                    # Data layer implementation
│   │   ├── repository/          # Repository implementations
│   │   ├── mapper/              # DTO ↔ Domain mapping
│   │   └── di/                  # Data DI module
│   │
│   ├── database/                # Local persistence (Room)
│   │   ├── dao/                 # Data Access Objects
│   │   ├── entity/              # Room entities
│   │   └── CommuteDatabase.kt   # Database setup
│   │
│   ├── network/                 # Remote data (Ktor)
│   │   └── api/                 # API clients (future)
│   │
│   └── ui/                      # Shared UI components
│       ├── theme/               # Material3 theme
│       ├── component/           # Reusable composables
│       └── extensions/          # UI-specific extensions
│
└── feature/                     # Feature modules (one per screen)
    ├── dashboard/               # Home screen
    ├── tracking/                # Active trip tracking
    ├── history/                 # Trip history list
    ├── statistics/              # Analytics overview
    └── settings/                # App settings
```

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose, Material3 |
| **Architecture** | Clean Architecture, MVVM |
| **DI** | Hilt |
| **Async** | Kotlin Coroutines + Flow |
| **Database** | Room |
| **Preferences** | DataStore |
| **Networking** | Ktor (KMP-ready) |
| **Navigation** | Jetpack Navigation Compose |
| **Build** | Gradle (Kotlin DSL), Version Catalog |

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17 or later
- Android SDK 24+ (minSdk 24, targetSdk 35)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yudistirosaputro/commuteTrack.git
   cd commuteTrack
   ```

2. **Open in Android Studio**
   - Open the project folder
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Dashboard** | Home screen with active trip summary and quick actions |
| **Tracking** | Start/end trip with location and transport mode input |
| **History** | List of all past trips with filters |
| **Statistics** | Commute analytics (distance, duration, transport usage) |
| **Settings** | App preferences (theme, units, saved locations) |

## 🎨 UI Components

Reusable components in `core:ui`:
- `StatCard` - Display statistics with icon and value
- `TransportModeChip` - Selectable transport mode chip
- `SessionCard` - Trip session display card
- `CommuteBottomBar` - Bottom navigation bar

## 🧪 Testing

The architecture supports testing at every layer:
- **Domain**: Pure Kotlin unit tests (no Android needed)
- **Data**: Repository implementation tests with fake dependencies
- **Presentation**: ViewModel tests with fake use cases
- **UI**: Compose UI tests

## 📦 Dependency Injection

Hilt modules organized by layer:
- `DatabaseModule` - Room database and DAOs
- `DataModule` - Repository bindings and use cases
- Feature modules provide their own ViewModels via `@HiltViewModel`

## 🔄 Data Flow

```
UI (Compose)
  ↓ collects StateFlow
ViewModel
  ↓ invokes UseCase
UseCase (Domain)
  ↓ calls Repository Interface
Repository Implementation (Data)
  ↓ queries DAO/Service
DAO (Room) / DataStore / API
  ↓ returns Entity/DTO
Mapper (Data)
  ↓ converts to Domain Model
UseCase (Domain)
  ↓ returns Result/Flow
ViewModel
  ↓ updates StateFlow
UI (Compose)
  ↓ recomposes with new state
```

## 🎯 Key Features Implementation

### Trip Tracking Flow
1. User enters start location and selects transport mode
2. `StartSessionUseCase` creates new session with `ACTIVE` status
3. `TrackingViewModel` starts timer using `StateFlow`
4. When trip ends, `EndSessionUseCase` calculates duration and updates status
5. Session saved to Room database for history

### Statistics Calculation
- `GetStatisticsUseCase` queries completed sessions from repository
- Calculates totals, averages, and most-used transport mode
- Groups trips by day of week for weekly breakdown
- Returns `CommuteStatistics` domain model

### Settings Persistence
- Uses DataStore for type-safe preference storage
- `UserSettings` domain model stores all preferences
- Settings observed as `Flow<UserSettings>` in ViewModels
- Changes trigger UI updates automatically

## 📄 License

```
Copyright (c) 2026 Yudistiro Saputro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:
1. Follow Clean Architecture principles
2. Write unit tests for new features
3. Keep PRs focused and atomic
4. Follow existing code style

## 📧 Contact

- **Author**: Yudistiro Saputro
- **GitHub**: [@yudistirosaputro](https://github.com/yudistirosaputro)
