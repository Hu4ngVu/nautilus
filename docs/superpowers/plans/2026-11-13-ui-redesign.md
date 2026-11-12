# Thiết kế lại UI/UX "Blocky Modern": Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Làm lại toàn bộ giao diện `nautilus-client` theo phong cách Blocky Modern, đồng thời mở rộng DTO danh sách server, fix N+1 và sửa 2 lỗi ở trang Stars trong `nautilus-server`.

**Architecture:** Làm backend trước (Task 1–4) để client có dữ liệu thật. Frontend dựng nền trước (font, token, utility, theme Nuxt UI ở Task 5), sau đó tới component dùng chung (Task 6–7), rồi làm lại từng nhóm trang (Task 8–13). Task 14 xác minh toàn bộ trên app thật.

**Tech Stack:** Spring Boot + Spring Modulith + JPA + JUnit5/Mockito (Gradle); Nuxt 4 SPA, Nuxt UI v4, Tailwind v4, @nuxtjs/i18n, Pinia, `@nuxt/fonts` (mới).

**Spec:** `docs/superpowers/specs/2026-11-13-ui-redesign-design.md`

## Global Constraints

- Font: `--font-sans: 'Be Vietnam Pro'` (400/500/600); `--font-pixel: 'VT323'`. Chỉ subset `latin` + `vietnamese`, qua `@nuxt/fonts`.
- VT323 chỉ dùng cho heading, tên server, nút, tab, chip, con số lớn; không dùng cho đoạn văn hay label form.
- Neutral = Tailwind Gray. Tối: nền `gray-950`, bề mặt `gray-900`, `--block-border` `gray-800`. Sáng: nền `gray-50`, bề mặt `white`, `--block-border` `gray-900`.
- Chữ primary trên nền: `nautilus-400` (tối) / `nautilus-700` (sáng). Nút primary: nền `nautilus-400`, chữ `gray-950`.
- `--ui-radius: 0.25rem`; viền khối 2px; `--block-offset: 6px`; bóng nút 3px khi nghỉ, 5px khi hover.
- `block-lift`: hover hoặc focus-visible → `translate(-4px,-4px)` + bóng `--lift-color`; `:active` → `transform:none; box-shadow:none`. Easing `cubic-bezier(.3,1.6,.6,1)` .15s.
- `animate-rise`: .6s `cubic-bezier(.2,.8,.2,1)`, delay `calc(min(var(--i,0),8) * 60ms)`. Chạy một lần.
- `prefers-reduced-motion: reduce` → không dịch chuyển, không trồi lên.
- Mọi chuỗi giao diện mới phải có ở cả `i18n/locales/vi.json` và `en.json`.
- Tên tag phải khớp `app/utils/tags.ts` (vd. `Survival Mode`, `PvP`, `SMP`, `Skyblock`, `Minigames`, `Factions`).
- Backend phải giữ ranh giới module Spring Modulith (`ArchitectureTest` phải xanh): module này chỉ gọi module khác qua interface trong `api/`.
- Mỗi trang danh sách server: tối đa khoảng 4 query cố định, không phụ thuộc `size`.
- WCAG AA cho chữ thường; focus ring luôn nhìn thấy được.
- Mốc hiện tại: `./gradlew test` 89/89 xanh; `pnpm lint` và `pnpm typecheck` sạch. Không được làm giảm mốc này.

## Review Focus

1. **Ảnh thiếu hoặc URL ký sẵn đã hết hạn** (avatar/banner `null` hay trả 403): phải hiện `BlockAvatar`, không có ảnh vỡ hay khung trống. Kiểm ở Task 6, Step 4.
2. **Dữ liệu dài hoặc rỗng**: tên server 50 ký tự có dấu, summary rỗng `""`, 0 tag, `primaryVersion = null`, không có banner. Card/hàng/header không được vỡ layout, không để lại chip rỗng hay dấu "·" thừa. Kiểm ở Task 7, Step 3.
3. **Quyền xem người gắn sao thay đổi bất đồng bộ**: `canEdit` của editor chỉ biết sau khi probe xong; ẩn danh mở `?tab=stargazers`. Tab phải xuất hiện khi quyền được xác nhận; người không có quyền thì về tab Giới thiệu và không gọi API `/stars/{u}/{s}`. Kiểm ở Task 10, Step 3.
4. **Bỏ sao rồi bấm Hoàn tác, hoặc request lỗi**: bỏ sao lỗi thì card hiện lại kèm toast lỗi; Hoàn tác thành công thì card về đúng chỗ; Hoàn tác lỗi thì báo lỗi, không nhân đôi card. Kiểm ở Task 11, Step 4.
5. **`localStorage` bị chặn** (private mode): Browse vẫn render ở chế độ `list`, không lỗi console. Kiểm ở Task 9, Step 3.

---

## Phần A: Backend

### Task 1: Batch query adapters giữa các module

**Files:**
- Modify: `nautilus-server/src/main/java/vn/io/huangnosimp/nautilusserver/api/AuthUserQuery.java`, `api/UserQuery.java`, `api/ServerQuery.java`
- Modify: `.../events/ServerData.java` (thêm `ownerUserId`)
- Modify: `.../authentication/services/AuthUserQueryImpl.java`, `.../authentication/repositories/AuthUserRepository.java`
- Modify: `.../user/services/UserQueryImpl.java`, `.../user/repositories/UserRepository.java`
- Modify: `.../server/services/ServerQueryImpl.java`, `.../server/repositories/ServerRepository.java`
- Test: `nautilus-server/src/test/java/vn/io/huangnosimp/nautilusserver/QueryAdaptersTest.java`

**Interfaces:**
- Produces:
  - `Map<String, String> AuthUserQuery.getUsernamesByAuthUserIds(Collection<String> authUserIds)`: id → username; id không tồn tại thì không có trong map; input rỗng → `Map.of()` và không gọi repository.
  - `Map<String, UserData> UserQuery.getUserDataByAuthUserIds(Collection<String> authUserIds)`
  - `Map<String, ServerData> ServerQuery.getPublicServerDataByServerIds(Collection<String> serverIds)`: chỉ server `visibility = true`.
  - `List<String> ServerQuery.filterPublicServerIds(Collection<String> serverIds)`
  - `ServerData(String serverId, String ownerUserId, String servername, Integer serverTotalStar, String serverAvatarUrl)`. `getPublicServerDataByServerId` cũ cũng phải điền `ownerUserId`.
  - Repository: `AuthUserRepository.findIdAndUsernameByIdIn(Collection<String>)` (projection `interface IdUsername { String getId(); String getUsername(); }` hoặc JPQL `SELECT a.id AS id, a.username AS username`); `UserRepository.findAllByAuthUserIdIn(Collection<String>)`; `ServerRepository.findAllByIdInAndVisibilityIsTrue(Collection<String>)`; `ServerRepository.findPublicIdsByIdIn(Collection<String>)` (JPQL `SELECT s.id FROM Server s WHERE s.id IN :ids AND s.visibility = true`).

- [ ] **Step 1: Viết test thất bại trong `QueryAdaptersTest`**

```java
@Test void authQueryResolvesUsernamesInOneBatch()
// repository.findIdAndUsernameByIdIn(Set.of("u1","u2")) trả 2 dòng
// assertEquals(Map.of("u1","alice","u2","bob"), query.getUsernamesByAuthUserIds(List.of("u1","u2","u1")))
// verify(repository, times(1)).findIdAndUsernameByIdIn(any())

@Test void batchQueriesShortCircuitOnEmptyInput()
// getUsernamesByAuthUserIds(List.of()), getUserDataByAuthUserIds(List.of()),
// getPublicServerDataByServerIds(List.of()), filterPublicServerIds(List.of()) đều rỗng;
// verifyNoInteractions(cả ba repository)

@Test void serverQueryBatchReturnsOnlyPublicServersWithOwner()
// findAllByIdInAndVisibilityIsTrue trả server s1 (userId "owner-1", servername "Atlantis", starCount 3)
// result.get("s1").ownerUserId() == "owner-1"; !result.containsKey("s2")

@Test void userQueryBatchSignsAvatarPerUser()
// 2 user, storage.createSignedGetUrl("a1") -> "url1"; map theo authUserId
```

Cập nhật test `serverQueryMapsPublicDataAndDelegatesVisibilityLookups` hiện có để assert thêm `ownerUserId`.

- [ ] **Step 2: Chạy test, xác nhận thất bại**

Run: `cd nautilus-server && ./gradlew test --tests '*QueryAdaptersTest'`
Expected: lỗi biên dịch (các method chưa tồn tại).

- [ ] **Step 3: Implement các method trong mục Interfaces.** Trước khi gọi repository thì bỏ trùng id (`Set.copyOf`). Gắn `@Logging(level = Level.DEBUG)` như các method cạnh đó.

- [ ] **Step 4: Chạy test, xác nhận pass**

Run: `./gradlew test --tests '*QueryAdaptersTest' --tests '*ArchitectureTest'`
Expected: PASS.

- [ ] **Step 5: Commit**: `git commit -m "feat(server): add batch cross-module query adapters"`

### Task 2: Mở rộng `ServerInfoResponse` và fix N+1 ở danh sách server

**Files:**
- Modify: `.../server/dto/responses/ServerInfoResponse.java`
- Modify: `.../server/services/ServerService.java` (`findServers`, `getServerByUser`, thêm `toInfoPage`)
- Modify: `.../server/repositories/TagRepository.java`, `JavaMcVersionRepository.java`, `BedrockMcVersionRepository.java`
- Test: `nautilus-server/src/test/java/.../server/services/ServerServiceTest.java`

**Interfaces:**
- Consumes: `AuthUserQuery.getUsernamesByAuthUserIds` (Task 1).
- Produces: record `ServerInfoResponse(String username, String servername, Boolean requiresPremium, String avatarUrl, String bannerUrl, String summary, Integer starCount, Boolean visibility, ServerType serverType, ServerPlatform serverPlatform, List<String> tags, String primaryVersion)`. Client (Task 6) dựa vào đúng các tên JSON này.
- Repository: `TagRepository.findAllByServerIdInOrderByCreatedAtAsc(Collection<String>)`; `JavaMcVersionRepository.findAllByServerIdInAndIsPrimaryMcVersionTrue(Collection<String>)`; tương tự cho `BedrockMcVersionRepository`.

- [ ] **Step 1: Viết test thất bại.** Thêm `@Mock` cho `TagRepository`, `JavaMcVersionRepository`, `BedrockMcVersionRepository`, `ResourceStorage`, `AuthUserQuery` (nếu chưa có).

```java
@Test void listingMapsExtendedFieldsWithBatchedLookups()
// trang có 3 server: s1 JAVA (banner "b1", 4 tag theo thứ tự t1..t4, primary java "1.21.1"),
//                    s2 BEDROCK (không tag, primary bedrock "1.21.40"), s3 JAVA (không có primary)
// cả 3 cùng owner "o1" -> username "alice"
// assert s1: bannerUrl == signed("b1"), tags == [t1,t2,t3], primaryVersion "1.21.1", serverPlatform JAVA, visibility true
// assert s2: tags == [], primaryVersion "1.21.40"; s3: primaryVersion null
// verify(authUserQuery, times(1)).getUsernamesByAuthUserIds(any())
// verify(tagRepository, times(1)).findAllByServerIdInOrderByCreatedAtAsc(any())
// verify(javaRepo, times(1)) và verify(bedrockRepo, times(1)) ...IsPrimaryMcVersionTrue(any())
// verify(authUserQuery, never()).getUsernameByAuthUserId(any())

@Test void listingSkipsBedrockLookupWhenPageHasNoBedrockServer()
// toàn server JAVA -> verify(bedrockRepo, never())...

@Test void emptyPageDoesNoBatchLookups()
// verifyNoInteractions(authUserQuery, tagRepository, javaRepo, bedrockRepo)

@Test void listingFailsClearlyWhenOwnerMissing()
// getUsernamesByAuthUserIds trả map rỗng -> assertThrows(UserNotFound.class, ...)

@Test void ownerListingIncludesPrivateServersWithVisibilityFalse()
// getServerByUser("o1","o1",...) dùng findAllByUserId; server private -> visibility false trong response
```

- [ ] **Step 2: Chạy test, xác nhận thất bại**

Run: `./gradlew test --tests '*ServerServiceTest'`
Expected: lỗi biên dịch hoặc FAIL.

- [ ] **Step 3: Implement `private Page<ServerInfoResponse> toInfoPage(Page<Server> page)`** theo spec §5.2 (bước 1–5). `findServers` và `getServerByUser` đều gọi hàm này. Lấy tag theo lô thì gom bằng `LinkedHashMap` để giữ thứ tự `createdAt`, rồi `limit(3)`. Chỉ query version của platform có mặt trong trang.

- [ ] **Step 4: Chạy test, xác nhận pass**

Run: `./gradlew test --tests '*ServerServiceTest' --tests '*ServerControllerWebTest' --tests '*ArchitectureTest'`
Expected: PASS.

- [ ] **Step 5: Commit**: `git commit -m "feat(server): extend server summary DTO and batch its lookups"`

### Task 3: `StarService`: batch, đúng username chủ server, lọc server riêng tư

**Files:**
- Modify: `.../star/services/StarService.java`
- Modify: `.../star/repositories/StarRepository.java`
- Modify: `docs/superpowers/specs/2026-11-13-ui-redesign-design.md` §5.3 (ghi lại điều chỉnh bên dưới)
- Test: `nautilus-server/src/test/java/.../star/services/StarServiceTest.java`

**Điều chỉnh so với spec:** spec §5.3 dùng JPQL join `Star`–`Server`, nhưng như vậy vi phạm ranh giới Spring Modulith. Thay vào đó dùng 3 query:
1. `StarRepository.findServerIdsByUserId(String userId)`, JPQL `SELECT s.serverId FROM Star s WHERE s.userId = :userId`.
2. `ServerQuery.filterPublicServerIds(ids)`.
3. `StarRepository.findAllByUserIdAndServerIdIn(String userId, Collection<String> serverIds, Pageable)`.

Cách này phân trang và đếm `totalElements` đúng mà không đổi schema. Chi phí là O(số sao của user) id, chấp nhận được. Danh sách id công khai rỗng thì trả `Page.empty(pageable)` và bỏ qua query 3.

**Interfaces:**
- Consumes: `UserQuery.getUserDataByAuthUserIds`, `ServerQuery.getPublicServerDataByServerIds`, `ServerQuery.filterPublicServerIds`, `AuthUserQuery.getUsernamesByAuthUserIds`, `ServerData.ownerUserId` (Task 1). Thêm `AuthUserQuery` vào constructor của `StarService`.
- Produces: `GET /stars/{username}`: `ServerStaredInfo.username` = **username chủ server**. Đây là hợp đồng mà trang Stars (Task 11) dùng.

- [ ] **Step 1: Viết test thất bại; sửa test cũ `starredUsersAreMappedAndMissingUserDataFailsClearly` sang API theo lô**

```java
@Test void starredServersReportOwnerUsernameNotStargazer()
// user "fan" sao server s1 (owner "o1" -> "alice")
// result.getContent().getFirst().username() == "alice"  (KHÔNG phải username của "fan")

@Test void privateStarredServerIsExcludedAndPaginationCountsOnlyPublic()
// findServerIdsByUserId("fan") -> [s1, s2]; filterPublicServerIds -> [s1]
// verify(starRepository).findAllByUserIdAndServerIdIn(eq("fan"), eq(List.of("s1")), any())
// không ném exception

@Test void noPublicStarredServersReturnsEmptyPageWithoutPageQuery()

@Test void starredListsResolveDataInOneBatchEach()
// trang 3 sao: verify times(1) cho getPublicServerDataByServerIds, getUsernamesByAuthUserIds;
// getUserStaredInfo: verify times(1) getUserDataByAuthUserIds; never() các method lấy từng cái

@Test void stargazerWithMissingUserDataFailsClearly()
// getUserDataByAuthUserIds trả map thiếu user -> UserNotFound
```

- [ ] **Step 2: Chạy test, xác nhận thất bại**

Run: `./gradlew test --tests '*StarServiceTest'`
Expected: FAIL hoặc lỗi biên dịch.

- [ ] **Step 3: Implement** `getUserStaredInfo` và `getServerStaredInfo` theo mục điều chỉnh ở trên. Bỏ `getUserData(userId)` đang nằm trong vòng lặp. Nếu sau bước lọc server bị chuyển sang riêng tư (race), `getPublicServerDataByServerIds` thiếu server thì bỏ qua phần tử đó, không ném exception. Xoá các helper `getUserData`/`getServerData` nếu không còn dùng.

- [ ] **Step 4: Chạy test, xác nhận pass**

Run: `./gradlew test --tests '*StarServiceTest' --tests '*ReadControllersWebTest' --tests '*ArchitectureTest'`
Expected: PASS.

- [ ] **Step 5: Cập nhật spec §5.3 (lỗi 2) theo mục điều chỉnh, rồi commit**: `git commit -m "fix(star): return server owner username, skip private servers, batch lookups"`

### Task 4: Siết quyền xem người gắn sao và cập nhật tài liệu API

**Files:**
- Modify: `.../star/controllers/StarController.java:26-29`
- Create: `nautilus-server/src/test/java/vn/io/huangnosimp/nautilusserver/star/controllers/StarControllerSecurityTest.java`
- Modify: `client-requirement.md` (bảng Server: các trường mới của `/servers`, `/servers/search`, `/servers/{username}`; bảng Star: quyền mới và nghĩa của `username`)

- [ ] **Step 1: Viết test thất bại.** `@WebMvcTest(StarController.class)` bật method security (`@Import` một `@TestConfiguration @EnableMethodSecurity`), `@MockitoBean(name = "permissionCheckerImpl") PermissionChecker`, `@MockitoBean GlobalIdResolver`, `@MockitoBean StarService`, `@WithMockUser("bob")`. Bỏ qua filter chain bằng `@AutoConfigureMockMvc(addFilters = false)` giống `ReadControllersWebTest`.

```java
@Test void viewerWhoIsNotEditorIsForbiddenFromStargazers()
// isViewer -> true, isEditor -> false; GET /stars/owner/server -> 403
@Test void editorCanListStargazers()
// isEditor -> true -> 200; verify(starService).getUserStaredInfo(eq("server-id"), any())
```

- [ ] **Step 2: Chạy test, xác nhận test 403 thất bại** (hiện vẫn đang dùng `isViewer`).

Run: `./gradlew test --tests '*StarControllerSecurityTest'`

- [ ] **Step 3: Đổi `isViewer` thành `isEditor`** trong `@PreAuthorize` của `getUserStaredInfo`.

- [ ] **Step 4: Chạy toàn bộ test backend**

Run: `./gradlew test`
Expected: tất cả xanh, số test ≥ 89 + số test mới.

- [ ] **Step 5: Cập nhật `client-requirement.md`, rồi commit**: `git commit -m "feat(star): restrict stargazer list to editors; document API changes"`

---

## Phần B: Frontend

Mọi task frontend đều kết thúc bằng cùng một **cổng xác minh** (gọi là "Gate"):

```bash
cd nautilus-client && pnpm lint && pnpm typecheck && pnpm build
```

Expected: cả ba exit 0, không có warning mới. Riêng các task có giao diện thì kèm thêm bước **kiểm tra trực quan**: chạy `pnpm dev` với backend đang chạy, mở trang liên quan ở 1280px và 375px, cả sáng lẫn tối, rồi so với mockup trong `.superpowers/brainstorm/61520-1794500662/content/`. Dùng skill `run` hoặc `claude-in-chrome` để chụp màn hình.

### Task 5: Nền tảng: font, token, utility, theme Nuxt UI

**Files:**
- Modify: `nautilus-client/package.json` (thêm `@nuxt/fonts` qua `pnpm add @nuxt/fonts`), `nuxt.config.ts` (thêm module và cấu hình `fonts`)
- Modify: `nautilus-client/app/assets/css/main.css`
- Modify: `nautilus-client/app/app.config.ts`
- Modify: `.gitignore` (thêm dòng `.superpowers/`; chỉ stage dòng này bằng `git add -p` vì file đang có thay đổi khác của chủ dự án)

**Interfaces:**
- Produces (CSS, mọi task sau đều dùng): class `font-pixel`, `text-pixel-{sm,base,lg,xl,2xl,3xl,4xl}`, `block-surface`, `block-lift`, `lift-{primary,secondary,ink,warning,error}`, `animate-rise` (đọc `--i`), `bg-block-grid`; biến `--block-border`, `--block-offset`, `--lift-color`.

- [ ] **Step 1:** Cấu hình `fonts` trong `nuxt.config.ts`: `families: [{ name: 'Be Vietnam Pro', weights: [400,500,600], subsets: ['latin','vietnamese'] }, { name: 'VT323', weights: [400], subsets: ['latin','vietnamese'] }]`, provider `google`, `defaults.preload` chỉ cho Be Vietnam Pro 400.
- [ ] **Step 2:** Viết token và utility trong `main.css` theo spec §2.1–2.5 và Global Constraints. Tách token sáng/tối bằng `:root` và `.dark` (cách Nuxt UI color-mode dùng). Thang `text-pixel-*`: `sm 1.125rem`, `base 1.25rem`, `lg 1.5rem`, `xl 1.75rem`, `2xl 2.25rem`, `3xl 2.75rem`, `4xl 3.5rem`, line-height 1. Đổi heading của `.markdown` sang `font-pixel`.
- [ ] **Step 3:** Ghi đè theme trong `app.config.ts` theo spec §2.6. Giữ `compoundVariants` chữ tối trên nền primary. Xoá comment "Design rule: no outlined…".
- [ ] **Step 4: Gate + kiểm tra trực quan:** mọi trang hiện có vẫn dùng được; nút, input và card đã đổi sang kiểu khối; tiếng Việt có dấu hiển thị đúng ở `font-pixel`; DevTools bật `prefers-reduced-motion` thì hover không dịch chuyển; tab qua các nút luôn thấy focus ring.
- [ ] **Step 5: Commit**: `git commit -m "feat(client): Blocky Modern design tokens, fonts and Nuxt UI theme"`

### Task 6: Kiểu dữ liệu và component nền

**Files:**
- Modify: `app/types/api.ts` (thêm các trường mới vào `ServerSummary`, khớp record ở Task 2: `bannerUrl: string | null`, `visibility: boolean`, `serverType: ServerType`, `serverPlatform: ServerPlatform`, `tags: string[]`, `primaryVersion: string | null`)
- Create: `app/components/PixelShell.vue`, `BlockAvatar.vue`, `PageHeader.vue`, `app/utils/blockColor.ts`, `app/directives/rise.ts`, `app/plugins/rise.ts`, `app/error.vue`
- Modify: `app/components/MediaImage.vue`, `AppLogo.vue`, `EmptyState.vue`
- Modify: `i18n/locales/vi.json`, `en.json` (khoá `errors.pageNotFound`, `errors.pageNotFoundHint`, `errors.backHome`)

**Interfaces:**
- Produces:
  - `blockColor(name: string): { face: string, shade: string, light: string }`: hash ổn định (vd. FNV-1a) chọn từ một bảng cố định 6 màu brand-friendly (nautilus, ocean, amber, rose, violet, teal).
  - `<BlockAvatar :name="string" shape="square|banner" />`: `square` là khối có shading + chữ cái đầu (`font-pixel`); `banner` là sọc chéo 45°.
  - `<MediaImage :src :alt :name? :fallback-icon? :eager? />`: thiếu `src` hoặc ảnh `@error` thì render `BlockAvatar` (`name ?? alt`; `shape` là `banner` khi được truyền `banner` prop boolean).
  - `<PixelShell :size="number" />`: SVG lưới 8×8, `shape-rendering="crispEdges"`, `role="img"` + `aria-label="Nautilus"`.
  - `<PageHeader :title :description?>` với slot `actions`.
  - Directive `v-rise="index"`: một IntersectionObserver dùng chung, `threshold: 0.1`, đặt `style.setProperty('--i', index)` và thêm `animate-rise` khi vào màn hình rồi `unobserve`. Không có IO thì không làm gì.

- [ ] **Step 1:** Implement các file theo Interfaces và spec §3.1–3.2. `error.vue` dùng `useError`/`clearError({ redirect: '/' })`, hiện `statusCode` bằng `font-pixel text-pixel-4xl`.
- [ ] **Step 2:** Đổi `AppLogo` sang `PixelShell` + "Nautilus" `font-pixel`. Đổi `EmptyState` theo spec §3.2.
- [ ] **Step 3: Gate.**
- [ ] **Step 4: Kiểm tra trực quan (Review Focus #1):** mở một server không có avatar/banner → thấy `BlockAvatar`. Trong DevTools, chặn request tới host object storage (Network request blocking) → ảnh lỗi chuyển sang `BlockAvatar`, không có icon ảnh vỡ. Mở `/khong-ton-tai` → trang 404 mới, nút về trang chủ hoạt động.
- [ ] **Step 5: Commit**: `git commit -m "feat(client): base pixel components, rise directive, error page"`

### Task 7: Component server dùng chung

**Files:**
- Create: `app/components/ServerTile.vue`, `ServerRow.vue`, `ServerRepoCard.vue`
- Delete: `app/components/ServerCard.vue`
- Modify: `app/components/ServerList.vue`, `StarButton.vue`, `CopyAddress.vue`
- Modify: i18n (`server.public`, `server.openServer`)

**Interfaces:**
- Consumes: `MediaImage`, `blockColor`, `v-rise` (Task 6); các trường `ServerSummary` (Task 6).
- Produces:
  - `<ServerTile :server="ServerSummary" :index="number" :show-visibility?="boolean">` với slot `actions`. `lift-secondary` khi `serverPlatform === 'BEDROCK'`, ngược lại `lift-primary`. Badge visibility nằm ở góc banner, chỉ hiện khi `showVisibility`.
  - `<ServerRow :server="ServerSummary" :index="number">`
  - `<ServerRepoCard :username :servername :avatar-url :star-count :summary? :platform? :index>` với slot `actions`.
  - `<ServerList :servers :loading? :view="'list' | 'grid'">`, mặc định `'list'`.

- [ ] **Step 1:** Implement theo spec §3.1 và mockup `home-layout.html` / `browse-light-dark.html`. Link chính dùng `NuxtLink`; slot `actions` nằm ngoài link để không lồng phần tử tương tác. Số sao dùng `n()` và có `aria-label` như `ServerCard` cũ.
- [ ] **Step 2:** Restyle `StarButton` (vàng, `lift-ink`, số sao trong ô con) và `CopyAddress` (ô `font-pixel`, nút Copy primary, chuyển sang ✓ trong 1500ms) theo spec §3.2. Giữ nguyên logic.
- [ ] **Step 3: Gate + kiểm tra trực quan (Review Focus #2):** tạo, hoặc sửa tạm qua trang manage, một server tên 50 ký tự có dấu (`Thế Giới Ngầm Siêu Cấp Vũ Trụ Đại Chiến Phiên Bản Mùa Đông`), summary rỗng, 0 tag, không có primary version, không có banner. Kiểm tra `ServerTile`, `ServerRow`, `ServerRepoCard` ở 375px và 1280px: tên bị truncate với `…`, không có chip rỗng hay "·" thừa, chiều cao card trong lưới đều nhau.
- [ ] **Step 4: Commit**: `git commit -m "feat(client): ServerTile, ServerRow and ServerRepoCard"`

### Task 8: Layout và Home

**Files:**
- Modify: `app/app.vue`, `app/components/UserMenu.vue`, `LanguageSwitcher.vue`
- Create: `app/components/ServerShelf.vue`
- Modify: `app/pages/index.vue`
- Modify: i18n (`home.shelves.{popular,survival,modpack,bedrock}`, `home.seeAll`, `home.searchShortcut`, `tags.label.<TagName>` cho 6 chip trong cả hai ngôn ngữ)

**Interfaces:**
- Consumes: `ServerTile`, `PixelShell` (Task 6–7); `useServerApi().list` (có sẵn).
- Produces: `<ServerShelf :title="string" :filters="ServerFilters & { sort?: string }" :to="RouteLocationRaw">`: fetch `list({ ...filters, size: 8, page: 0 })` với key `shelf:${JSON.stringify(filters)}`; ẩn cả kệ khi lỗi hoặc rỗng.

- [ ] **Step 1:** `app.vue`: `body` có `bg-block-grid`; header và footer theo spec §4 (Layout). `UserMenu`: avatar vuông có viền khối, giữ nguyên các mục.
- [ ] **Step 2:** `index.vue` theo spec §4 (Home) và mockup `home-layout.html` (A). Phím `/` focus ô tìm kiếm khi focus không nằm trong input/textarea (`useEventListener` hoặc `onKeyStroke` của VueUse). Chip dẫn tới `/browse?tags=<TagName>`. 4 kệ: `{ sort: 'starCount,desc' }`, `{ tags: ['Survival Mode'] }`, `{ serverType: 'MODPACK' }`, `{ serverPlatform: 'BEDROCK' }`. Trên mobile, kệ là `flex overflow-x-auto snap-x`, mỗi tile `snap-start w-64 shrink-0`.
- [ ] **Step 3: Gate + kiểm tra trực quan:** Home ở 375/1280, sáng/tối, vi/en; các card trồi lên lần lượt; phím `/` focus ô tìm kiếm; kệ rỗng thì bị ẩn.
- [ ] **Step 4: Commit**: `git commit -m "feat(client): blocky layout and home with server shelves"`

### Task 9: Browse

**Files:**
- Modify: `app/pages/browse.vue`, `app/components/ServerFilters.vue`
- Create: `app/composables/useBrowseView.ts`
- Modify: i18n (`browse.viewList`, `browse.viewGrid`, `browse.activeFilters`, `browse.removeFilter`)

**Interfaces:**
- Produces: `useBrowseView(): Ref<'list' | 'grid'>`. Đọc `localStorage['nautilus:browse-view']` khi khởi tạo, ghi lại khi đổi; mọi truy cập đều bọc `try/catch`; giá trị lạ hoặc lỗi → `'list'`.

- [ ] **Step 1:** Implement `useBrowseView`. Trong `browse.vue`, thêm nhóm 2 nút icon (`i-lucide-list`, `i-lucide-layout-grid`, `aria-pressed`, `aria-label` i18n) và truyền `:view` cho `ServerList`. Thêm hàng chip "bộ lọc đang áp dụng": mỗi giá trị (platform, type, premium, từng tag) là một chip có ✕, bấm thì gọi `update` bỏ giá trị đó. Restyle `ServerFilters` (checkbox khối, tiêu đề nhóm `font-pixel`).
- [ ] **Step 2: Gate + kiểm tra trực quan** theo mockup `browse-light-dark.html`, kể cả slideover bộ lọc trên mobile.
- [ ] **Step 3: Kiểm tra Review Focus #5:** trong console chạy `Object.defineProperty(window, 'localStorage', { get() { throw new Error('blocked') } })` trước khi điều hướng client-side tới `/browse` (hoặc mở bằng cửa sổ có chặn site data) → trang hiện chế độ list, bấm chuyển grid vẫn chạy trong phiên, không có lỗi chưa bắt.
- [ ] **Step 4: Commit**: `git commit -m "feat(client): browse with list/grid toggle and active filter chips"`

### Task 10: Server detail

**Files:**
- Modify: `app/pages/s/[username]/[servername]/index.vue`
- Modify: `app/components/GalleryGrid.vue` (thêm prop `limit?: number`; lightbox `UModal` khi bấm ảnh, nếu chưa có)
- Modify: `app/composables/useServerPermission.ts` (trả thêm `resolved: Ref<boolean>`: owner hoặc chưa đăng nhập → `true` ngay; editor probe → `true` sau khi probe xong)
- Modify: i18n (`server.tabs.{about,gallery,stargazers}`, `gallery.seeAll`)

**Interfaces:**
- Consumes: `useServerPermission().canEdit`, `StarButton`, `CopyAddress`, `MediaImage` (Task 6–7).
- Tab đồng bộ `?tab=about|gallery|stargazers` qua `router.replace`, mặc định `about`.

- [ ] **Step 1:** Làm lại trang theo spec §4 (Server detail) và mockup `server-detail.html`. Tab Giới thiệu hiện `GalleryGrid :limit="3"` + link "Xem tất cả (n) →" (`n = gallery.totalElements`, chỉ hiện khi `n > 3`) chuyển sang tab gallery. Tab gallery có phân trang (`size: 24`). Xoá khối stargazers ở cột phải. Cột phải dùng `lg:sticky lg:top-[calc(var(--ui-header-height)+1.5rem)]`.
- [ ] **Step 2:** `useAsyncData` của stargazers dùng `immediate: false` + `watch(canEdit, v => v && execute(), { immediate: true })`. Tab stargazers chỉ có trong `items` khi `canEdit`. `watch` `[tab, canEdit]`: nếu `tab === 'stargazers'` và quyền đã được xác định là không có thì `replace` về `about`. Để không đá editor ra trong lúc probe đang chạy, `useServerPermission` cần trả thêm `resolved: Ref<boolean>`; chỉ redirect khi `resolved && !canEdit`.
- [ ] **Step 3: Kiểm tra Review Focus #3:** (a) ẩn danh mở `/s/<u>/<s>?tab=stargazers` → về tab Giới thiệu, tab Network không có request `/api/stars/<u>/<s>`; (b) đăng nhập bằng editor không phải chủ → sau khi probe xong, tab xuất hiện và mở thẳng `?tab=stargazers` vẫn ở đúng tab; (c) owner → tab có ngay.
- [ ] **Step 4: Gate + kiểm tra trực quan** ở 375/1280, sáng/tối: server có hoặc không có địa chỉ, có hoặc không có gallery, gallery ≤ 3 và > 3 ảnh.
- [ ] **Step 5: Commit**: `git commit -m "feat(client): tabbed server detail with sticky join panel"`

### Task 11: Dashboard, Profile, Stars

**Files:**
- Modify: `app/pages/dashboard.vue`, `app/pages/u/[username].vue`, `app/pages/stars.vue`, `app/stores/stars.ts` (thêm `star(username: string, servername: string): Promise<void>`, đối xứng với `unstar` có sẵn, cập nhật cùng tập `isStarred`)
- Modify: i18n (`dashboard.serverCount`, `dashboard.createTile`, `stars.undo`, `stars.undoFailed`)

**Interfaces:**
- Consumes: `ServerTile` (slot `actions`, `show-visibility`), `ServerRepoCard`, `PageHeader` (Task 6–7); `StarredServer.username` = chủ server (Task 3).

- [ ] **Step 1:** Dashboard theo spec §4 và mockup `dashboard-profile.html`: `PageHeader` + chip `t('dashboard.serverCount', { n })` từ `totalElements`; lưới `ServerTile :show-visibility="true"` với các nút Xem/Sửa/Xoá; ô "+ Tạo server" nét đứt chỉ hiện khi `page === 1`. Giữ modal xoá.
- [ ] **Step 2:** Profile: thẻ trái `lift-secondary` với bóng tĩnh, lưới phải dùng `ServerRepoCard`.
- [ ] **Step 3:** Stars: lưới `ServerRepoCard`, nút ★ trong slot actions. Bỏ sao theo kiểu optimistic: lấy phần tử khỏi danh sách hiển thị (nhớ index), gọi `stars.unstar`. Lỗi thì chèn lại đúng index và `notify(err)`. Thành công thì toast có action "Hoàn tác": gọi `stars.star(u, s)`, thành công thì chèn lại đúng index, lỗi thì toast `stars.undoFailed`. Có cờ chống bấm Hoàn tác hai lần.
- [ ] **Step 4: Kiểm tra Review Focus #4 và lỗi backend đã sửa:** (a) gắn sao một server của user khác → trên Stars, link mở đúng server và nút bỏ sao gọi đúng `/stars/<chủ>/<server>`; (b) chủ server chuyển server đó sang riêng tư → `/stars` vẫn tải được và server đó biến mất; (c) DevTools chặn `PUT/DELETE /api/stars/*` → card hiện lại kèm toast lỗi; (d) bấm Hoàn tác hai lần liên tiếp → chỉ có một card.
- [ ] **Step 5: Gate + kiểm tra trực quan, rồi commit**: `git commit -m "feat(client): dashboard, profile and stars in block style"`

### Task 12: Đăng nhập, đăng ký, callback

**Files:**
- Modify: `app/pages/login.vue`, `app/pages/register.vue`, `app/pages/auth/callback.vue`
- Create: `app/components/AuthShell.vue` (bố cục chia đôi dùng chung), `app/utils/passwordStrength.ts`, `app/composables/useAvailability.ts`
- Modify: i18n (`auth.panelTagline`, `auth.available`, `auth.taken`, `auth.checking`, `auth.strength.{0,1,2,3,4}`)

**Interfaces:**
- Produces:
  - `passwordStrength(pw: string): 0 | 1 | 2 | 3 | 4`: 0 khi rỗng; +1 cho mỗi tiêu chí trong số (độ dài ≥ 8; có cả hoa lẫn thường; có số; có ký tự không phải chữ/số); nếu độ dài < 8 thì tối đa 1.
  - `useAvailability(kind: 'username' | 'email', value: Ref<string>, isValid: (v: string) => boolean): Ref<'idle' | 'checking' | 'available' | 'taken' | 'error'>`: debounce 400ms, chỉ gọi khi `isValid`, bỏ qua kết quả cũ nếu giá trị đã đổi (so sánh với giá trị mới nhất).
- Kiểm tra trống chỉ để gợi ý; vẫn submit được khi `error`; server mới là nơi validate cuối cùng.

- [ ] **Step 1:** `AuthShell` (panel trái ẩn dưới `lg`: `PixelShell`, tagline, lưới; slot phải là card `lift-primary` với bóng tĩnh). Login và register dùng chung. Nút Google dạng khối.
- [ ] **Step 2:** Register: hiện trạng thái ✓/✕ + chữ (`aria-live="polite"`) cạnh username/email; thanh 4 ô pixel cho độ mạnh mật khẩu, kèm nhãn chữ (không truyền thông tin chỉ bằng màu).
- [ ] **Step 3:** Callback: spinner 3 ô vuông (tắt animation khi reduced-motion) + chữ.
- [ ] **Step 4: Gate + kiểm tra trực quan:** gõ nhanh username đã tồn tại rồi sửa → trạng thái cuối khớp giá trị cuối; đăng nhập Google vẫn chuyển hướng đúng.
- [ ] **Step 5: Commit**: `git commit -m "feat(client): split-screen auth with availability and strength hints"`

### Task 13: Settings, New server, Manage

**Files:**
- Modify: `app/pages/settings.vue`, `app/pages/new.vue`, `app/pages/s/[username]/[servername]/manage.vue`, `app/components/manage/*.vue`, `ImageUploader.vue`, `MarkdownEditor.vue`, `VersionEditor.vue`
- Modify: i18n (`settings.sections.*`, `create.platformHint.*`, `create.typeHint.*`, `manage.unsaved`, `manage.discard`)

- [ ] **Step 1:** Settings theo spec §4: menu dọc (`UNavigationMenu orientation="vertical"` trên `md+`, tab ngang dưới `md`), đồng bộ `?section=profile|account|appearance`. Chế độ sáng/tối là 3 thẻ khối (`URadioGroup` variant `card`).
- [ ] **Step 2:** New: platform, type và visibility dùng `URadioGroup` variant `card` có icon + mô tả. Giữ validate và submit hiện tại.
- [ ] **Step 3:** Manage: menu dọc thay cho `UTabs` ngang (giữ nguyên `items`/giá trị tab hiện tại). Thanh "Lưu / Huỷ" dính ở đáy chỉ dành cho các form đang theo dõi trạng thái dirty (General); nếu component con chưa expose dirty state thì thêm `defineExpose({ dirty, save, reset })` cho `manage/General.vue`. Vùng nguy hiểm trong `manage/Advanced.vue` dùng `lift-error` và viền đỏ.
- [ ] **Step 4: Gate + kiểm tra trực quan** từng tab Manage (Chung, Tag, Phiên bản, Thư viện ảnh, Nâng cao) và từng mục Settings, ở 375/1280, sáng/tối.
- [ ] **Step 5: Commit**: `git commit -m "feat(client): block-style settings, new server and manage pages"`

### Task 14: Xác minh toàn bộ

- [ ] **Step 1:** `cd nautilus-server && ./gradlew test` → tất cả xanh. `cd nautilus-client && pnpm lint && pnpm typecheck && pnpm build` → sạch.
- [ ] **Step 2:** Chạy backend + `pnpm dev`. Chụp màn hình các trang Home, Browse (list/grid), Detail (3 tab), Dashboard, Profile, Stars, Settings, New, Manage, Login, Register, 404, theo ma trận {375, 1280} × {sáng, tối}, cùng một lượt `en` cho Home/Browse/Detail. Lưu ảnh vào scratchpad và đối chiếu với mockup.
- [ ] **Step 3:** Kiểm tra bàn phím ở Home, Browse và Detail (tab, mũi tên trong UTabs, Enter/Space trên card, Esc đóng modal/slideover).
- [ ] **Step 4:** Đo tương phản (DevTools hoặc tính tỉ lệ từ hex) cho: `text-muted` trên nền và bề mặt (sáng/tối), chữ `gray-950` trên `nautilus-400`, `nautilus-700` trên `gray-50`, chữ trên nút sao `amber-300`. Tất cả phải ≥ 4.5:1 (chữ ≥ 24px hoặc in đậm ≥ 18.66px thì ≥ 3:1).
- [ ] **Step 5:** Bật giả lập `prefers-reduced-motion` → không còn dịch chuyển hay trồi lên ở mọi trang.
- [ ] **Step 6:** Ghi kết quả (đạt/không đạt kèm bằng chứng) vào phần mô tả PR hoặc báo lại cho chủ dự án. Không commit ảnh chụp màn hình.
