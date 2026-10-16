# Behgeh — Venue & Event Booking

Android application for booking **venues and events**, built with Kotlin,
Jetpack Compose, and Room.

## Features

- Browse available venues and events
- Reserve a time slot and manage bookings
- See availability per room or date
- Receive reminders for upcoming bookings
- Local-first data, with synchronization when a connection is available

## AI features

The app integrates Gemini AI to:

- answer natural-language questions about availability
- suggest alternative slots
- explain booking rules and policies

## Tech stack

- Kotlin, Jetpack Compose, MVVM
- Room database with coroutines and Flow
- Gemini via the ML Kit / generative AI integration
- Firebase for push notifications (optional)

## Testing

Unit and instrumented tests live in `app/src/test` and
`app/src/androidTest`.
