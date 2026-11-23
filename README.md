# 🌐 InteractHub Backend (Java Spring Boot 3.x)

## 🛠️ Công nghệ Sử dụng (Tech Stack)

| Hạng mục                  | Công nghệ                                      |    Phiên bản     |
| :------------------------ | :--------------------------------------------- | :--------------: |
| **Ngôn ngữ**              | Java (LTS)                                     |       `21`       |
| **Framework chính**       | Spring Boot                                    |     `3.3.6`      |
| **ORM / Data Access**     | Spring Data JPA (Hibernate)                    |     `3.3.6`      |
| **Cơ sở dữ liệu**         | PostgreSQL                                     |      `16+`       |
| **Xác thực & Bảo mật**    | Spring Security + JJWT (HmacSHA256)            | `6.x` / `0.12.6` |
| **Mã hóa mật khẩu**       | BCrypt Password Encoder                        |        -         |
| **Lưu trữ Media**         | Cloudinary Java SDK                            |     `1.38.0`     |
| **Tài liệu API**          | Springdoc OpenAPI (Swagger UI)                 |     `2.7.0`      |
| **Validation**            | Jakarta Bean Validation (`jakarta.validation`) |        -         |
| **Boilerplate Reduction** | Project Lombok                                 |     `1.18.x`     |
| **Testing**               | JUnit 5 + Mockito + Spring Boot Test           |        -         |
| **Công cụ Build**         | Gradle (Kotlin DSL) & Apache Maven song song   |        -         |

---

## 📁 Cấu trúc Thư mục Dự án

```
interacthub-backend/
├── build.gradle.kts                  # Cấu hình build Gradle (Kotlin DSL)
├── pom.xml                           # Cấu hình build Maven (dùng để xác minh độc lập)
├── settings.gradle.kts               # Cấu hình tên project Gradle
├── README.md                         # Tài liệu hướng dẫn này
└── src/
    ├── main/
    │   ├── java/com/interacthub/
    │   │   ├── InteractHubApplication.java  # Main Application Runner
    │   │   ├── config/               # Cấu hình Security, Cloudinary, CORS, DataSeeder
    │   │   │   ├── CloudinaryConfig.java
    │   │   │   ├── CorsConfig.java
    │   │   │   ├── DataSeeder.java   # Tự động seed tài khoản Admin khi khởi chạy
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/           # Tầng REST Controllers (11 controllers)
    │   │   │   ├── BaseController.java        # Helper trích xuất currentUserId từ Token
    │   │   │   ├── AuthController.java        # Đăng ký, Đăng nhập
    │   │   │   ├── PostController.java        # CRUD bài viết, Feed phân trang, Repost, Report
    │   │   │   ├── CommentController.java     # Bình luận gốc & Reply phân cấp
    │   │   │   ├── ProfileController.java     # Hồ sơ cá nhân, đổi mật khẩu, avatar, follow
    │   │   │   ├── InteractionController.java # Like toggle & Comment nhanh
    │   │   │   ├── FriendController.java      # Gửi/nhận lời mời, danh sách bạn bè
    │   │   │   ├── ChatController.java        # Danh sách hội thoại, lịch sử tin nhắn
    │   │   │   ├── StoryController.java       # Story 24h (global & bạn bè)
    │   │   │   ├── NotificationController.java# Xem & đánh dấu đã đọc thông báo
    │   │   │   ├── HashtagController.java     # Tra cứu bài viết theo hashtag
    │   │   │   └── ModerationController.java  # Admin kiểm duyệt bài viết vi phạm
    │   │   ├── dto/                  # Data Transfer Objects (Records & Lombok)
    │   │   │   ├── auth/             # LoginRequest, RegisterRequest, AuthResponse
    │   │   │   ├── chat/             # ConversationDto, MessageDto,...
    │   │   │   ├── comment/          # CommentRequest, RecommentRequest, CommentResponse
    │   │   │   ├── common/           # ApiResponse, PagedResponse
    │   │   │   ├── friend/           # FriendDto
    │   │   │   ├── interaction/      # LikeRequest, InteractionResponse
    │   │   │   ├── media/            # MediaUploadResultDto
    │   │   │   ├── moderation/       # ReportedPostSummaryDto, PostReportDetailDto
    │   │   │   ├── notification/     # NotificationResponse
    │   │   │   ├── post/             # PostResponse, PostDetailResponse, RepostRequest,...
    │   │   │   ├── profile/          # ProfileResponse, UpdateProfileRequest,...
    │   │   │   └── story/            # StoryResponse, CreateStoryRequest
    │   │   ├── entity/               # JPA Entities (14 entities, 3 enums, 3 embeddables)
    │   │   │   ├── User.java
    │   │   │   ├── Post.java
    │   │   │   ├── Comment.java
    │   │   │   ├── Like.java & LikeId.java
    │   │   │   ├── Conversation.java
    │   │   │   ├── Friendship.java & FriendshipId.java
    │   │   │   ├── Message.java
    │   │   │   ├── Notification.java
    │   │   │   ├── Hashtag.java
    │   │   │   ├── PostHashtag.java & PostHashtagId.java
    │   │   │   ├── PostMedia.java
    │   │   │   ├── PostReport.java
    │   │   │   ├── Story.java
    │   │   │   └── enums/            # MediaType, FriendshipStatus, NotificationType
    │   │   ├── exception/            # Xử lý ngoại lệ toàn cục
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice chuẩn hóa JSON lỗi
    │   │   ├── repository/           # Spring Data JPA Repositories (13 interfaces)
    │   │   ├── security/             # JWT & Custom UserDetails
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── CustomUserDetails.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   └── service/              # Nghiệp vụ hệ thống (14 services)
    │   │       ├── AuthService.java
    │   │       ├── PostService.java
    │   │       ├── CommentService.java
    │   │       ├── ProfileService.java
    │   │       ├── FriendService.java
    │   │       ├── ChatService.java
    │   │       ├── StoryService.java
    │   │       ├── InteractionService.java
    │   │       ├── NotificationService.java
    │   │       ├── HashtagService.java
    │   │       ├── CloudinaryService.java
    │   │       ├── ReportService.java
    │   │       ├── StoryCleanupService.java   # @Scheduled dọn story hết hạn mỗi 1 giờ
    │   │       └── PresenceService.java
    │   └── resources/
    │       ├── application.yml       # Cấu hình chính thức
    │       └── application-dev.yml   # Cấu hình môi trường dev (show-sql)
    └── test/
        └── java/com/interacthub/
            └── service/
                └── AuthServiceTest.java       # Unit Test với JUnit 5 & Mockito
```

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy (Getting Started)

### 1. Yêu cầu Tiên quyết (Prerequisites)

- **JDK 21** trở lên ([Eclipse Adoptium Temurin 21](https://adoptium.net/))
- **PostgreSQL 14+** đang chạy trên máy hoặc qua Docker
- **Maven 3.9+** hoặc **Gradle 8.5+**

### 2. Cấu hình Cơ sở Dữ liệu & Môi trường

Tạo cơ sở dữ liệu PostgreSQL:

```sql
CREATE DATABASE interacthub;
```

Cập nhật thông số kết nối trong [`src/main/resources/application.yml`](src/main/resources/application.yml) hoặc khai báo qua biến môi trường:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/interacthub
    username: postgres
    password: ${DB_PASSWORD:postgres}

app:
  jwt:
    secret: ${JWT_SECRET:defaultSecretKeyThatShouldBeChangedInProductionAndMustBeLongEnoughForHS256Algorithm}
    expiration-ms: 86400000 # 24 giờ
  cloudinary:
    cloud-name: ${CLOUDINARY_CLOUD_NAME:demo}
    api-key: ${CLOUDINARY_API_KEY:key}
    api-secret: ${CLOUDINARY_API_SECRET:secret}
  seed:
    admin-email: admin@interacthub.com
    admin-password: ${ADMIN_PASSWORD:Admin@123}
```

> [!NOTE]
> Khi ứng dụng khởi động lần đầu, `DataSeeder` sẽ tự động tạo tài khoản quản trị viên:
>
> - **Email:** `admin@interacthub.com`
> - **Mật khẩu:** `Admin@123`
> - **Role:** `ROLE_ADMIN`

### 3. Biên dịch và Khởi chạy Ứng dụng

**Sử dụng Maven:**

```bash
# Biên dịch
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run
```

**Sử dụng Gradle (nếu máy đã cài Gradle):**

```bash
# Biên dịch
gradle build -x test

# Chạy ứng dụng
gradle bootRun
```

Ứng dụng sẽ khởi động tại cổng mặc định: `http://localhost:8080`.

### 4. Tài liệu Swagger UI / OpenAPI

Sau khi chạy ứng dụng, truy cập giao diện tương tác API tại:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📡 Danh mục API Endpoints Chính

Tất cả các endpoint (trừ Auth và Swagger) đều yêu cầu Header: `Authorization: Bearer <JWT_TOKEN>`.

### 1. Xác thực (`/api/auth`)

| Method | Endpoint             | Mô tả                              | Quyền  |
| :----: | :------------------- | :--------------------------------- | :----: |
| `POST` | `/api/auth/register` | Đăng ký tài khoản người dùng mới   | Public |
| `POST` | `/api/auth/login`    | Đăng nhập và nhận JWT Bearer Token | Public |

### 2. Bài viết (`/api/post`)

|  Method  | Endpoint                    | Mô tả                                                          | Quyền |
| :------: | :-------------------------- | :------------------------------------------------------------- | :---: |
|  `GET`   | `/api/post/feed`            | Lấy bảng tin bài viết phân trang (Cursor-based)                | User  |
|  `GET`   | `/api/post/{postId}`        | Chi tiết bài viết kèm danh sách bình luận đệ quy               | User  |
|  `POST`  | `/api/post`                 | Đăng bài viết mới (hỗ trợ `multipart/form-data` kèm ảnh/video) | User  |
| `DELETE` | `/api/post/{postId}`        | Xóa bài viết cá nhân                                           | User  |
|  `POST`  | `/api/post/{postId}/repost` | Chia sẻ lại (Repost - giới hạn 1 cấp lồng nhau)                | User  |
|  `POST`  | `/api/post/{postId}/report` | Báo cáo vi phạm bài viết                                       | User  |

### 3. Bình luận (`/api/comments`)

| Method | Endpoint                         | Mô tả                                        | Quyền |
| :----: | :------------------------------- | :------------------------------------------- | :---: |
| `GET`  | `/api/comments/post/{postId}`    | Danh sách cây bình luận của bài viết         | User  |
| `POST` | `/api/comments`                  | Thêm bình luận gốc vào bài viết              | User  |
| `POST` | `/api/comments/{parentId}/reply` | Trả lời bình luận (giới hạn phân cấp 1 tầng) | User  |

### 4. Hồ sơ Cá nhân (`/api/profile`)

| Method | Endpoint                          | Mô tả                                          | Quyền |
| :----: | :-------------------------------- | :--------------------------------------------- | :---: |
| `GET`  | `/api/profile/me`                 | Xem thông tin tài khoản hiện tại               | User  |
| `GET`  | `/api/profile/{userId}`           | Xem trang cá nhân người dùng khác              | User  |
| `PUT`  | `/api/profile/me`                 | Cập nhật thông tin tiểu sử, ngày sinh, địa chỉ | User  |
| `POST` | `/api/profile/change-password`    | Đổi mật khẩu cá nhân                           | User  |
| `POST` | `/api/profile/upload-avatar`      | Upload ảnh đại diện lên Cloudinary             | User  |
| `POST` | `/api/profile/{userId}/follow`    | Bật/tắt theo dõi (Follow / Unfollow toggle)    | User  |
| `GET`  | `/api/profile/{userId}/followers` | Danh sách người đang theo dõi                  | User  |
| `GET`  | `/api/profile/{userId}/following` | Danh sách người mình đang theo dõi             | User  |

### 5. Tương tác Nhanh (`/api/interaction`)

| Method | Endpoint                   | Mô tả                                     | Quyền |
| :----: | :------------------------- | :---------------------------------------- | :---: |
| `POST` | `/api/interaction/like`    | Thả / Bỏ tim bài viết (Idempotent toggle) | User  |
| `POST` | `/api/interaction/comment` | Bình luận nhanh bài viết                  | User  |

### 6. Bạn bè (`/api/friends`)

| Method | Endpoint                         | Mô tả                             | Quyền |
| :----: | :------------------------------- | :-------------------------------- | :---: |
| `POST` | `/api/friends/request/{userId}`  | Gửi lời mời kết bạn               | User  |
| `POST` | `/api/friends/accept/{senderId}` | Chấp nhận lời mời kết bạn         | User  |
| `POST` | `/api/friends/reject/{senderId}` | Từ chối lời mời kết bạn           | User  |
| `POST` | `/api/friends/cancel/{userId}`   | Hủy lời mời kết bạn đã gửi        | User  |
| `POST` | `/api/friends/unfriend/{userId}` | Hủy quan hệ bạn bè                | User  |
| `GET`  | `/api/friends/list`              | Danh sách bạn bè hiện tại         | User  |
| `GET`  | `/api/friends/requests`          | Danh sách lời mời kết bạn đã nhận | User  |

### 7. Trò chuyện (`/api/chat`)

| Method | Endpoint                                | Mô tả                                              | Quyền |
| :----: | :-------------------------------------- | :------------------------------------------------- | :---: |
| `GET`  | `/api/chat/conversations`               | Danh sách các cuộc trò chuyện gần nhất             | User  |
| `POST` | `/api/chat/conversations`               | Tạo mới hoặc mở cuộc trò chuyện 1-1                | User  |
| `GET`  | `/api/chat/conversations/{id}/messages` | Lấy lịch sử tin nhắn (phân trang lùi theo cursor)  | User  |
| `PUT`  | `/api/chat/conversations/{id}/read`     | Đánh dấu tất cả tin nhắn trong hội thoại là đã đọc | User  |

### 8. Tin 24 Giờ (`/api/story`)

|  Method  | Endpoint               | Mô tả                                          | Quyền |
| :------: | :--------------------- | :--------------------------------------------- | :---: |
|  `GET`   | `/api/story/global`    | Danh sách tin công khai còn hiệu lực 24h       | User  |
|  `GET`   | `/api/story/local`     | Danh sách tin từ bạn bè/following còn hiệu lực | User  |
|  `POST`  | `/api/story`           | Đăng story kèm ảnh/video và caption            | User  |
| `DELETE` | `/api/story/{storyId}` | Xóa story cá nhân                              | User  |

### 9. Thông báo (`/api/notification`)

|  Method  | Endpoint                      | Mô tả                               | Quyền |
| :------: | :---------------------------- | :---------------------------------- | :---: |
|  `GET`   | `/api/notification`           | Danh sách thông báo mới nhất        | User  |
| `PATCH`  | `/api/notification/{id}/read` | Đánh dấu thông báo cụ thể là đã đọc | User  |
| `PATCH`  | `/api/notification/read-all`  | Đánh dấu tất cả thông báo là đã đọc | User  |
| `DELETE` | `/api/notification/{id}`      | Xóa thông báo                       | User  |

### 10. Hashtag (`/api/hashtag`)

| Method | Endpoint                       | Mô tả                               | Quyền |
| :----: | :----------------------------- | :---------------------------------- | :---: |
| `GET`  | `/api/hashtag/{hashtag}/posts` | Lọc danh sách bài viết theo hashtag | User  |

### 11. Kiểm duyệt Quản trị (`/api/admin/moderation`)

|  Method  | Endpoint                                       | Mô tả                                         | Quyền |
| :------: | :--------------------------------------------- | :-------------------------------------------- | :---: |
|  `GET`   | `/api/admin/moderation/reported-posts`         | Thống kê danh sách bài viết bị báo cáo        | Admin |
|  `GET`   | `/api/admin/moderation/posts/{postId}/reports` | Xem chi tiết lý do và người báo cáo bài viết  | Admin |
|  `POST`  | `/api/admin/moderation/posts/{postId}/approve` | Duyệt xử phạt và xóa bài viết vi phạm         | Admin |
| `DELETE` | `/api/admin/moderation/posts/{postId}/reports` | Hủy bỏ các báo cáo cho bài viết không vi phạm | Admin |

---

## 🧪 Kiểm thử (Testing)

Dự án áp dụng kiểm thử đơn vị với **JUnit 5** và **Mockito**:

```bash
mvn test
```

Kết quả kiểm thử lớp dịch vụ xác thực:

```text
[INFO] Running com.interacthub.service.AuthServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.061 s
[INFO] BUILD SUCCESS
```

---

## 🗺️ Lộ trình Chuyển đổi (Migration Roadmap)

- [x] **Phase 1: Core Foundation & Basic CRUD**
  - Khởi tạo kiến trúc phân tầng Spring Boot 3.x với Java 21.
  - Chuyển đổi toàn diện 14 Entities sang JPA/Hibernate, hỗ trợ PostgreSQL.
  - Triển khai 14 Services, 13 JpaRepositories và 11 REST Controllers.
  - Hoàn thiện 100% biên dịch sạch và kiểm thử đơn vị cơ bản.
- [ ] **Phase 2: Security & Authentication**
  - Làm mịn luồng JWT (Refresh Token, Token Revocation, Blacklist).
  - Phân quyền động chi tiết (Method Security `@PreAuthorize`).
  - Thêm bộ lọc giới hạn tần suất request (Rate Limiting Filter / Bucket4j).
- [ ] **Phase 3: Real-time Communication & Caching**
  - Thay thế SignalR bằng **Spring WebSocket + STOMP** (`/topic`, `/queue`).
  - Tích hợp **Spring Data Redis**: Cache bài viết hot, Redis Set lưu Presence (Online/Offline) và Blacklist bài viết.
  - Tích hợp Redis Streams & Consumer Container thay thế Worker microservice.
- [ ] **Phase 4: Graph Database & Production Deployment**
  - Tích hợp **Spring Data Neo4j**: Quản lý đồ thị quan hệ bạn bè, thuật toán gợi ý bài viết (3-hop Cypher traversal).
  - Cấu hình Dockerfile đa tầng & Docker Compose (Spring Boot + PostgreSQL + Redis + Neo4j).
