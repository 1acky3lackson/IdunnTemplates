import { intlayer, intlayerCompiler } from 'vite-intlayer'; // Add the plugin to the Vite plugin list
import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import path from "path";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [intlayer(), intlayerCompiler(), tailwindcss(), reactRouter(), tsconfigPaths(),],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './app')
    }
  }
});
