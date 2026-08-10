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
      },
      animation: {
        'blob-float': 'blob-float 9s ease-in-out infinite',
        'blob-float-slow': 'blob-float-slow 13s ease-in-out infinite',
        'fade-in-up': 'fade-in-up 0.45s ease-out both',
        'fade-in': 'fade-in 0.35s ease-out both',
        shimmer: 'shimmer 1.6s linear infinite',
        'drawer-in': 'drawer-in 0.35s cubic-bezier(0.16,1,0.3,1) both',
        'backdrop-in': 'backdrop-in 0.25s ease-out both',
        'modal-in': 'modal-in 0.25s cubic-bezier(0.16,1,0.3,1) both',
      },
    },
  },
  plugins: [],
}