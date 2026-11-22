# نور الصالحي | Noor Al-Salhi

**AI DRIVER INTELLIGENCE — Drive Smarter. Earn Better.**

Android MVP for delivery drivers. This starter currently includes a manual order-profit calculator and configurable operating-cost switches. Screen reading, floating overlay, order history, multiple saved profiles, and any automated order acceptance are not implemented yet.

## Current MVP
- Arabic-first dark interface.
- Manual fare, pickup distance, trip distance, and duration entry.
- Editable commission and fuel cost.
- Independent toggles for fuel, maintenance, tires, vehicle depreciation, and reserve.
- Estimated net profit, profit per km, profit per hour, and order rating.
- Local calculation; no order data is uploaded.

## Build
Requires JDK 17, Android SDK Platform 35, and Gradle 8.9.

```bash
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:testDebugUnitTest
```

GitHub Actions workflow: **Actions → Build Noor Al-Salhi APK → Run workflow**. The generated debug APK is available under the workflow run's Artifacts.

## Important
All amounts and ratings are estimates based on manually entered values. The app does not connect to Baly or Sar and does not read their screens or accept orders automatically. Such features require separate compatibility, permission, and platform-policy review.
