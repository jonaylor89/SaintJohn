# SaintJohn Website

Minimalist marketing website for the SaintJohn Android launcher.

## Design Philosophy

The website follows the visual language of your existing properties (jonaylor.com, blog.jonaylor.com, getfuego.dev) with inspiration from Blloc's product-focused approach:

- **Minimalist aesthetic**: Black/white color scheme, generous whitespace
- **Typography-focused**: Clean sans-serif, clear hierarchy
- **Content over decoration**: Let features and screenshots tell the story
- **Purposeful simplicity**: Every element serves a function

## Structure

```
web/
├── index.html          # Main landing page
├── privacy.html        # Privacy policy (required for Play Store)
├── style.css           # All styling
├── script.js           # Smooth scrolling & animations
└── README.md           # This file
```

## Placeholders to Replace

The site uses placeholder elements for visual content. Replace these with actual assets:

### 1. Hero Video (Line 27)
```html
<div class="placeholder-video">
```
Replace with: Looping video or animated GIF of SaintJohn in action (home screen → widgets → chat)

### 2. Feature Screenshots (3 total)
```html
<div class="placeholder-screenshot">
```
Replace with:
- Chat interface showing LLM streaming responses
- Home screen with weather, calendar, notes widgets
- App drawer with organized folders

### 3. Showcase Mockups (3 large device frames)
```html
<div class="placeholder-large">
```
Replace with: Large phone mockups showing:
- Home screen (widgets)
- Chat screen (AI conversation)
- App drawer (organized apps)

## Customization Needed

1. **Play Store Link**: Update `#download` href with actual Play Store URL
2. **Support Email**: Replace `support@example.com` with real contact
3. **Version Number**: Update "Current version: 1.0" in download section
4. **Assets**: Add actual screenshots and product videos

## Deployment

This is a static site. Deploy to:
- GitHub Pages
- Netlify
- Vercel
- Any static hosting service

## Local Testing

Open `index.html` in a browser, or serve locally:

```bash
# Python 3
python -m http.server 8000

# Node.js
npx serve
```

## Brand Consistency

The design maintains consistency with your existing sites:
- Same typography stack (system fonts, Trebuchet MS fallback)
- Minimal color palette (black/white/gray)
- Border-based hover states
- Generous padding and spacing
- Mobile-responsive breakpoints

## Blloc-Inspired Elements

- Three-screen showcase section
- Large device mockups dominating the page
- Minimal text, visual-first presentation
- Philosophy section emphasizing values over features
- Clean, uncluttered layouts with breathing room
