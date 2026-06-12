# Food Label Detective Web App Site

This is the public static website for the Android app landing pages, privacy policy, support page, and disclaimer pages.

## Stack

- Astro
- Static output
- English-first public website
- Optional Turkish pages under `/tr`
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

Default English pages:

```text
https://foodlabeldetective.com.tr/app/
https://foodlabeldetective.com.tr/app/privacy-policy/
https://foodlabeldetective.com.tr/app/support/
https://foodlabeldetective.com.tr/app/terms/
```

Optional Turkish pages:

```text
https://foodlabeldetective.com.tr/app/tr/
https://foodlabeldetective.com.tr/app/tr/privacy-policy/
https://foodlabeldetective.com.tr/app/tr/support/
https://foodlabeldetective.com.tr/app/tr/terms/
```

Legacy English duplicate pages may also exist under `/en` until removed intentionally.
