/** @type {import('tailwindcss').Config} */
module.exports = {
  // Memindai seluruh file HTML dan JS di direktori root untuk mengompilasi class yang digunakan
  content: ["./*.html", "./*.js"],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Playfair Display"', 'serif'],
        sans:    ['"DM Sans"', 'sans-serif'],
      },
      colors: {
        surface: '#F0EFEC', 
        panel:   '#FAFAF8',
        border:  '#E0DDD8', 
        muted:   '#B0ADA8',
        ink:     '#1A1916', 
        accent:  '#2D2926',
      },
    },
  },
  plugins: [],
}