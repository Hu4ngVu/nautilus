# Thiết kế lại UI/UX Nautilus: "Blocky Modern"

- Ngày: 2026-11-13
- Phạm vi: `nautilus-client` (toàn bộ trang) + thay đổi hỗ trợ ở `nautilus-server`
- Trạng thái: chờ review

## 1. Mục tiêu và bối cảnh

Giao diện hiện tại đúng chức năng nhưng đơn điệu: hầu hết component dùng mặc định của Nuxt UI (variant `soft`, neutral), card danh sách phẳng và không có banner, trang Home chỉ có icon, chữ và một ô tìm kiếm. Có hai nguyên nhân chính:

1. Hai ràng buộc thiết kế cũ: font Arial (`client-requirement.md`), và quy tắc "no outlined cards, no gradients, no glow" (`app/app.config.ts`).
2. DTO danh sách server (`ServerInfoResponse`) thiếu banner, platform, tag và version, nên card không có gì để hiển thị.

**Mục tiêu**: một giao diện hiện đại, mang chất game và nhận diện Minecraft rõ ràng, nhất quán trên mọi trang ở cả chế độ sáng và tối, vẫn đạt WCAG AA và giữ hiệu năng tốt.

### Quyết định đã chốt với chủ dự án

| Chủ đề | Quyết định |
|---|---|
| Ràng buộc cũ | Bỏ cả hai: được đổi font, được dùng viền, bóng, hoạ tiết |
| Hướng visual | **Blocky Modern**: góc vuông 4px, viền 2px, bóng lệch đặc màu (neo-brutalism), nền lưới mờ |
| Hiệu ứng "nổi lên" | **A**: nhấc khối khi hover, lún khi bấm. **C**: trồi lên và nảy nhẹ khi vào màn hình (chạy một lần, lần lượt từng phần tử) |
| Font heading | **VT323** (pixel, có tiếng Việt). Pixelify Sans bị loại vì thiếu glyph tiếng Việt |
| Font nội dung | **Be Vietnam Pro** |
| Phạm vi | Toàn bộ trang trong một đợt |
| Backend | Được sửa: mở rộng DTO, siết quyền xem người gắn sao, fix N+1, sửa 2 lỗi ở trang Stars |
| Home | Hero căn giữa + các "kệ" server theo chủ đề |
| Thống kê Dashboard | Chỉ số server (`totalElements`); không có tổng sao hay số server riêng tư |
| Ảnh ở tab Giới thiệu | Tối đa 3 ảnh + link "Xem tất cả" sang tab Thư viện ảnh |
| Người gắn sao | Chỉ owner/editor (theo `schema.fga`) được xem, áp dụng cả backend lẫn client |

Mockup tham chiếu nằm trong `.superpowers/brainstorm/61520-1794500662/content/`: `visual-direction.html`, `lift-effects.html`, `heading-font.html`, `home-layout.html`, `browse-light-dark.html`, `server-detail.html`, `dashboard-profile.html`.

## 2. Hệ thống thiết kế

### 2.1 Font

- Thêm module `@nuxt/fonts`: font được tự host, chỉ lấy subset `latin` + `vietnamese`, preload font chính, không gọi Google lúc runtime.
- `--font-sans: 'Be Vietnam Pro'` (400/500/600) dùng cho toàn bộ nội dung.
- `--font-pixel: 'VT323'` dùng qua class `font-pixel`, cho heading, tên server, nút, tab, chip, con số lớn.
- VT323 có x-height nhỏ và chỉ một độ đậm, nên cần bộ cỡ chữ riêng `text-pixel-{sm,base,lg,xl,2xl,3xl,4xl}`. Mỗi cỡ lớn hơn cỡ Tailwind tương ứng khoảng 1.25 lần, line-height khoảng 1.
- Không dùng VT323 cho đoạn văn, label form hay chữ nhỏ hơn 16px hiển thị thực tế.

### 2.2 Màu

- Primary `nautilus` và secondary `ocean`: giữ nguyên thang màu trong `main.css`.
- Neutral: **Tailwind Gray**.

| Token | Tối | Sáng |
|---|---|---|
| Nền trang (`--ui-bg`) | `gray-950` | `gray-50` |
| Bề mặt card (`--ui-bg-elevated` / block surface) | `gray-900` | `white` |
| `--block-border` | `gray-800` | `gray-900` |
| Lưới nền (`bg-block-grid`) | `white` @ 2.5% | `gray-900` @ 4% |
| Màu chữ primary trên nền (link, version chính) | `nautilus-400` | `nautilus-700` (đạt AA) |
| Nền nút primary | `nautilus-400`, chữ `gray-950` | như bên tối |
| Nút sao | `amber-300`, chữ `amber-950` | như bên tối |

Màu bóng lệch (`--lift-color`): `primary` = `nautilus-500`, `secondary` = `ocean-500`, `ink` = `black` (tối) / `gray-900` (sáng), `warning` = `amber-300`, `error` = `red-500`.

### 2.3 Hình khối

- `--ui-radius: 0.25rem` (4px). Badge và chip dùng 2–3px.
- Viền khối: 2px, màu `--block-border`.
- `--block-offset: 6px` cho card; nút dùng 3px khi nghỉ và 5px khi hover.

### 2.4 Utility (Tailwind v4 `@utility` trong `main.css`)

- `block-surface`: nền bề mặt, viền 2px `--block-border`, `rounded-(--ui-radius)`.
- `block-lift`: `transition: transform .15s cubic-bezier(.3,1.6,.6,1), box-shadow .15s`.
  - Khi hover hoặc `focus-visible`: `translate(-4px,-4px)` và `box-shadow: var(--block-offset) var(--block-offset) 0 var(--lift-color)`.
  - Khi `:active`: `transform: none; box-shadow: none`, với transition .05s.
- `lift-primary | lift-secondary | lift-ink | lift-warning | lift-error`: chỉ đặt `--lift-color`.
- `animate-rise`: keyframes `translateY(24px), opacity 0` → `translateY(-3px)` ở mốc 70% → `none`, thời lượng .6s, `cubic-bezier(.2,.8,.2,1)`, `both`, `animation-delay: calc(var(--i, 0) * 60ms)`. Giới hạn `--i` ở mức 8 để danh sách dài không phải chờ lâu.
- `bg-block-grid`: hai lớp `repeating-linear-gradient` tạo lưới 16px. Áp dụng trên `body`.
- Bên trong `@media (prefers-reduced-motion: reduce)`: `block-lift` chỉ còn đổi bóng, không dịch chuyển; `animate-rise` bị tắt (`animation: none`).

### 2.5 Focus và khả năng truy cập

- Mọi phần tử tương tác có `focus-visible`: outline 2px `ocean-400` cách 2px, kèm hiệu ứng nhấc của `block-lift` nếu có.
- Không truyền đạt thông tin chỉ bằng màu: badge Công khai/Riêng tư có icon, phiên bản chính có cả màu lẫn chữ đậm.
- Tab dùng `UTabs` (Reka UI), hỗ trợ phím mũi tên. Ảnh có `alt`.

### 2.6 Tuỳ biến Nuxt UI (`app/app.config.ts`)

Ghi đè `slots`, `variants` và `compoundVariants` để các trang đang dùng `UButton`, `UInput`… tự đổi sang kiểu khối:

- **Button**: `font-pixel`, viền 2px, `block-lift`. Variant `solid`: bóng nghỉ 3px `lift-ink`. `soft`/`outline`: bề mặt khối. `ghost`/`link`: không có bóng. Giữ `compoundVariants` chữ tối trên nền primary.
- **Input / Select / Textarea**: bề mặt khối. Khi focus thì có bóng 4px `lift-primary`, không dùng ring mặc định.
- **Badge**: góc 2px, chữ cỡ nhỏ (Be Vietnam Pro).
- **Card / PageCard**: `block-surface`.
- **Tabs / NavigationMenu**: label `font-pixel`; mục đang chọn có gạch chân 3px primary (`box-shadow: 0 3px 0`).
- **DropdownMenu / Modal / Slideover / Popover / Toast**: `block-surface` + bóng 6px `lift-ink`.
- **Checkbox / Switch**: ô vuông khối; khi bật thì nền primary có shading (inset).
- **Pagination**: nút khối dùng `font-pixel`.
- **Skeleton**: góc 4px.

Bỏ comment "Design rule: no outlined cards, no gradients, no glow".

## 3. Component

### 3.1 Mới

| Component | Props / slot chính | Mô tả |
|---|---|---|
| `ServerTile.vue` | `server: ServerSummary`, slot `actions`, `index` (cho `--i`) | Card dạng lưới: banner 16:5, icon đè lên banner, tên (`font-pixel`), tác giả, ★, platform + phiên bản chính, tối đa 3 tag. Cả card là một `NuxtLink`; slot `actions` nằm ngoài link. `lift-primary`, hoặc `lift-secondary` khi platform là Bedrock |
| `ServerRow.vue` | `server: ServerSummary`, `index` | Hàng kiểu Modrinth: icon 64–96px, tên + "bởi", summary 2 dòng, badge platform + tag, ★ lớn và phiên bản chính bên phải. Thay cho `ServerCard.vue` |
| `ServerRepoCard.vue` | `username`, `servername`, `avatarUrl`, `summary?`, `starCount`, `platform?`, slot `actions` | Card gọn kiểu repo GitHub, dùng ở Profile và Stars |
| `ServerShelf.vue` | `title`, `filters: ServerQueryParams`, `to` | Tự gọi `servers.list({...filters, size: 8})`. Desktop là lưới 4 cột; dưới `sm` là dải cuộn ngang có snap. Có skeleton khi tải; ẩn cả kệ nếu rỗng hoặc lỗi |
| `PixelShell.vue` | `size` | Logo vỏ ốc pixel art dạng SVG (`shape-rendering="crispEdges"`), dùng màu brand |
| `BlockAvatar.vue` | `name`, `shape: 'square' \| 'banner'` | Ảnh thay thế: khối có shading, màu chọn từ một bảng cố định theo hash của `name`, kèm chữ cái đầu (`font-pixel`). Với banner thì dùng sọc chéo |
| `PageHeader.vue` | `title`, `description?`, slot `actions` | Tiêu đề trang thống nhất |
| `directives/rise.ts` (`v-rise`) | giá trị: index | IntersectionObserver (`threshold: .1`) thêm class `animate-rise` một lần khi phần tử vào màn hình, đặt `--i`. Không hỗ trợ IO thì hiển thị luôn |
| `app/error.vue` | | Chữ 404/lỗi cỡ lớn (`font-pixel`), `PixelShell`, nút về trang chủ, hỗ trợ i18n |

### 3.2 Sửa

- `MediaImage.vue`: khi không có `src` thì render `BlockAvatar`, nhận thêm prop `name`. Đồng thời xử lý `@error` của ảnh, ví dụ URL ký sẵn hết hạn, bằng cách chuyển sang `BlockAvatar`.
- `ServerList.vue`: thêm prop `view: 'list' | 'grid'`. List dùng `ServerRow`, grid dùng `ServerTile` (lưới `sm:2 xl:3`).
- `AppLogo.vue`: `PixelShell` + "Nautilus" (`font-pixel`).
- `EmptyState.vue`: icon nằm trong một khối pixel, nền `block-surface` với viền nét đứt.
- `UserMenu.vue`: avatar vuông góc 3px có viền khối; giữ nguyên các mục menu.
- `StarButton.vue`: kiểu khối màu vàng (`lift-ink`), hiển thị số sao trong một ô con.
- `CopyAddress.vue`: ô địa chỉ dùng `font-pixel`, nút Copy primary; sau khi copy thì chuyển sang icon ✓ trong 1.5s.
- `ServerFilters.vue`, `GalleryGrid.vue`, `ImageUploader.vue`, `MarkdownEditor.vue`, `VersionEditor.vue`, `manage/*`: chỉ restyle, không đổi logic.
- `.markdown` trong `main.css`: heading dùng `font-pixel`, code và blockquote dùng bề mặt khối.

### 3.3 Xoá

- `ServerCard.vue` (thay bằng `ServerRow`).

## 4. Trang

| Trang | Thiết kế |
|---|---|
| **Layout (`app.vue`)** | `UHeader`: `AppLogo`; tab Khám phá / Bảng điều khiển (`font-pixel`, gạch chân 3px); bên phải có `UColorModeButton` + `UserMenu`, hoặc nút Đăng nhập. Mobile dùng drawer. Footer gọn: © + `LanguageSwitcher`, viền trên 2px. `body` có `bg-block-grid` |
| **Home (`index.vue`)** | Hero căn giữa: `PixelShell` lớn, tagline h1 (`font-pixel`, cụm "máy chủ Minecraft" tô primary), ô tìm kiếm với bóng primary và gợi ý phím `/` (phím `/` focus ô tìm kiếm), hàng chip tag dẫn tới `/browse?tags=…`. Bên dưới là 4 `ServerShelf`: Phổ biến nhất (`sort=starCount,desc`), Sinh tồn (`tags=Survival Mode`), Modpack (`serverType=MODPACK`), Bedrock (`serverPlatform=BEDROCK`). Mỗi kệ có "Xem tất cả" dẫn sang `/browse` với cùng bộ lọc. Chip ở hero: `Survival Mode`, `PvP`, `SMP`, `Skyblock`, `Minigames`, `Factions`. Tên tag phải khớp `utils/tags.ts`, còn nhãn hiển thị đi qua i18n |
| **Browse** | Giữ logic hiện tại. Bộ lọc bên trái (desktop), slideover (mobile). Thanh công cụ gồm ô tìm kiếm, sắp xếp, nút chuyển List/Grid (`UFieldGroup` của 2 nút icon, có `aria-pressed`) lưu vào `localStorage` key `nautilus:browse-view` (đọc/ghi trong try/catch, mặc định `list`). Hàng chip "bộ lọc đang áp dụng" bấm ✕ để xoá từng bộ lọc. Hiển thị số kết quả. Danh sách dùng `v-rise` |
| **Server detail** | Banner `aspect-[4/1] max-h-80` có gradient mờ dần vào nền ở đáy. Avatar 96px đè lên banner, bóng primary. Tên (`font-pixel text-pixel-4xl`), "bởi" + link. `StarButton` + nút Quản lý (nếu `canEdit`). Badge platform, type, premium, private. `UTabs`, đồng bộ với `?tab=` qua `router.replace`: **Giới thiệu** (summary + bio markdown + tối đa 3 ảnh + "Xem tất cả (n) →"), **Thư viện ảnh** (toàn bộ ảnh, phân trang, lightbox), **Người gắn sao** (chỉ khi `canEdit`; lưới avatar + username, phân trang). Cột phải sticky: ô **Tham gia** (viền + bóng primary, `CopyAddress`), Phiên bản, Tag. Bỏ ô người gắn sao ở cột phải. Nếu không có `canEdit` thì không gọi API stargazers; nếu `?tab=stargazers` mà không có quyền thì về tab Giới thiệu |
| **Dashboard** | `PageHeader` ("Server của bạn", mô tả, chip "n server", nút "+ Server mới"). Lưới `ServerTile` có badge Công khai/🔒 Riêng tư trên banner, slot actions gồm Xem / Sửa / Xoá (ghost, đỏ). Ô cuối lưới là "+ Tạo server" nét đứt (chỉ ở trang đầu). Giữ modal xoá |
| **Profile (`u/[username]`)** | Cột trái là thẻ khối với bóng secondary: avatar vuông lớn, username (`font-pixel`), số server, bio markdown, nút "Sửa hồ sơ" (nếu là chính chủ). Cột phải là "Server", lưới 2 cột `ServerRepoCard` |
| **Stars** | `PageHeader`. Lưới `ServerRepoCard` có nút ★ để bỏ sao. Khi bấm, card biến mất ngay (optimistic) và hiện toast "Đã bỏ sao · Hoàn tác"; Hoàn tác gọi lại `PUT /stars/…` |
| **Settings** | Menu dọc bên trái (mobile chuyển thành tab ngang): Hồ sơ (avatar, bio), Tài khoản (username, mật khẩu), Giao diện & ngôn ngữ (chế độ sáng/tối dạng 3 thẻ Sáng/Tối/Hệ thống, ngôn ngữ). Mỗi nhóm là `block-surface`. Vùng nguy hiểm dùng `lift-error` và viền đỏ (chỉ khi đã có hành động nguy hiểm) |
| **Login / Register** | Chia đôi trên `lg`: panel trái gồm `PixelShell`, tagline, lưới (ẩn dưới `lg`); panel phải là form trong card có bóng primary. Nút Google dạng khối. Register thêm: kiểm tra username/email còn trống khi gõ (debounce 400ms, `/auth/check-username`, `/auth/check-email`) với icon ✓/✕ và chữ; thanh độ mạnh mật khẩu 4 ô pixel (độ dài ≥ 8, chữ hoa/thường, số, ký tự đặc biệt). Kết quả kiểm tra chỉ để gợi ý, validate cuối cùng vẫn do server |
| **New server** | Form một cột. Platform, type và visibility chọn bằng **thẻ khối** (`URadioGroup` variant card, có icon + mô tả) |
| **Manage** | Menu dọc: Chung, Tag, Phiên bản, Thư viện ảnh, Nâng cao. Các component `manage/*` được restyle. Nếu có thay đổi chưa lưu thì một thanh "Lưu / Huỷ" dính ở đáy màn hình. Nâng cao (đổi tên, visibility, xoá) nằm trong vùng nguy hiểm màu đỏ |
| **Auth callback** | Spinner khối (3 ô vuông nhấp nháy) + chữ |

Mọi trang có danh sách đều có skeleton khi tải, `EmptyState` khi rỗng, và `EmptyState` + nút Thử lại khi lỗi (giữ nguyên hành vi hiện tại).

## 5. Backend

### 5.1 Mở rộng `ServerInfoResponse`

```java
public record ServerInfoResponse(
        String username, String servername, Boolean requiresPremium,
        String avatarUrl, String bannerUrl, String summary, Integer starCount,
        Boolean visibility, ServerType serverType, ServerPlatform serverPlatform,
        List<String> tags,          // tối đa 3, sắp theo createdAt asc
        String primaryVersion) {}   // theo platform; null nếu chưa đặt
```

- `bannerUrl`: `resourceStorage.createSignedGetUrl(server.getServerBannerId())`, xử lý id null giống `avatarUrl`.
- `primaryVersion`: nếu `platform == JAVA` thì lấy `JavaMcVersion` có `isPrimaryMcVersion = true`; nếu `BEDROCK` thì lấy `BedrockMcVersion` tương ứng.

### 5.2 Fix N+1 trong danh sách server

Gộp phần map đang lặp ở `ServerService.findServers` và `getServerByUser` thành `private Page<ServerInfoResponse> toInfoPage(Page<Server> page)`:

1. `ids = page.map(Server::getId)`, `ownerIds = …getUserId` (bỏ trùng).
2. `AuthUserQuery.getUsernamesByAuthUserIds(ownerIds) → Map<String,String>`: **1 query** (projection `id, username` trong `AuthUserRepository`).
3. `TagRepository.findAllByServerIdInOrderByCreatedAtAsc(ids)` (`ServerTag.server` là quan hệ, nên property path là `server.id`): **1 query**, gom thành `Map<serverId, List<String>>` rồi `limit(3)`.
4. `JavaMcVersionRepository.findAllByServerIdInAndIsPrimaryMcVersionTrue(ids)` và tương tự cho Bedrock: **tối đa 2 query**, chỉ gọi khi trang có server thuộc platform đó.
5. Map từng server. Nếu chủ server không tồn tại thì ném `UserNotFound` như hiện tại.

Kết quả: mỗi trang khoảng 1 (page) + 1 (count) + 4 query, không phụ thuộc `size`. Trang rỗng không gọi các bước 2–4.

### 5.3 Fix N+1 và 2 lỗi trong `StarService`

- `getUserStaredInfo(serverId)`: lấy theo lô qua `UserQuery.getUserDataByAuthUserIds(Collection<String>) → Map<String, UserData>`.
- `getServerStaredInfo(userId)`:
  - **Lỗi 1, sai `username`**: response hiện trả username của người gắn sao. Sửa: thêm `ownerUserId` vào `ServerData`, rồi lấy username của **chủ server** theo lô qua `AuthUserQuery.getUsernamesByAuthUserIds`.
  - **Lỗi 2, server riêng tư làm hỏng cả trang**: lọc trước khi phân trang, theo 3 bước để giữ ranh giới Spring Modulith (module `star` không được JPQL join sang entity `Server`): `StarRepository.findServerIdsByUserId(userId)` → `ServerQuery.filterPublicServerIds(ids)` → `StarRepository.findAllByUserIdAndServerIdIn(userId, publicIds, pageable)`. Nhờ vậy phân trang và `totalElements` đúng mà không đổi schema. Nếu một server bị chuyển sang riêng tư giữa các bước thì bỏ qua phần tử đó.
  - Dữ liệu server được lấy theo lô qua `ServerQuery.getPublicServerDataByServerIds(Collection<String>) → Map<String, ServerData>`.
  - Bỏ lời gọi `getUserData(userId)` đang nằm trong vòng lặp.

### 5.4 Quyền xem người gắn sao

`StarController` `GET /stars/{username}/{servername}`: đổi `@permissionCheckerImpl.isViewer(...)` thành `@permissionCheckerImpl.isEditor(...)` (đã có sẵn trong `PermissionChecker`; `editor = editor_member or owner` trong `schema.fga`).

### 5.5 Tài liệu

Cập nhật bảng API trong `client-requirement.md`: các trường mới của danh sách server, quyền mới của `GET /stars/{username}/{servername}`, và ý nghĩa `username` = chủ server trong `GET /stars/{username}`.

## 6. Client: dữ liệu và i18n

- `types/api.ts`: thêm vào `ServerSummary` các trường `bannerUrl`, `visibility`, `serverType`, `serverPlatform`, `tags`, `primaryVersion`.
- i18n: mọi chuỗi mới đều có ở cả `i18n/locales/vi.json` và `en.json`, gồm kệ ở Home, chip tag, tab và "Xem tất cả", nút chuyển List/Grid, bộ lọc đang áp dụng, Công khai/Riêng tư, toast bỏ sao/hoàn tác, độ mạnh mật khẩu, trạng thái kiểm tra username/email, 404/lỗi, các thẻ chế độ sáng/tối. Không hard-code chữ trong component.
- `.gitignore`: thêm `.superpowers/`.

## 7. Kiểm thử và xác minh

### Backend (JUnit + Mockito, theo các test hiện có)

- `ServerServiceTest`:
  - Kết quả danh sách có đủ các trường mới.
  - Server không có tag hoặc chưa có primary version → `tags = []`, `primaryVersion = null`.
  - Chỉ trả tối đa 3 tag.
  - `getUsernamesByAuthUserIds`, `findAllByServerIdInOrderByCreatedAtAsc`, `findAllByServerIdInAndIsPrimaryMcVersionTrue` đều được gọi `times(1)` cho một trang nhiều server.
  - Trang rỗng không gọi các query lấy theo lô.
- `StarServiceTest`:
  - `username` trả về là của chủ server, không phải của người gắn sao.
  - Repository lọc server công khai được gọi (server riêng tư không làm hỏng danh sách).
  - Dữ liệu được lấy theo lô (`times(1)`).
- `StarController` (test controller hiện có): editor/owner → 200, viewer → 403.
- Chạy toàn bộ `./gradlew test` và phải xanh.

### Frontend

Không thêm test runner trong đợt này. Yêu cầu xác minh:

1. `pnpm lint`, `pnpm typecheck`, `pnpm build` đều sạch.
2. Chạy app với backend thật, chụp màn hình mọi trang ở 375px và 1280px, sáng/tối, vi/en, so với mockup.
3. Bàn phím: tab qua header, card, tab ở detail, form; focus ring luôn nhìn thấy được; phím `/` focus ô tìm kiếm ở Home.
4. Độ tương phản AA cho các cặp: chữ thường/muted trên nền và bề mặt ở cả hai chế độ, chữ trên nút primary, chữ primary trong chế độ sáng.
5. Bật giả lập `prefers-reduced-motion` → không còn dịch chuyển hay trồi lên.
6. Kiểm tra trang Stars: sao một server của user khác → link và nút bỏ sao hoạt động; chuyển server đó sang riêng tư → trang vẫn tải được.

## 8. Ngoài phạm vi

- Thêm test runner frontend (Vitest/Playwright).
- Endpoint thống kê cho Dashboard (tổng sao, số server riêng tư).
- Hiệu ứng lơ lửng (B) và nghiêng 3D (D).
- Kệ "Mới nhất" / sắp xếp theo ngày tạo (có thể làm sau qua `sort=createdAt,desc`, nhưng cần thêm lựa chọn vào bộ lọc và i18n).

## 9. Rủi ro

- **VT323 ở cỡ nhỏ khó đọc**: đã giới hạn chỉ dùng cho heading và các phần tử ngắn, có thang cỡ riêng.
- **Signed URL của banner tăng chi phí tạo URL mỗi trang**: mỗi card thêm một lần ký URL (thao tác cục bộ, không gọi mạng nếu dùng presigner của SDK). Cần xác nhận khi làm.
- **Ghi đè theme Nuxt UI rộng có thể ảnh hưởng component ít dùng**: phải xác minh trực quan từng trang, kể cả `manage/*`.
- **Thay đổi hợp đồng `GET /stars/{username}` (`username` đổi nghĩa)**: hiện chỉ client này dùng, và client vốn đã hiểu theo nghĩa đúng.
