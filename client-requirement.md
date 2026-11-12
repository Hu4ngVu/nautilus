# Mục tiêu:
- Thiết kế client cho server.
- Tạo ra một sản phẩm dễ sử dụng, thân thiện với người dùng.
- Đảm bảo rằng giao diện người dùng đáp ứng các yêu cầu về khả năng truy cập và tương thích với nhiều thiết bị.
- Đảm bảo rằng giao diện người dùng phản hồi nhanh và hiệu quả, tối ưu hóa hiệu suất của ứng dụng web.

# Yêu cầu kỹ thuật:
- Sử dụng Nuxt.js, nuxt ui, nuxt image để xây dựng Single Page Application (SPA) client cho server.
- có thể sử dụng thêm các thư viện khác nếu cần thiết, nhưng phải đảm bảo rằng chúng không làm giảm hiệu suất của ứng dụng.

# Yêu cầu về giao diện người dùng:
## Màu sắc:
- neutral colors: chọn bảng màu Gray từ tailwindcss
- primary colors: #73ff9c, #2ff96b, #04e347
- secondary colors: #6180fa, #2f58f9, #0736ee
## Font:
- nội dung dùng Be Vietnam Pro; heading, tên server, nút và tab dùng VT323 (thay cho Arial, theo spec thiết kế lại UI 2026-11-13).
## Tính năng:
- hỗ trợ chế độ sáng/tối, có thể chuyển đổi bằng nút bấm.
- hỗ trợ responsive, hiển thị tốt trên các thiết bị di động và máy tính để bàn.
- hỗ trợ chuyển đổi ngôn ngữ giữa tiếng Việt và tiếng Anh, có thể chọn ngôn ngữ trong cài đặt.
- hỗ trợ đăng nhập bằng tài khoản Google, có thể đăng nhập bằng email và mật khẩu.
- hỗ trợ đăng ký tài khoản mới bằng email và mật khẩu hoặc đăng ký bằng tài khoản Google.
- hỗ trợ định dạng gif cho server avatar và banner.
- hỗ trợ markdown cho user bio và server bio.
## Page:
- Home page: phía trên có 2 tab: browse và dashboard, góc trên bên trái có logo kèm tên ứng dụng, góc trên bên phải có avatar người dùng, 
khi click vào avatar sẽ hiện ra menu dropdown với các tùy chọn: profile, setting, logout, star. Ở giữa là logo kèm dòng chữ
"Discover and share your favorite Minecraft servers". 
- Browse page: thiết kế giống trang discover của modrinth
- Dashboard page: hiển thị danh sách server của người dùng, có thể thêm, sửa, xóa server. Mỗi server hiển thị thông tin cơ bản như tên server, số lượng star, avatar và banner. Có thể click vào server để xem chi tiết và chỉnh sửa.
- Server detail page: hiển thị thông tin chi tiết về server.
- Profile page: hiển thị thông tin cá nhân của người dùng ở thẻ bên trái và danh sách server của người dùng tạo thành các thẻ ngang bên phải giống như github.
- Star page: hiển thị danh sách server mà người dùng đã star, có thể bỏ star hoặc click vào server để xem chi tiết.
- Setting page: cho phép người dùng thay đổi thông tin cá nhân, mật khẩu, ngôn ngữ, chế độ sáng/tối và các cài đặt khác.
- Login page: cho phép người dùng đăng nhập bằng email và mật khẩu hoặc đăng nhập bằng tài khoản Google.


# Tài liệu API:
## Tài liệu API backend

### Kết nối chung
- Backend mặc định chạy tại `http://localhost:3279`; client nên cấu hình `NUXT_PUBLIC_API_BASE` theo môi trường triển khai.
- Các endpoint bên dưới dùng đường dẫn tương đối với API base URL. Request/response JSON dùng `Content-Type: application/json`, trừ endpoint nhận query params hoặc tải file lên URL ký sẵn.
- Endpoint cần đăng nhập nhận `Authorization: Bearer <accessToken>`. Access token có thời hạn 15 phút.
- Refresh token nằm trong cookie `refreshToken` (`HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`, tối đa 7 ngày theo endpoint). Gửi request refresh/logout kèm cookie trình duyệt (`credentials: 'include'`). Không đọc cookie này từ JavaScript.
- Backend phân quyền theo chủ server, editor, viewer; một số endpoint GET cũng cần quyền viewer/editor. Thiếu đăng nhập hoặc quyền sẽ nhận `401`/`403`.
- Endpoint danh sách nhận `page` (bắt đầu từ 0), `size`, `sort`, ví dụ `?page=0&size=20&sort=starCount,desc`. Response là trang Spring Data, có `content`, `totalElements`, `totalPages`, `number`, `size` và thông tin phân trang.

### Xác thực và tài khoản

| Method | Endpoint | Quyền | Request / kết quả |
|---|---|---|---|
| `POST` | `/auth/register` | Công khai | JSON `{ "username": "...", "email": "...", "password": "..." }`; username 3–20 ký tự, chỉ chữ/số/`_@.`; trả `200` rỗng. |
| `POST` | `/auth/login` | Công khai | JSON `{ "username": "...", "password": "..." }`; `username` nhận username hoặc email; trả `201`, JSON `{ "accessToken": "..." }` và đặt cookie refresh. |
| `POST` | `/auth/refresh` | Cookie refresh | Gửi cookie `refreshToken`; trả `201`, access token mới và refresh cookie mới. |
| `PUT` | `/auth/password` | Đăng nhập | JSON `{ "currentPassword": "...", "newPassword": "..." }` (8–50 ký tự); sai mật khẩu hiện tại hoặc tài khoản chỉ có Google trả `400`. Thu hồi mọi phiên cũ, trả `200` kèm access token mới và refresh cookie mới. |
| `POST` | `/auth/logout` | Đăng nhập | Gửi Bearer token; thu hồi phiên/token và xóa refresh cookie; trả `200` rỗng. |
| `POST` | `/auth/check-username?username=...` | Công khai | Trả boolean JSON: `true` nếu username đã tồn tại. |
| `POST` | `/auth/check-email?email=...` | Công khai | Trả boolean JSON: `true` nếu email đã tồn tại. |
| `GET` | `/users/{username}` | Công khai | Trả `{ username, serverCount, bio, avatarUrl }`. |
| `PATCH` | `/users/{username}` | Chính chủ | JSON tùy chọn `{ "resourceId": "...", "bio": "..." }`; trường bỏ qua thì không đổi, `null` xóa giá trị nullable. Trả `200` rỗng. |
| `PUT` | `/users/{username}` | Chính chủ | JSON `{ "newUsername": "..." }`; đổi username; trả `200` rỗng. |

Đăng nhập OAuth Google do Spring Security xử lý tại `/oauth2/authorization/google`; URL callback phụ thuộc cấu hình môi trường backend.

### Server

`ServerType`: `VANILLA`, `MODPACK`. `ServerPlatform`: `JAVA`, `BEDROCK`.

Các endpoint danh sách server (`/servers`, `/servers/search`, `/servers/{username}`) trả phần tử dạng `ServerSummary`: `{ username, servername, requiresPremium, avatarUrl, bannerUrl, summary, starCount, visibility, serverType, serverPlatform, tags, primaryVersion }`. `username` là chủ server; `tags` gồm tối đa 3 tag đầu tiên; `primaryVersion` là phiên bản chính theo platform của server, có thể `null`.

| Method | Endpoint | Quyền | Request / kết quả |
|---|---|---|---|
| `GET` | `/servers` | Công khai | Bộ lọc tùy chọn `requiresPremium`, `serverType`, `serverPlatform`, `tags` (lặp query param cho nhiều tag); trả trang `ServerSummary`. Mặc định sắp xếp `starCount,desc`. |
| `GET` | `/servers/search?query=...` | Công khai | Tìm kiếm cùng bộ lọc và phân trang như `/servers`; `query` bắt buộc. |
| `GET` | `/servers/{username}/{servername}`| Server công khai hoặc viewer | Trả `{ servername, visibility, requiresPremium, javaAddress, bedrockAddress, avatarUrl, bannerUrl, summary, bio, tags, categories, javaVersions, bedrockVersions, serverType, serverPlatform, starCount }`. |
| `GET` | `/servers/{username}` | Công khai | Trả trang `ServerSummary`; chính chủ thấy cả server riêng tư (`visibility: false`). Mặc định sắp xếp `starCount,desc`. |
| `POST` | `/servers/{username}` | Chính chủ | JSON `{ "servername": "...", "visibility": true, "requiresPremium": false, "type": "VANILLA", "platform": "JAVA", "summary": "..." }`; trả `201` rỗng. Servername tối đa 50 ký tự, summary tối đa 255. |
| `PATCH` | `/servers/{username}/{servername}` | Editor | JSON gồm các trường cần cập nhật: `avatarResourceId`, `bannerResourceId`, `requiresPremium`, `javaAddress`, `bedrockAddress`, `summary`, `bio`. Trả `204`. |
| `PUT` | `/servers/{username}/{servername}` | Chủ server | Query param `newServername`; đổi tên; trả `204`. |
| `PUT` | `/servers/{username}/{servername}/visibility` | Chủ server | JSON `{ "visibility": true }`; trả `204`. |
| `DELETE` | `/servers/{username}/{servername}` | Chủ server | Xóa server; trả `204`. |

Với `JsonNullable`, trường PATCH bị bỏ qua sẽ không đổi; gửi `null` để xóa giá trị nullable. Địa chỉ server là host tùy chọn kèm `:port`.

### Gallery, tag và phiên bản Minecraft

| Method | Endpoint | Quyền | Request / kết quả |
|---|---|---|---|
| `GET` | `/servers/gallery/{username}/{servername}` | Server công khai hoặc viewer | Trang ảnh `{ imageId, imageUrl, imageTitle, orderIndex, createdAt }`; mặc định sắp xếp `orderIndex,asc`, rồi `createdAt,asc`. |
| `GET` | `/servers/gallery/{username}/{servername}/{imageId}` | Server công khai hoặc viewer | Chi tiết ảnh `{ imageId, imageUrl, imageTitle, summary, orderIndex, createdAt }`. |
| `POST` | `/servers/gallery/{username}/{servername}` | Editor | JSON `{ "resourceId": "...", "title": "...", "summary": "...", "orderIndex": 0 }`; title tối đa 64, summary tối đa 255; trả `201`. |
| `PATCH` | `/servers/gallery/{username}/{servername}/{imageId}` | Editor | JSON tùy chọn `{ "resourceId": "...", "title": "...", "summary": "...", "orderIndex": 0 }`; trả `204`. |
| `DELETE` | `/servers/gallery/{username}/{servername}/{imageId}` | Editor | Xóa ảnh; trả `204`. |
| `GET` | `/servers/tags/{username}/{servername}` | Editor | Trả `{ "tags": { "FEATURES": [...], "GAMEPLAY": [...], "META": [...], "COMMUNITY": [...] } }`. |
| `PUT` | `/servers/tags/{username}/{servername}` | Editor | JSON `{ "tags": { "FEATURES": ["..."], "GAMEPLAY": ["..."] } }`; tối đa 20 tag/category, mỗi tag tối đa 50 ký tự và phải thuộc danh mục backend hỗ trợ. Trả `200` rỗng. |
| `GET` | `/servers/versions/java/{username}/{servername}` | Server công khai hoặc viewer | Trả `{ "javaVersions": { "1.20": ["1.20.1"] }, "primaryJavaVersion": "1.20.1" }`. |
| `PUT` | `/servers/versions/java/{username}/{servername}` | Editor | JSON `{ "javaVersions": { "1.20": ["1.20.1"] }, "primaryJavaVersion": "1.20.1" }`; trả `200` rỗng. |
| `GET` | `/servers/versions/bedrock/{username}/{servername}` | Server công khai hoặc viewer | Trả `{ "bedrockVersions": { "...": ["..."] }, "primaryBedrockVersion": "..." }`. |
| `PUT` | `/servers/versions/bedrock/{username}/{servername}` | Editor | JSON `{ "bedrockVersions": { "...": ["..."] }, "primaryBedrockVersion": "..." }`; trả `200` rỗng. |

### Star (yêu thích)

| Method | Endpoint | Quyền | Request / kết quả |
|---|---|---|---|
| `GET` | `/stars/{username}/{servername}` | Editor hoặc chủ server | Trang người đã star `{ username, avatarUrl }`; mặc định `size=20`, sắp xếp `createdAt,asc`. |
| `GET` | `/stars/{username}` | Chính chủ | Trang server user đã star `{ username, servername, serverTotalStar, serverAvatarUrl }`; `username` là **chủ server** (dùng để dựng link `/s/{username}/{servername}`). Server không còn công khai bị loại khỏi danh sách. Mặc định `size=20`, sắp xếp `createdAt,asc`. |
| `PUT` | `/stars/{username}/{servername}` | Đăng nhập và viewer | Thêm star; trả `201` rỗng. |
| `DELETE` | `/stars/{username}/{servername}` | Đăng nhập và viewer | Bỏ star; trả `204`. |

### Tải tài nguyên

| Method | Endpoint | Quyền | Request / kết quả |
|---|---|---|---|
| `GET` | `/storage/upload-url?contentType=...&contentLength=...` | Đăng nhập | Trả `{ "uploadUrl": "...", "resourceId": "..." }`. Tải file trực tiếp lên object storage bằng URL ký sẵn; sau đó dùng `resourceId` trong cập nhật avatar/banner/ảnh. |
| `PUT` | `/storage/webhook/upload-complete` | Webhook nội bộ | Không gọi từ client. Backend yêu cầu header `X-Secret-Key` và JSON `{ "resourceId": "..." }`. |

### Lỗi thường gặp
- `400 Bad Request`: payload/query không hợp lệ, trùng username/email/server, refresh token không hợp lệ, tag hoặc phiên bản không hỗ trợ. Một số lỗi trả chuỗi mô tả; lỗi validate có thể theo định dạng mặc định của Spring.
- `401 Unauthorized`: thiếu hoặc access token hết hạn/không hợp lệ; client có thể gọi `/auth/refresh` rồi thử lại request.
- `403 Forbidden`: đã xác thực nhưng không đủ quyền.
- `404 Not Found`: không tìm thấy user, server, ảnh, tài nguyên hoặc star.
- `5xx`: lỗi server/tải lên; hiển thị thông báo chung và cho phép thử lại khi phù hợp.
