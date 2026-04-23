# Clean Architecture Implementation - Scapes Backend

Dokumentasi implementasi Clean Architecture untuk backend Scapes.

## Overview

Clean Architecture adalah pendekatan desain software yang memisahkan aplikasi menjadi layer-layer independent, sehingga:

- **Testable**: Business logic dapat ditest tanpa UI, database, atau external dependencies
- **Maintainable**: Mudah dipahami dan dimodifikasi
- **Flexible**: Mudah mengganti implementasi dependencies
- **Independent**: Tidak tergantung pada framework atau tool tertentu

---

## Layer Architecture

### 1. Domain Layer (Core/Domain)

**Tanggung jawab**: Mengandung business logic dan entities.

**File/Folder**: `src/Core/Domain/`

**Karakteristik**:
- Independent, tidak bergantung pada layer lain
- Mengandung Entity, ValueObjects, dan Business Rules
- Tidak boleh import dari Infrastructure atau Interfaces

**Contoh**:

```php
// src/Core/Domain/User.php
namespace Scapes\Core\Domain;

class User {
  private int $id;
  private string $email;
  private string $passwordHash;
  private string $role;

  public function __construct(
    int $id,
    string $email,
    string $passwordHash,
    string $role
  ) {
    $this->id = $id;
    $this->email = $email;
    $this->passwordHash = $passwordHash;
    $this->role = $role;
  }

  public function getId(): int {
    return $this->id;
  }

  public function getEmail(): string {
    return $this->email;
  }
}
```

---

### 2. Application Layer (Application/UseCases)

**Tanggung jawab**: Mengandung use cases dan orchestration logic.

**File/Folder**: `src/Application/UseCases/`

**Karakteristik**:
- Mendeskripsikan business scenarios / use cases
- Mengorkestra interaksi antara domain dan infrastructure
- Tidak bergantung pada UI atau framework
- Menggunakan dependency injection

**Contoh**:

```php
// src/Application/UseCases/RegisterUserUseCase.php
namespace Scapes\Application\UseCases;

use Scapes\Core\Domain\User;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Core\Exceptions\ValidationException;

class RegisterUserUseCase {
  private UserRepository $userRepository;

  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Melakukan registrasi user baru.
   *
   * @param string $email Email user.
   * @param string $password Password user (plaintext).
   * @param string $role Role user (contributor atau admin).
   *
   * @return User User yang baru terdaftar.
   * @throws ValidationException Jika validasi gagal.
   */
  public function execute(
    string $email,
    string $password,
    string $role = 'contributor'
  ): User {
    // Validasi email format
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      throw new ValidationException('Email format invalid');
    }

    // Cek jika email sudah terdaftar
    if ($this->userRepository->findByEmail($email) !== null) {
      throw new ValidationException('Email sudah terdaftar');
    }

    // Hash password
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

    // Buat user baru (dummy ID, akan di-assign saat simpan)
    $user = new User(0, $email, $passwordHash, $role);

    // Simpan ke repository
    return $this->userRepository->save($user);
  }
}
```

---

### 3. Infrastructure Layer (Infrastructure)

**Tanggung jawab**: Mengandung implementasi teknis seperti database, cache, API calls, dll.

**File/Folder**: `src/Infrastructure/`

**Sub-folders**:
- `Database/`: Database connections dan queries
- `Repository/`: Data access layer
- `ExternalServices/`: API calls ke layanan eksternal
- `Cache/`: Caching implementation

**Karakteristik**:
- Implementasi detail teknis
- Dapat diganti tanpa mengubah business logic
- Mengimplementasikan interfaces dari Application layer

**Contoh**:

```php
// src/Infrastructure/Repository/UserRepository.php
namespace Scapes\Infrastructure\Repository;

use Scapes\Core\Domain\User;
use Scapes\Infrastructure\Database\DatabaseConnection;

class UserRepository extends BaseRepository {
  protected string $table = 'users';

  /**
   * Mencari user berdasarkan email.
   *
   * @param string $email Email user.
   *
   * @return User|null User jika ditemukan, null jika tidak.
   */
  public function findByEmail(string $email): ?User {
    $query = "SELECT * FROM {$this->table} WHERE email = ?";
    $result = $this->db->query($query, [$email]);
    $data = $result->fetch();

    if (!$data) {
      return null;
    }

    return $this->mapToUser($data);
  }

  /**
   * Menyimpan user ke database.
   *
   * @param User $user User yang akan disimpan.
   *
   * @return User User dengan ID yang sudah di-assign.
   */
  public function save(User $user): User {
    // Implementation untuk insert atau update
  }

  /**
   * Mengkonversi data database menjadi User entity.
   *
   * @param array $data Data dari database.
   *
   * @return User User entity.
   */
  private function mapToUser(array $data): User {
    return new User(
      (int) $data['id'],
      $data['email'],
      $data['password_hash'],
      $data['role']
    );
  }
}
```

---

### 4. Interface Adapters Layer (Interfaces)

**Tanggung jawab**: Mengkonversi data dari/ke format yang sesuai untuk UI, API, database, dll.

**File/Folder**: `src/Interfaces/`

**Sub-folders**:
- `Http/Controllers/`: HTTP controllers
- `Http/Presenters/`: Mengkonversi data untuk HTTP response
- `Console/`: CLI commands jika ada

**Karakteristik**:
- Bridge antara external world dan application layer
- Menangani HTTP requests/responses
- Melakukan input validation dan output formatting

**Contoh**:

```php
// src/Interfaces/Http/Controllers/AuthController.php
namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\RegisterUserUseCase;
use Scapes\Core\Exceptions\ValidationException;

class AuthController {
  private RegisterUserUseCase $registerUserUseCase;

  public function __construct(RegisterUserUseCase $registerUserUseCase) {
    $this->registerUserUseCase = $registerUserUseCase;
  }

  /**
   * Handle registrasi user baru.
   *
   * @return void
   */
  public function register(): void {
    try {
      $email = $_POST['email'] ?? '';
      $password = $_POST['password'] ?? '';

      $user = $this->registerUserUseCase->execute($email, $password);

      // Kirim response
      http_response_code(201);
      echo json_encode([
          'success' => true,
          'data' => [
              'id' => $user->getId(),
              'email' => $user->getEmail(),
          ],
      ]);
    } catch (ValidationException $e) {
      http_response_code(400);
      echo json_encode([
          'success' => false,
          'error' => $e->getMessage(),
      ]);
    }
  }
}
```

---

### 5. Frameworks & Drivers Layer (Config, Public)

**Tanggung jawab**: Framework setup, HTTP server setup, entry points.

**File/Folder**: `config/`, `public/`

**Karakteristik**:
- Entry point aplikasi (public/index.php)
- Framework initialization
- Database connection setup

---

## Dependency Direction

Dalam Clean Architecture, dependencies mengalir ke **inward**, bukan outward:

```
Frameworks & Drivers
        ↑
Interface Adapters (Controllers)
        ↑
Application (UseCases)
        ↑
Domain (Entities, Business Rules)
```

**Aturan**:
- Inner layers tidak boleh bergantung pada outer layers
- Outer layers boleh bergantung pada inner layers
- Communication antar layer melalui interfaces, bukan konkrit implementations

---

## Folder Structure Detail

```
src/backend/
│
├── src/
│   │
│   ├── Core/                          # Business logic & domain
│   │   ├── Domain/                   # Entities
│   │   │   ├── User.php
│   │   │   ├── Wallpaper.php
│   │   │   └── Category.php
│   │   │
│   │   └── Exceptions/               # Custom exceptions
│   │       ├── DatabaseException.php
│   │       ├── ValidationException.php
│   │       └── NotFoundException.php
│   │
│   ├── Application/                   # Use cases & orchestration
│   │   └── UseCases/
│   │       ├── User/
│   │       │   ├── RegisterUserUseCase.php
│   │       │   ├── LoginUserUseCase.php
│   │       │   └── ResetPasswordUseCase.php
│   │       │
│   │       └── Wallpaper/
│   │           ├── UploadWallpaperUseCase.php
│   │           ├── ApproveWallpaperUseCase.php
│   │           └── DeleteWallpaperUseCase.php
│   │
│   ├── Infrastructure/                # Technical implementations
│   │   │
│   │   ├── Database/
│   │   │   ├── DatabaseConnection.php
│   │   │   └── DatabaseConnectionInterface.php
│   │   │
│   │   ├── Repository/
│   │   │   ├── BaseRepository.php
│   │   │   ├── UserRepository.php
│   │   │   └── WallpaperRepository.php
│   │   │
│   │   ├── ExternalServices/
│   │   │   ├── PexelsAPIService.php
│   │   │   └── UnsplashAPIService.php
│   │   │
│   │   └── Persistence/              # Cache, queue, dll
│   │       └── RedisCache.php
│   │
│   └── Interfaces/                    # Interface adapters
│       └── Http/
│           ├── Controllers/
│           │   ├── AuthController.php
│           │   ├── WallpaperController.php
│           │   └── CategoryController.php
│           │
│           └── Presenters/
│               └── UserPresenter.php
│
├── config/                            # Configuration
│   └── bootstrap.php
│
├── public/                            # Web root
│   └── index.php
│
└── storage/                           # Non-code files
    └── logs/
```

---

## Best Practices

### 1. Dependency Injection

Gunakan constructor injection untuk semua dependencies:

```php
// ✓ Good
public function __construct(
  UserRepository $userRepository,
  PasswordHasher $passwordHasher
) {
  $this->userRepository = $userRepository;
  $this->passwordHasher = $passwordHasher;
}

// ✗ Bad
public function register() {
  $userRepository = new UserRepository();  // Tight coupling!
  $user = $userRepository->save($data);
}
```

### 2. Use Interfaces

Define interfaces untuk repository dan services:

```php
// ✓ Good
interface UserRepositoryInterface {
  public function findById(int $id): ?User;
  public function save(User $user): User;
}

class UserRepository implements UserRepositoryInterface {
  // Implementation
}

// Inject interface, bukan konkrit class
public function __construct(UserRepositoryInterface $repository) {
  $this->repository = $repository;
}

// ✗ Bad
public function __construct(UserRepository $repository) {
  // Terikat pada implementasi konkrit
  $this->repository = $repository;
}
```

### 3. Separation of Concerns

Setiap class harus punya satu tanggung jawab:

```php
// ✓ Good - UseCase fokus pada business logic
class RegisterUserUseCase {
  public function execute(string $email, string $password): User {
    // Business logic
  }
}

// ✓ Good - Repository fokus pada data access
class UserRepository {
  public function save(User $user): User {
    // Data access logic
  }
}

// ✗ Bad - Multiple responsibilities
class UserService {
  public function register($data) {
    // Validate
    // Hash password
    // Save to DB
    // Send email
    // Create JWT token
  }
}
```

### 4. Exception Handling

Throw domain exceptions, catch di controller:

```php
// ✓ Good
class RegisterUserUseCase {
  public function execute(string $email, string $password): User {
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      throw new ValidationException('Invalid email');
    }
  }
}

class AuthController {
  public function register() {
    try {
      $user = $this->useCase->execute($_POST['email'], $_POST['password']);
      // Success response
    } catch (ValidationException $e) {
      // Error response
    }
  }
}
```

---

## Testing

Clean Architecture memudahkan testing:

```php
// ✓ Test use case tanpa database
class RegisterUserUseCaseTest {
  public function testRegisterWithValidData() {
    $mockRepository = $this->createMock(UserRepositoryInterface::class);
    $useCase = new RegisterUserUseCase($mockRepository);

    $user = $useCase->execute('test@example.com', 'password123');

    $this->assertNotNull($user->getId());
  }
}
```

---

## References

- Robert C. Martin - Clean Architecture: A Craftsman's Guide to Software Structure and Design
- https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html

---

Untuk pertanyaan lebih lanjut, baca dokumentasi di folder `docs/`
