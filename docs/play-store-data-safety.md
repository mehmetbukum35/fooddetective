# Play Store Data Safety Notes

Last updated: 4 June 2026

These notes are intended to help complete the Google Play Console Data Safety form for Gıda Dedektörü / Food Detective. Review the final Play Console answers before publishing.

## App permissions and features

The app uses:

- Camera permission: to scan product labels for additive codes.
- Internet permission: to update the additive database and check connection status.
- Network state permission: to understand whether the device is online.
- Gallery image picker: to allow the user to choose a product label image.

## Data handled by the app

### Photos and videos

Camera photos and gallery images are used only for on-device OCR text recognition.

Recommended Play Console interpretation:

- Data collected: No, as long as images are not transmitted off the device.
- Data shared: No.
- Purpose: App functionality.
- User control: The user chooses whether to use camera or gallery.

### App activity / app interactions

The app stores local preferences such as selected language, selected theme, and last successful database sync information.

Recommended Play Console interpretation:

- Data collected off device: No, unless server-side logging is later added for user-level analytics.
- Data shared: No.
- Purpose: App functionality.

### Device or other identifiers

The current app code does not intentionally collect advertising IDs, Android IDs, account IDs, or device identifiers.

Recommended Play Console interpretation:

- Data collected: No.
- Data shared: No.

### Location

The app does not request location permission.

Recommended Play Console interpretation:

- Data collected: No.
- Data shared: No.

### Personal info

The app does not require registration and does not ask for name, email address, phone number, address, or payment information.

Recommended Play Console interpretation:

- Data collected: No.
- Data shared: No.

## Server communication

The app connects to https://ozgurkitap.com/ to update additive data. These API requests are for app functionality. The privacy policy should remain accurate if the server logs standard technical request data.

## Privacy Policy URL

Use this URL in Play Console after publishing the page on the website:

https://ozgurkitap.com/food/privacy-policy

The same URL is referenced inside the Android app strings.

## Store listing disclaimer suggestion

Suggested short disclaimer for the Play Store description:

Gıda Dedektörü / Food Detective provides general information about food additives and E-codes. It does not replace product labels, manufacturer statements, official notices, expert advice, or certification decisions.
