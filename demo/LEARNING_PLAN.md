# Shopee Clone - Complete Learning & Coding Plan

**Project:** E-commerce Monolith (Clean Architecture + Spring Modulith)  
**Framework:** Spring Boot 4.0.3 | Java 21 | MySQL 8.0+  
**Architecture:** Clean Architecture with Domain-Driven Design  
**Current Status:** ✅ Structure Ready | ⏳ Implementation Starting

---

## 📚 PART 1: FOUNDATION CONCEPTS (Learn First!)

Before you write code, understand these concepts:

### Week 1: Core Concepts

#### Day 1-2: Clean Architecture
**What to Learn:**
- Layers: Presentation → Application → Domain → Infrastructure
- Dependencies flow INWARD (outer layers depend on inner)
- Domain layer = pure business logic (no Spring annotations)
- Each layer has specific responsibilities

**Resource:**
```
Domain (Core)
    ↑ (depends on)
Application (Use Cases)
    ↑ (depends on)
Infrastructure (Tools)
    ↑ (depends on)
Presentation (Controllers)
```

#### Day 3: Domain-Driven Design (DDD)
**What to Learn:**
- Entities: Objects with identity (User, Product, Order)
- Value Objects: Objects without identity (Money, Address)
- Aggregates: Collections of entities/value objects
- Repositories: Collections abstraction (interface = domain, impl = infrastructure)

#### Day 4: Spring Modulith
**What to Learn:**
- Module independence: Each module can work alone
- API boundaries: Each module has public API
- No circular dependencies
- Testing modules in isolation

#### Day 5: REST API Design
**What to Learn:**
- HTTP methods: GET, POST, PUT, DELETE, PATCH
- Status codes: 200, 201, 400, 404, 500
- JSON request/response format
- Pagination and filtering

---

## 📋 PART 2: PROJECT STRUCTURE GUIDE

### Your Current Structure (Already Created!)
```
com.example.shopee/
├── common/                    # Shared utilities
│   ├── exception/
│   │   ├── ApplicationException.java     ✅ Created
│   │   ├── EntityNotFoundException.java  ✅ Created
│   │   └── ValidationException.java      ✅ Created
│   └── util/
│       ├── DateUtils.java               ✅ Created
│       └── StringUtils.java             ✅ Created
│
├── config/                    # Global configurations
│   └── SwaggerConfig.java               ✅ Created
│
├── modules/                   # Business logic (8 modules)
│   ├── product/
│   │   ├── domain/
│   │   │   ├── model/         Product.java (Pure Entity)
│   │   │   └── repository/    IProductRepository.java (Interface)
│   │   ├── application/
│   │   │   ├── service/       ProductService.java
│   │   │   └── dto/           ProductRequest/Response DTO
│   │   ├── infrastructure/
│   │   │   ├── persistence/   ProductJpaRepository, ProductEntity
│   │   │   └── mapper/        ProductMapper (Domain ↔ DB)
│   │   └── presentation/
│   │       └── controller/    ProductController
│   │
│   ├── user/                  ← Most Important First!
│   ├── cart/
│   ├── cartitem/
│   ├── inventory/
│   ├── payment/
│   ├── review/
│   └── shop/
│
└── ShopeeApplication.java     ✅ Created
```

---

## 🎯 PART 3: STEP-BY-STEP CODING PLAN

### Phase 1: Foundation (Week 1-2)
**Goal:** Build core infrastructure that all modules will use

#### Step 1.1: Base Entity Class (Day 1)
**What:** Abstract class all domain entities extend
**File:** `common/base/BaseEntity.java`
**Concepts:** 
- Inheritance
- JPA annotations (@MappedSuperclass)
- Common fields (id, createdAt, updatedAt)

**Code Structure:**
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // getters
}
```

**Why:** Avoid duplicating id/timestamp in every entity

---

#### Step 1.2: Global Exception Handler (Day 2)
**What:** Handle all exceptions and return proper HTTP responses
**File:** `config/GlobalExceptionHandler.java`
**Concepts:**
- @ControllerAdvice (Spring annotation)
- @ExceptionHandler (catches exceptions)
- ResponseEntity (HTTP response)

**Code Structure:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            404
        );
        return ResponseEntity.status(404).body(error);
    }
    
    // Handle other exceptions...
}
```

**Why:** Consistent error responses across all APIs

---

#### Step 1.3: Base Service Class (Day 3)
**What:** Abstract service with common CRUD operations
**File:** `common/service/BaseService.java`
**Concepts:**
- Generic class <T, ID>
- Template method pattern
- Spring @Service annotation

**Code Structure:**
```java
public abstract class BaseService<T, ID> {
    
    protected abstract IRepository<T, ID> getRepository();
    
    public T create(T entity) {
        return getRepository().save(entity);
    }
    
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }
    
    public List<T> findAll() {
        return getRepository().findAll();
    }
    
    public void delete(ID id) {
        getRepository().deleteById(id);
    }
}
```

**Why:** Avoid duplicating basic CRUD in every service

---

#### Step 1.4: Base Repository Interface (Day 4)
**What:** Common repository operations
**File:** `common/repository/IRepository.java`
**Concepts:**
- Generics
- Interface design
- Repository pattern

**Code Structure:**
```java
public interface IRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
}
```

**Why:** Abstraction layer between domain and database

---

#### Step 1.5: Database Configuration (Day 5)
**What:** Configure database connection, JPA settings
**Files:** 
- `application.properties` (DONE ✅)
- `application-dev.properties` (DONE ✅)
- `application-prod.properties` (DONE ✅)

**Verify:**
```bash
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

### Phase 2: User Module (Week 3)
**Goal:** Complete one full module end-to-end (template for others)

#### Step 2.1: User Domain (Day 1)
**File:** `modules/user/domain/model/User.java`
**Concepts:**
- Domain entity (NO @Entity here!)
- Pure Java business logic
- Value objects (Email, Password wrapped in classes)

**What to Code:**
```java
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    
    // Constructor, getters
    public static User create(String email, String name, String hashedPassword) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.password = hashedPassword;
        user.createdAt = LocalDateTime.now();
        return user;
    }
}
```

**Why:** Separates business logic from database concerns

---

#### Step 2.2: User Repository Interface (Day 1)
**File:** `modules/user/domain/repository/IUserRepository.java`
**Concepts:**
- Contract/interface
- Repository pattern
- Query methods

**What to Code:**
```java
public interface IUserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void delete(User user);
    boolean existsByEmail(String email);
}
```

**Why:** Domain layer doesn't know about database implementation

---

#### Step 2.3: User JPA Entity (Day 2)
**File:** `modules/user/infrastructure/persistence/UserEntity.java`
**Concepts:**
- JPA annotations (@Entity, @Column, @Table)
- Persistence model (database model)
- Different from domain model!

**What to Code:**
```java
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    // Getters, setters (use Lombok!)
}
```

**Why:** Database columns and constraints defined here

---

#### Step 2.4: User Mapper (Day 2)
**File:** `modules/user/infrastructure/mapper/UserMapper.java`
**Concepts:**
- Conversion between domain and persistence models
- Separation of concerns
- Reusable mapper

**What to Code:**
```java
@Component
public class UserMapper {
    
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        // ... map other fields
        return user;
    }
    
    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        // ... map other fields
        return entity;
    }
}
```

**Why:** Keep domain model separate from database model

---

#### Step 2.5: User JPA Repository (Day 3)
**File:** `modules/user/infrastructure/persistence/UserJpaRepository.java`
**Concepts:**
- Spring Data JPA interface
- Extends JpaRepository
- Implements domain repository interface

**What to Code:**
```java
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, IUserRepository {
    
    @Override
    Optional<UserEntity> findByEmail(String email);
    
    @Override
    boolean existsByEmail(String email);
}
```

**Why:** Spring automatically implements this, very clean!

---

#### Step 2.6: User Service (Day 3)
**File:** `modules/user/application/service/UserService.java`
**Concepts:**
- Business logic orchestration
- Use cases (create, read, update, delete)
- Validation before persistence

**What to Code:**
```java
@Service
public class UserService {
    
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    
    @Autowired
    public UserService(IUserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    public User createUser(UserCreateRequest request) {
        // Validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        
        // Create domain entity
        User user = User.create(
            request.getEmail(),
            request.getName(),
            encodePassword(request.getPassword())
        );
        
        // Persist
        return userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User", id.toString()));
    }
}
```

**Why:** All business logic in one place, easy to test

---

#### Step 2.7: User DTOs (Day 4)
**File:** `modules/user/application/dto/`
**Concepts:**
- Transfer objects for request/response
- Input validation
- Separates API contract from internal model

**What to Code:**
```java
// UserCreateRequest.java
public class UserCreateRequest {
    @NotBlank(message = "Email required")
    @Email(message = "Invalid email")
    private String email;
    
    @NotBlank(message = "Name required")
    private String name;
    
    @NotBlank(message = "Password required")
    @Length(min = 8, message = "Min 8 characters")
    private String password;
    
    // getters
}

// UserResponse.java
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    
    // getters
}
```

**Why:** API clients don't see internal fields (like password hash)

---

#### Step 2.8: User Controller (Day 4)
**File:** `modules/user/presentation/controller/UserController.java`
**Concepts:**
- REST endpoints (@RestController, @PostMapping, @GetMapping)
- Request/response handling
- HTTP status codes

**What to Code:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    
    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request);
        UserResponse response = toResponse(user);
        return ResponseEntity.status(201).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = toResponse(user);
        return ResponseEntity.ok(response);
    }
    
    private UserResponse toResponse(User user) {
        // Map domain to response DTO
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
```

**Why:** Entry point for HTTP requests, converts to/from DTOs

---

#### Step 2.9: Unit Tests (Day 5)
**File:** `src/test/java/.../modules/user/application/service/UserServiceTest.java`
**Concepts:**
- JUnit 5
- Mockito (mock dependencies)
- Test different scenarios

**What to Code:**
```java
@DisplayName("User Service Tests")
class UserServiceTest {
    
    private UserService userService;
    private IUserRepository userRepository;
    private UserMapper userMapper;
    
    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class);
        userMapper = mock(UserMapper.class);
        userService = new UserService(userRepository, userMapper);
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("user@example.com");
        request.setName("John");
        request.setPassword("password123");
        
        // When
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        User result = userService.createUser(request);
        
        // Then
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw error if email exists")
    void testCreateUserEmailExists() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("existing@example.com");
        
        // When
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // Then
        assertThrows(ValidationException.class, () -> userService.createUser(request));
    }
}
```

**Why:** Ensure code works before integration

---

### Phase 3: Product Module (Week 4)
**Goal:** Replicate User module pattern for Product

**Tasks (Same Pattern as User):**
1. Domain model: Product.java
2. Domain repository: IProductRepository.java
3. JPA entity: ProductEntity.java
4. Mapper: ProductMapper.java
5. JPA repository: ProductJpaRepository.java
6. Service: ProductService.java
7. DTOs: ProductCreateRequest, ProductResponse.java
8. Controller: ProductController.java
9. Tests: ProductServiceTest.java

**Key Differences from User:**
- Product has: name, description, price, category
- No password/encryption
- May have images/attachments (File handling)
- Stock quantity integration with Inventory

---

### Phase 4: Cart Module (Week 5)
**Goal:** Implement shopping cart (more complex - relationships)

**Key Concepts:**
- One-to-Many: User has many Carts
- Many-to-Many: Cart has many Products (through CartItem)
- Aggregate: Cart + CartItems form aggregate

**Tasks:**
1. Cart domain entity + repository
2. CartItem domain entity + repository
3. Cart/CartItem persistence entities
4. Mappers
5. Service (addToCart, removeFromCart, getCart)
6. Controller
7. Tests

---

### Phase 5: Inventory Module (Week 6)
**Goal:** Manage product stock

**Key Concepts:**
- Updates when product added/removed from cart
- Cross-module communication (cart → inventory)

---

### Phase 6: Order Module (Week 7)
**Goal:** Checkout and order creation

**Key Concepts:**
- Complex aggregate (Order + OrderItems)
- Cart → Order conversion
- Order status management (PENDING, CONFIRMED, SHIPPED, DELIVERED)

---

### Phase 7: Payment Module (Week 8)
**Goal:** Payment processing

**Key Concepts:**
- Integration with payment gateway (Stripe API)
- Payment status tracking

---

### Phase 8: Other Modules (Weeks 9-10)
- Review Module
- Shop Module

---

## 💡 LEARNING PATTERNS

### For Each Module, Follow This Pattern:

```
1. DOMAIN LAYER (Pure business logic)
   ├── Create model (entity class)
   └── Create repository interface

2. INFRASTRUCTURE LAYER (Database)
   ├── Create JPA entity
   ├── Create JPA repository
   ├── Create mapper

3. APPLICATION LAYER (Use cases)
   ├── Create DTOs (request/response)
   └── Create service

4. PRESENTATION LAYER (API)
   └── Create controller

5. TESTS
   ├── Service tests
   ├── Controller tests (integration)
```

---

## 🚀 WEEK-BY-WEEK ROADMAP

| Week | Focus | Status |
|------|-------|--------|
| Week 1-2 | Foundation (Base classes, config) | ⏳ Starting |
| Week 3 | User Module | ⏳ Next |
| Week 4 | Product Module | ⏳ After User |
| Week 5 | Cart Module | ⏳ After Product |
| Week 6 | Inventory Module | ⏳ After Cart |
| Week 7 | Order Module | ⏳ After Inventory |
| Week 8 | Payment Module | ⏳ After Order |
| Week 9-10 | Review + Shop + Polish | ⏳ Final |

---

## 📖 KEY CONCEPTS TO MASTER

### Layer Responsibilities

**Domain Layer:**
- ✅ Pure Java classes (no Spring)
- ✅ Business logic
- ✅ Interfaces only (no implementations)
- ✅ No database knowledge

**Application Layer:**
- ✅ Use cases/services
- ✅ DTOs for input/output
- ✅ Validation rules
- ✅ Orchestration

**Infrastructure Layer:**
- ✅ JPA entities (@Entity)
- ✅ Repositories (implementations)
- ✅ Database queries
- ✅ External APIs (payment, etc.)

**Presentation Layer:**
- ✅ REST controllers
- ✅ HTTP handling
- ✅ Request/response conversion
- ✅ API documentation

---

## 🧪 TESTING STRATEGY

For each module:
1. **Unit Tests:** Test service logic with mocked repository
2. **Repository Tests:** Test queries with actual DB (H2 in test)
3. **Controller Tests:** Test endpoints with MockMvc
4. **Integration Tests:** Test full flow

---

## 🎓 LEARNING RESOURCES

### Concepts
- Clean Architecture: Robert C. Martin's book
- DDD: Eric Evans' Domain-Driven Design
- Spring Boot: Official Spring docs
- REST: RESTful API design principles

### Practice
- Build one module completely
- Write tests FIRST (TDD)
- Refactor after understanding

---

## ✅ SUCCESS CRITERIA

**Module Complete When:**
- ✅ All 5 layers implemented
- ✅ All tests passing
- ✅ Can run API endpoints
- ✅ Follows clean architecture rules
- ✅ No circular dependencies
- ✅ Documented with Swagger

---

**Start Here:** 
1. Create `BaseEntity.java` (Step 1.1)
2. Create `GlobalExceptionHandler.java` (Step 1.2)
3. Run application and verify it starts

Ready to code? Let's go! 🚀

