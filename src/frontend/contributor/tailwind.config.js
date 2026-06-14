/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    './index.html',
    './scripts/**/*.js',
  ],
  theme: {
    extend: {
      fontFamily: {
        heading: ['"Plus Jakarta Sans"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['"Courier Prime"', 'monospace'],
      },
      colors: {
        scapes: {
          'light-base': '#f8f8ef',
          'light-primary': '#137586',
          'light-secondary': '#0d6271',
          'light-accent': '#70c3c6',
          'light-highlight': '#f9c52e',
          'dark-base': '#0f0f0f',
          'dark-primary': '#2bb4c1',
          'dark-secondary': '#1fa6b3',
          'dark-accent': '#1a6d75',
          'dark-highlight': '#ffc107',
          base: 'var(--color-base)',
          primary: 'var(--color-primary)',
          secondary: 'var(--color-secondary)',
          accent: 'var(--color-accent)',
          highlight: 'var(--color-highlight)',
        },
      },
      keyframes: {
        'fade-in': {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'scale-in': {
          from: { opacity: '0', transform: 'scale(0.97)' },
          to: { opacity: '1', transform: 'scale(1)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 300ms ease-out both',
        'scale-in': 'scale-in 250ms ease-out both',
      },
    },
  },
  plugins: [],
};
