# ShopSphere Frontend

React + TypeScript + Vite interface for the ShopSphere product and category APIs.

## Run locally

1. Start the backend and SQL Server from `shopsphere/` with Docker Compose. The API uses host port `8081` so it can run alongside the GDRN backend on port `8080`.
2. In this directory, install dependencies and start Vite:

   ```powershell
   npm install
   npm run dev
   ```

3. Open the local URL printed by Vite (normally `http://localhost:5173`).

Vite proxies `/api` requests to `http://localhost:8081`, so the browser does not need a separate CORS configuration. To use a different backend address, copy `.env.example` to `.env.local` and change `VITE_API_PROXY_TARGET`.

The first screen supports product search, category filtering, stock summaries, and product create, edit, and delete. The Categories screen supports category creation and deletion.
