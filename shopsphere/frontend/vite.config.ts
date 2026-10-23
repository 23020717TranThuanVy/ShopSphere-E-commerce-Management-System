import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    esbuild: { jsx: 'automatic' },
    server: {
      host: '0.0.0.0',
      proxy: { '/api': { target: env.VITE_API_PROXY_TARGET || 'http://localhost:8081', changeOrigin: true } },
    },
  };
});
