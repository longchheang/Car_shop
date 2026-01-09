# Car Shop - Android Application

A modern Android car dealership application built with Kotlin and Jetpack Compose. The app provides a platform for users to browse cars, save favorites, and send inquiries, while admins can manage inventory and respond to customer messages.

## Features

### User Features
- **Browse Cars** - View all available cars with real-time updates
- **Search & Filter** - Search by name, brand, or model; filter by year
- **Car Details** - View complete specifications including mileage, fuel type, transmission, and body type
- **Favorites** - Save cars locally for quick access
- **Inquiries** - Send messages to admin about specific cars and receive replies
- **Shop Location** - Open shop location in Google Maps

### Admin Features
- **Dashboard** - View statistics (total cars, users, available cars)
- **Car Management** - Add, edit, delete cars with image upload
- **Availability Toggle** - Mark cars as sold or available
- **Inquiry Management** - View and reply to customer inquiries
- **Location Setting** - Set shop location via interactive map

### Authentication
- Email/password authentication
- Role-based access (User/Admin)
- Session persistence

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt |
| Backend | Firebase (Auth, Firestore, Storage) |
| Maps | Google Maps SDK |
| Local Storage | DataStore Preferences |
| Image Loading | Coil |
| Min SDK | 24 |
| Target SDK | 36 |

## Project Structure

```
com.example.car_shop/
├── core/                          # Core infrastructure
│   ├── di/                        # Hilt dependency injection modules
│   ├── local/                     # Local data storage (DataStore)
│   ├── model/                     # Data models (Car, User, Inquiry)
│   ├── navigation/                # Navigation routes and configuration
│   └── repository/                # Data repositories
│
├── feature/                       # Feature modules
│   ├── admin/                     # Admin-only features
│   │   ├── carForm/              # Add/Edit car form
│   │   ├── dashboard/            # Admin dashboard with stats
│   │   ├── inquiries/            # Manage user inquiries
│   │   ├── main/                 # Admin main container
│   │   └── mapPicker/            # Shop location picker
│   ├── auth/                      # Authentication
│   │   ├── login/                # Login screen
│   │   └── register/             # Registration screen
│   ├── onboarding/               # Welcome screen
│   ├── profile/                  # User/Admin profile
│   ├── splash/                   # App entry point
│   └── user/                     # User-only features
│       ├── detail/               # Car detail view
│       ├── favorites/            # Favorite cars
│       ├── inquiries/            # User inquiries
│       ├── list/                 # Car listing
│       └── main/                 # User main container
│
└── shared/                        # Shared resources
    ├── components/               # Reusable UI components
    ├── theme/                    # App theme (colors, typography)
    └── utils/                    # Utility functions
```

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with **Clean Architecture** principles:

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│              (Composables / Screens)                    │
│                         │                               │
│                         ▼                               │
│                    ViewModels                           │
│              (StateFlow / UiState)                      │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                           │
│                   Repositories                          │
│     (AuthRepository, CarRepository, InquiryRepository)  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  Data Sources                           │
│        Firebase (Auth, Firestore, Storage)              │
│              DataStore Preferences                      │
└─────────────────────────────────────────────────────────┘
```

## Data Models

### Car
```kotlin
data class Car(
    val id: String,
    val name: String,
    val brand: String,
    val model: String,
    val year: Int,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val bodyType: String,
    val isAvailable: Boolean,
    val createdAt: Timestamp
)
```

### User
```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val isAdmin: Boolean,
    val profileImageUrl: String,
    val createdAt: Timestamp
)
```

### Inquiry
```kotlin
data class Inquiry(
    val id: String,
    val carId: String,
    val carName: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val message: String,
    val reply: String,
    val status: InquiryStatus,  // PENDING, REPLIED, CLOSED
    val userHasRead: Boolean,
    val adminHasRead: Boolean,
    val hiddenByUser: Boolean,
    val createdAt: Timestamp,
    val repliedAt: Timestamp
)
```

## Firebase Structure

### Firestore Collections
```
├── users/
│   └── {userId}
│       ├── id, email, name, phone
│       ├── isAdmin, profileImageUrl
│       └── createdAt
│
├── cars/
│   └── {carId}
│       ├── id, name, brand, model, year
│       ├── price, description, imageUrl
│       ├── mileage, fuelType, transmission, bodyType
│       ├── isAvailable
│       └── createdAt
│
├── inquiries/
│   └── {inquiryId}
│       ├── carId, carName, userId, userName
│       ├── userEmail, userPhone, message, reply
│       ├── status, userHasRead, adminHasRead
│       ├── hiddenByUser
│       └── createdAt, repliedAt
│
└── settings/
    └── shopLocation
        └── location: "latitude,longitude"
```

### Firebase Storage
```
├── profile_images/
│   └── {userId}/
│       └── {timestamp}.jpg
│
└── car_images/
    └── {carId}
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Firebase project

### Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)

2. Enable the following services:
   - Authentication (Email/Password)
   - Cloud Firestore
   - Storage

3. Download `google-services.json` and place it in the `app/` directory

4. Set up Firestore security rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
    match /cars/{carId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    match /inquiries/{inquiryId} {
      allow read, write: if request.auth != null;
    }
    match /settings/{document} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Google Maps Setup

1. Get an API key from [Google Cloud Console](https://console.cloud.google.com)

2. Enable Maps SDK for Android

3. Add the API key to `local.properties`:
```
MAPS_API_KEY=your_api_key_here
```

### Build & Run

1. Clone the repository
```bash
git clone <repository-url>
cd Car_shop
```

2. Open in Android Studio

3. Sync Gradle files

4. Run on emulator or device

## Creating an Admin User

To create an admin user, manually update the `isAdmin` field in Firestore:

1. Register a new user through the app
2. Go to Firebase Console > Firestore
3. Find the user document in the `users` collection
4. Change `isAdmin` from `false` to `true`
5. Log out and log back in

## App Navigation

```
Splash Screen
    │
    ├── Not logged in ──► Onboarding ──► Login ◄──► Register
    │                                      │
    │                                      ▼
    ├── User logged in ──────────────► User Main
    │                                      ├── Home (Car List)
    │                                      ├── Favorites
    │                                      └── Profile
    │
    └── Admin logged in ─────────────► Admin Main
                                           ├── Dashboard
                                           ├── Inquiries
                                           └── Profile
```

## Screenshots

| User Home | Car Detail | Admin Dashboard |
|-----------|------------|-----------------|
| Browse cars with search and filter | View full car specifications | Manage inventory with stats |

| Favorites | Inquiries | Map Picker |
|-----------|-----------|------------|
| Saved cars list | Message history | Set shop location |

## Dependencies

## License

This project is for educational purposes.
