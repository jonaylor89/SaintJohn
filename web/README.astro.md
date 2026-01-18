# Saint John Website - Astro

This is the Saint John launcher website, converted to Astro for optimal performance and maintainability.

## Performance Optimizations

- **Static Site Generation (SSG)**: Zero JavaScript by default, all pages pre-rendered at build time
- **Minimal JavaScript Hydration**: Only essential interactivity (salvation mode toggle, scroll animations) uses JavaScript
- **Inline Critical Scripts**: JavaScript is inlined in the HTML for faster initial load
- **CSS as Static Asset**: CSS served as a single static file for optimal caching
- **Compressed HTML**: HTML minification enabled
- **No Code Splitting**: Single CSS bundle for optimal caching on small sites

## Project Structure

```
/
├── public/              # Static assets (served as-is)
│   ├── assets/          # Images (screenshots, Byzantine art)
│   ├── style.css        # Global styles
│   └── ...             # Favicons, manifest
├── src/
│   ├── layouts/
│   │   └── BaseLayout.astro  # Base HTML structure with header/footer
│   └── pages/
│       ├── index.astro       # Homepage
│       └── privacy.astro     # Privacy policy
├── astro.config.mjs     # Astro configuration
└── package.json         # Dependencies
```

## Commands

| Command                | Action                                       |
| :--------------------- | :------------------------------------------- |
| `npm install`          | Install dependencies                         |
| `npm run dev`          | Start dev server at `localhost:4321`         |
| `npm run build`        | Build production site to `./dist/`           |
| `npm run preview`      | Preview build locally before deploying       |

## Development

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

Visit `http://localhost:4321` to see the site.

## Building for Production

```bash
# Build the site
npm run build

# The output will be in ./dist/
# Deploy the contents of ./dist/ to your web server
```

## Design Philosophy

This conversion maintains 100% design fidelity with the original HTML/CSS/JS website while adding:

1. **Zero Runtime Overhead**: Astro components compile to static HTML
2. **Optimal Asset Loading**: All assets are properly optimized and cached
3. **Type Safety**: TypeScript strict mode enabled
4. **Modern Tooling**: Vite-powered dev server with HMR
5. **Build-Time Rendering**: All pages pre-rendered for instant loads

## Features Preserved

- Salvation Mode theme toggle (Byzantine/religious art aesthetic)
- Smooth scroll animations
- Intersection Observer fade-in effects
- LocalStorage persistence for theme preference
- Header shadow on scroll
- All original styling and interactions

## Differences from Original

1. **HTML Files → Astro Components**: `index.html` and `privacy.html` are now `.astro` files
2. **Shared Layout**: Common elements (header, footer, meta tags) extracted to `BaseLayout.astro`
3. **Inline Scripts**: JavaScript moved inline for optimal performance (no separate .js file)
4. **Type Safety**: Added TypeScript for better DX and correctness
5. **Build Process**: Now requires a build step (`npm run build`)

## Performance Metrics

The Astro version should show:
- Faster initial page loads (pre-rendered HTML)
- Better caching (static assets with cache headers)
- Smaller JavaScript bundle (only essential code runs)
- Improved Core Web Vitals scores
