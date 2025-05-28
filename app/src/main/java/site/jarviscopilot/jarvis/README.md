# MVVM Architecture in Jarvis App

This document outlines the MVVM architecture implementation in the Jarvis app.

## Directory Structure

```
site.jarviscopilot.jarvis/
├── data/               # Model layer
│   ├── model/          # Data classes/models
│   ├── repository/     # Repositories for data access
│   └── source/         # Data sources (local, remote)
├── di/                 # Dependency Injection
├── ui/                 # View layer
│   ├── components/     # Reusable UI components
│   ├── screens/        # Individual screens/views
│   ├── navigation/     # Navigation-related classes
│   └── theme/          # Theme-related classes
├── util/               # Utility classes
└── viewmodel/          # ViewModel layer
```

## MVVM Components

### Model Layer (`data/`)

- Contains data classes, repositories, and data sources
- Repositories abstract the data sources and provide clean APIs for ViewModels
- Data sources handle local storage, network calls, etc.

### View Layer (`ui/`)

- Composable functions that represent UI elements
- Observes ViewModel state and renders accordingly
- Delegates user actions to ViewModel

### ViewModel Layer (`viewmodel/`)

- Holds UI state using StateFlow
- Contains business logic
- Communicates with repositories to get/update data
- Survives configuration changes

## Best Practices

1. Keep Views passive - they should only render state and forward events
2. ViewModels should never reference Views directly
3. Models should be independent of the platform
4. Use dependency injection for better testability
5. Use unidirectional data flow (ViewModel → View)
