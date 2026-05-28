module.exports = {
  content: ["./*.html", "./scripts/**/*.js"],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#0d6374',
          dark: '#0a4f5e',
          light: '#e6f4f7',
          muted: '#b3d9e0',
        }
      },
      fontFamily: {
        sans: ['DM Sans', 'sans-serif'],
        mono: ['DM Mono', 'monospace'],
      }
    }
  },
  plugins: [],
}