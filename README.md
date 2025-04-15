# 🎮 MovieVerse - OMDB Movie App

MovieVerse is an Android application that allows users to search and view detailed information about movies using the OMDB API. It also supports favoriting movies and offline support.

---

## 🚀 Features

- Search movies by title using the OMDB API
- View detailed information (cast, rating, genre, plot, etc.)
- Favorite movies and store them locally
- Clean MVVM architecture
- Unit tested ViewModels
- Uses Hilt, Retrofit, Room, LiveData, and Coroutines

---

## 🛀 Tech Stack

- **Kotlin**
- **MVVM Architecture**
- **Hilt** - Dependency Injection
- **Retrofit** - Networking
- **Room** - Local Caching
- **LiveData & StateFlow** - Reactive Programming
- **Coroutines** - Asynchronous operations
- **JUnit + Mockito + MockK** - Unit Testing
- **Glide** - Image loading

---

## 🗖️ Project Structure

```
📁 data/
    ├── Local/
    ├── Remote/
    └── Repository/
📁 presentation/
    ├── HomeScreen/
    ├── FavoritesScreen/
    └── MovieDetailScreen/
📁 utils/
📁 di/
```

---

## 🧑‍💻 Getting Started

### ✅ Prerequisites

- Android Studio Ladybug Feature Drop or higher
- Android SDK 34+
- An OMDB API key

### 🔧 Setup Instructions

1. **Clone the repo**

   ```bash
   git clone [https://github.com/tyagishivamm865/MovieVerse.git]
   cd MovieVerse
   ```

2. **Add your OMDB API Key**

   Open (or create) the `gradle.properties` file in the root directory and add:

   ```properties
   API_KEY=your_api_key_here
   ```

3. **Sync and build the project**

   Open in Android Studio → Let Gradle sync → Click **Run**

---

## 🧚 Running Unit Tests

Instructions To Run the Test Cases : 
Run tests using Android Studio(go to package package com.omdbmovies.movieverse(test) ->open HomeViewModelTest -> click (ctrl+shift+f10) or click on run icon(left side of class HomeViewModelTest)

### ✅ Test Coverage Highlights (HomeViewModelTest):

- ✅ Success when a valid movie is found
- ✅ Error when movie is not found
- ✅ Handles timeout (SocketTimeoutException)
- ✅ Handles no internet (UnknownHostException)
- ✅ Handles rate limiting (429 HTTP error)
- ✅ Handles internal server error (500 HTTP error)
- ✅ Handles general IO exceptions and unexpected runtime exceptions
- ✅ Clears search and resets UI state
- ✅ Skips search for empty queries
- ✅ Debounce logic: cancels previous job if new input comes
- ✅ Cancels previous API request and uses latest input
- ✅ Handles null response body and responds with appropriate error

All test cases are passing and cover edge scenarios to ensure robust ViewModel behavior.

### ✅ Test Coverage Highlights (FavoriteViewModelTest):

- ✅ Adds a movie to favorites and updates LiveData accordingly
- ✅ Removes a movie from favorites and updates LiveData
- ✅ Refreshes favorite list from the repository correctly
- ✅ Checks if a movie is favorited using LiveData binding

These unit tests ensure the `FavoriteViewModel` handles all critical interactions with the repository(data persistence), maintains UI consistency through LiveData. All tests are currently passing.

---

## 🚩 Troubleshooting

- **App crashes on image loading?** → Ensure placeholder image size is under \~500 KB.
- **API limit reached?** → OMDB API has daily rate limits. Switch to another key or upgrade.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

