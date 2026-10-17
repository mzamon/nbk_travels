# NBK Travel JSON API contract (for app integration)

The Android app calls JSON endpoints relative to `NBK_API_BASE_URL`. All remote endpoints must require HTTPS. The server must validate every request, derive the logged-in customer ID from the authenticated session/token, and never trust a `cust_id` supplied by the client.

## Common response format

Success:

```json
{"success":true,"message":"Request completed.","user":{},"token":"server-issued-bearer-token"}
```

Error:

```json
{"success":false,"message":"A user-friendly error message."}
```

Use suitable HTTP status codes (`400`, `401`, `403`, `409`, `422`, and `500`) and never return password hashes in a response.

## Authentication

### `POST auth/register.php`

Request fields: `full_name`, `email`, `phone`, `password`.

The server validates all values, checks unique email, stores a server-side `password_hash()` value (never plaintext), and creates a customer account with status `pending`. Response includes `success` and a verification message.

### `POST auth/login.php`

Request fields: `email`, `password`.

The server verifies the password hash and account status. On success, return `success:true`, `message`, `token`, and a `user` object with `user_id` (or `id`), `full_name` (or `fullName`), `email`, `phone`, `status`, and optionally `notifications_enabled`, `dark_mode`, and `language`. Reject pending/rejected accounts using `success:false` and a helpful message.

### `POST auth/google.php`

Request fields: `id_token`, `nonce`.

The server must verify the Google ID token signature and standard claims (issuer, audience, expiration, and nonce); map the verified identity to an NBK user; apply the account-verification rules; and return the same successful response shape as login. Never create a session from an unverified token.

### `POST auth/forgot-password.php`

Request field: `email`. Return a privacy-preserving response so the API does not expose whether an email address is registered. Actual email delivery and expiring single-use reset tokens must be implemented server-side.

### `POST auth/change-password.php`

Requires `Authorization: Bearer <token>`. Request fields: `current_password`, `new_password`. Revalidate the current password, validate the new password and replace the hash securely.

## Bookings

All booking endpoints require an authenticated customer bearer token.

### `GET bookings/list.php`

Return `{"success":true,"bookings":[...]}`. Each item should include `booking_id`, `pickup`, `dropoff`, `booking_date` (`YYYY-MM-DD`), `booking_time` (`HH:mm` or `HH:mm:ss`), `pax`, `fare`, `status`, and optional `driver_name` and `vehicle_name`. Return only bookings belonging to the authenticated user.

### `POST bookings/create.php`

Request fields: `pickup`, `dropoff`, `booking_date`, `booking_time`, `pax`, `fare`. Revalidate these values server-side, derive `cust_id` from the token, confirm account verification, save the booking with `pending` status, and return the created object under `booking`.

### `POST bookings/cancel.php`

Request field: `booking_id`. Verify the booking belongs to the authenticated user and that its state is cancellable before updating it.

## Profile and settings

All profile endpoints require an authenticated bearer token and must operate on the user identified by that token.

- `POST profile/update.php`: fields `full_name`, `phone`; returns `success` and optionally the updated user.
- `POST profile/settings.php`: fields `notifications_enabled`, `dark_mode`, `language`; returns `success` and a useful message.

## Integration note for the current PHP website

The supplied website files call SQL through PHP pages and use PHP sessions; no JSON API controllers or bearer-token validation layer was included in the pasted tree. These route names are the contract expected by the Android client, not a claim that the routes are already deployed. Implement and secure them on the server before setting `NBK_API_BASE_URL`.
FILE_TREE.txt
nbk_travels/
â”œâ”€â”€ .github/workflows/android.yml
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ build.gradle.kts
â”‚   â””â”€â”€ src/
â”‚       â”œâ”€â”€ main/
â”‚       â”‚   â”œâ”€â”€ AndroidManifest.xml
â”‚       â”‚   â”œâ”€â”€ java/com/example/nbk_travels/
â”‚       â”‚   â”‚   â”œâ”€â”€ MainActivity.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ data/ApiClient.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ data/AppRepository.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ data/Models.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ data/SessionStore.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/NbkTheme.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/UiComponents.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/screens/AuthScreens.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/screens/BookingScreens.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/screens/HomeScreen.kt
â”‚       â”‚   â”‚   â”œâ”€â”€ ui/screens/SettingsScreens.kt
â”‚       â”‚   â”‚   â””â”€â”€ util/InputValidator.kt
â”‚       â”‚   â””â”€â”€ res/
â”‚       â”‚       â”œâ”€â”€ drawable/ (fresh NBK vector and shape resources)
â”‚       â”‚       â”œâ”€â”€ drawable-nodpi/ (NBK logo and hero artwork)
â”‚       â”‚       â”œâ”€â”€ mipmap-*/ (custom launcher assets)
â”‚       â”‚       â””â”€â”€ values/ (colours, strings, themes)
â”‚       â”œâ”€â”€ test/java/com/example/nbk_travels/InputValidatorTest.kt
â”‚       â””â”€â”€ androidTest/java/com/example/nbk_travels/AppSmokeTest.kt
â”œâ”€â”€ README_ANDROID.md
â””â”€â”€ docs/API_CONTRACT.md

The bundle is an overlay/patch for the existing Android Studio project. It intentionally preserves your existing Gradle wrapper, settings.gradle.kts, root build.gradle.kts, and version catalog.
