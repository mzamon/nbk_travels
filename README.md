# NBK Travel â€” Android Customer App

Kotlin Android Views application for the NBK Travel shuttle-booking prototype. The interface follows the supplied Design 1 reference: navy and blue headers, white surfaces, golden-yellow calls to action, rounded cards, and NBK artwork.

## Features in this prototype

- White-background branded splash screen and onboarding.
- Customer registration, sign-in, forgot-password entry point, password confirmation and validation.
- Salted PBKDF2 password hashes in the local-only prototype store; passwords are never saved as plaintext.
- Google Credential Manager sign-in flow, with server-side ID-token verification required before it can complete.
- Customer dashboard, booking creation, booking list with status filters, booking details and cancellation.
- Edit profile, change password, notification preference, dark mode preference, language setting information, privacy notice, help FAQs, About and logout.
- Local prototype store so the core customer journey can be demonstrated without a hosted API.
- REST API client that switches to HTTPS mode when `NBK_API_BASE_URL` is supplied.
- Unit validation tests and a GitHub Actions build workflow.

## How to apply these files to your existing Android Studio project

1. Commit or back up your current project first.
2. Extract this source bundle into the **project root** (the folder containing `settings.gradle.kts`). Allow folder contents to merge. The source bundle already includes the drawable resources, and deliberately does not replace your wrapper, top-level build script, `settings.gradle.kts`, or version catalog.
3. The separate `nbk_travels_drawables.zip` is available if you prefer to copy only the assets; it uses the same `app/src/main/res/` paths. Delete the old generated `app/src/main/res/drawable/ic_launcher_background.xml` and `ic_launcher_foreground.xml`, plus old `mipmap-*/ic_launcher_round.*` files if present. The manifest uses the new NBK `ic_launcher` and `ic_launcher_round` resources.
4. In Android Studio, choose **Sync Project with Gradle Files** and then **Build > Make Project**.
5. Run on a physical Android phone with USB debugging enabled. You can use the seeded demo account below to inspect the full signed-in flow.

This project is configured for the AGP 9.x built-in Kotlin setup, which is consistent with the `compileSdk { version = release(36) }` DSL in the original module build file. Do not add the legacy `org.jetbrains.kotlin.android` plugin to an AGP 9.x project.

## Local prototype sign-in

The local mode seeds an example account on first install:

- Email: `demo@nbktravels.co.za`
- Password: `Demo123!`

The example account is for local presentation/testing only. Use your own account details for any real deployment. Clearing app data resets the local prototype database and seeds the example record again.

Local mode persists sample bookings on the device. New registrations are recorded as `pending`, matching the website's verification rule. To make a newly registered local account bookable, it would need admin verification through the connected service; the sample verified account is provided so the mobile flow can be demonstrated without a mobile admin portal.

## Configure hosted API and Google SSO

Add these properties to your **root** `gradle.properties` file, or provide them as Gradle project properties in CI. Do not commit client secrets or any private OAuth credentials.

```properties
NBK_API_BASE_URL=https://your-host.example/api/
NBK_GOOGLE_WEB_CLIENT_ID=your-google-web-oauth-client-id.apps.googleusercontent.com
```

The base URL must use HTTPS. Leaving it blank keeps the app in local prototype mode. Google SSO will not complete until a web OAuth client ID is configured and the API verifies the received Google ID token and creates/returns an NBK application session. See [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md).

The PHP website currently shown in the supplied files uses normal PHP page/session flows, not these REST JSON endpoints. Therefore, do not set `NBK_API_BASE_URL` to the website root until the corresponding API endpoints are implemented and deployed. An API client by itself cannot make those endpoints exist.

## Testing

Run in Android Studio or a terminal in the project root:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The GitHub Actions workflow in `.github/workflows/android.yml` runs unit tests and builds the debug APK on pushes and pull requests. Instrumentation smoke tests live under `app/src/androidTest`; they can be run on a connected test device using `./gradlew connectedDebugAndroidTest`.

## Important prototype limitations

- Online mode needs the API endpoints described in `docs/API_CONTRACT.md`, a secure server-side password hash, bearer-token/session validation, authorization checks, and a real database connection.
- Google SSO needs Google OAuth setup plus server-side token signature, audience, issuer and nonce verification. The Android client must never treat an unverified Google ID token as a logged-in NBK user.
- The language preference can be selected and stored, but the current UI copy remains English.
- The fare is a customer-entered amount because the supplied web prototype asks the customer to enter fare. Production pricing should be calculated server-side.
- The code has been reviewed and packaged, but this environment does not contain an Android SDK/Gradle wrapper runtime to claim a successful local Android build or physical-device test. Run the build/tests above in Android Studio or GitHub Actions before submitting the assessment.
