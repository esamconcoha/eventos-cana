/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
     extend: {
      fontFamily: {
        montserrat: ['"Montserrat"', 'sans-serif'],
        opensans: ['"Open Sans"', 'sans-serif'],
        display: ['"Space Grotesk"', 'sans-serif'],
        uimono: ['"JetBrains Mono"', 'monospace'],
        serif: ['"Playfair Display"', 'Georgia', 'serif'],
      },
      colors: {
        grayMedium: '#6B7280',
        brand: {
          950: '#020c1a',
          900: '#021930',
          800: '#052342',
          700: '#0a3459',
          600: '#0f4a7a',
          500: '#1565a8',
        },
      },
      boxShadow: {
        glow: '0 0 0 1px rgba(34,211,238,0.15), 0 8px 24px -8px rgba(34,211,238,0.45)',
        'glow-blue': '0 0 0 1px rgba(37,99,235,0.15), 0 8px 24px -8px rgba(37,99,235,0.5)',
        'glow-amber': '0 0 0 1px rgba(217,119,6,0.12), 0 10px 24px -10px rgba(180,83,9,0.45)',
        card: '0 1px 2px rgba(15,23,42,0.04), 0 12px 32px -14px rgba(15,23,42,0.16)',
        // Sombras del vidrio: un borde luminoso arriba (el reflejo del canto),
        // un borde tenue abajo y una sombra ambiental larga y suave. Es lo que
        // separa un panel de vidrio de un simple rectangulo semitransparente.
        glass: 'inset 0 1px 0 rgba(255,255,255,0.75), inset 0 -1px 0 rgba(2,25,48,0.04), 0 8px 32px -12px rgba(2,25,48,0.22)',
        'glass-lg': 'inset 0 1px 0 rgba(255,255,255,0.85), inset 0 -1px 0 rgba(2,25,48,0.05), 0 24px 64px -20px rgba(2,25,48,0.35)',
        'glass-navy': 'inset 0 1px 0 rgba(255,255,255,0.12), 0 12px 40px -16px rgba(2,12,26,0.6)',
        'inner-glass': 'inset 0 1px 2px rgba(2,25,48,0.07), inset 0 1px 0 rgba(255,255,255,0.6)',
      },
      backdropBlur: {
        xs: '2px',
        '4xl': '72px',
      },
      keyframes: {
        'blob-float': {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(20px, -30px) scale(1.08)' },
        },
        'blob-float-slow': {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(-25px, 25px) scale(1.05)' },
        },
        'fade-in-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-400px 0' },
          '100%': { backgroundPosition: '400px 0' },
        },
        'drawer-in': {
          '0%': { opacity: '0', transform: 'translateX(24px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        'backdrop-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'modal-in': {
          '0%': { opacity: '0', transform: 'scale(0.95) translateY(8px)' },
          '100%': { opacity: '1', transform: 'scale(1) translateY(0)' },
        },
        // El vidrio "se levanta" al aparecer: sube unos px y pierde el desenfoque
        // inicial, como si se enfocara. Es la entrada de tarjetas y paneles.
        'glass-rise': {
          '0%': { opacity: '0', transform: 'translateY(14px) scale(0.985)' },
          '100%': { opacity: '1', transform: 'translateY(0) scale(1)' },
        },
        // Reflejo que cruza los botones principales al pasar el mouse.
        sheen: {
          '0%': { transform: 'translateX(-130%) skewX(-18deg)' },
          '100%': { transform: 'translateX(230%) skewX(-18deg)' },
        },
      },
      animation: {
        'blob-float': 'blob-float 9s ease-in-out infinite',
        'blob-float-slow': 'blob-float-slow 13s ease-in-out infinite',
        'fade-in-up': 'fade-in-up 0.45s ease-out both',
        'fade-in': 'fade-in 0.35s ease-out both',
        shimmer: 'shimmer 1.6s linear infinite',
        'drawer-in': 'drawer-in 0.38s cubic-bezier(0.16,1,0.3,1) both',
        'backdrop-in': 'backdrop-in 0.3s ease-out both',
        'modal-in': 'modal-in 0.32s cubic-bezier(0.16,1,0.3,1) both',
        'glass-rise': 'glass-rise 0.55s cubic-bezier(0.16,1,0.3,1) both',
        sheen: 'sheen 0.9s ease-in-out',
      },
    },
  },
  plugins: [],
}
