FROM node:24-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm nuxt build

# The Nitro output bundles its own dependencies, so the runtime image needs no node_modules
FROM node:24-alpine
WORKDIR /app
COPY --from=build /app/.output .output
ENV NODE_ENV=production \
    HOST=0.0.0.0 \
    PORT=8082
EXPOSE 8082
USER node
CMD ["node", ".output/server/index.mjs"]
