# Food Label Detective Web App Site

This is the public static website for the Android app landing pages, privacy policy, support page, and disclaimer pages.

## Stack

- Astro
- Static output
- Turkish and English pages
- Built for deployment under `/app`

## Local development

```bash
cd web-app
npm install
npm run dev
```

## Build

```bash
cd web-app
npm run build
```

Astro writes the static output to:

```text
web-app/dist/
```

Upload the contents of `web-app/dist/` to the server directory that serves:

```text
https://foodlabeldetective.com.tr/app/
```

## Public URLs

```text
https://foodlabeldetective.com.tr/app/
https://foodlabeldetective.com.tr/app/en/
https://foodlabeldetective.com.tr/app/privacy-policy/
https://foodlabeldetective.com.tr/app/en/privacy-policy/
https://foodlabeldetective.com.tr/app/support/
https://foodlabeldetective.com.tr/app/en/support/
https://foodlabeldetective.com.tr/app/terms/
https://foodlabeldetective.com.tr/app/en/terms/
```
