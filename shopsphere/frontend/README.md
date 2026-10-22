# ShopSphere Frontend

React + TypeScript + Vite interface for the ShopSphere product and category APIs.

The app starts with a sign-in screen. Public registration creates a Customer account; Customers can browse the catalog, while product/category changes are limited to the bootstrapped Admin account. The browser uses the backend's HttpOnly session cookie and fetches a CSRF token for state-changing requests.

## Run locally

1. Start the backend and SQL Server from `shopsphere/` with Docker Compose. The API uses host port `8081` so it can run alongside the GDRN backend on port `8080`.
2. From `shopsphere/frontend/`, install dependencies and start Vite:

   ```powershell
   npm install
   npm run dev
   ```

3. Open the local URL printed by Vite (normally `http://localhost:5173`).

Vite proxies `/api` requests to `http://localhost:8081`, so the browser does not need a separate CORS configuration. To use a different backend address, copy `.env.example` to `.env.local` and change `VITE_API_PROXY_TARGET`.

The first screen supports product search, category filtering, stock summaries, and product create, edit, and delete. The Categories screen supports category creation and deletion.
