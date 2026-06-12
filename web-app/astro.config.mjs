import { defineConfig } from 'astro/config';
import sitemap from '@astrojs/sitemap';

export default defineConfig({
  site: 'https://foodlabeldetective.com.tr',
  base: '/app',
  output: 'static',
  integrations: [sitemap()]
});
