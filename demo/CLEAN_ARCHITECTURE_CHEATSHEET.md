# Clean Architecture Quick Reference

## 🏗️ The 4 Layers

```
┌─────────────────────────────────────────────────────┐
│  PRESENTATION (REST API)                             │
│  Controllers, DTOs, HTTP endpoints                   │
│  Example: ProductController, UserController          │
└────────────────────┬────────────────────────────────┘
                     │ (depends on)
┌────────────────────▼────────────────────────────────┐
│  APPLICATION (Use Cases)                             │
│  Services, DTOs, Business rules                      │
│  Example: ProductService, UserService                │
└────────────────────┬────────────────────────────────┘
                     │ (depends on)
┌────────────────────▼────────────────────────────────┐
│  DOMAIN (Core Business Logic)                        │
│  Entities, Repository Interfaces (NO implementation) │
│  Example: Product (entity), IProductRepository       │
└────────────────────┬────────────────────────────────┘
                     │ (depends on)
┌────────────────────▼────────────────────────────────┐
│  INFRASTRUCTURE (Tools & Implementation)             │
│  JPA entities, Repository implementations, Mappers   │
│  Example: ProductEntity, ProductJpaRepository        │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Folder Structure Per Module

```
modules/
└── product/
    ├── domain/                  ← CORE: Pure business logic
    │   ├── model/
    │   │   └── Product.java      (Pure Java entity, no @Entity)
    │   └── repository/
    │       └── IProductRepository.java (Interface only!)
    │
    ├── application/             ← USE CASES: Business rules
    │   ├── service/
    │   │   └── ProductService.java     (Orchestration, validation)
    │   └── dto/
    │       ├── ProductCreateRequest.java
    │       ├── ProductUpdateRequest.java
    │       └── ProductResponse.java
    │
    ├── infrastructure/          ← TOOLS: Implementation
    │   ├── persistence/
    │   │   ├── ProductEntity.java      (@Entity, database model)
    │   │   └── ProductJpaRepository.java (Spring Data JPA impl)
    │   └── mapper/
    │       └── ProductMapper.java      (Convert: Domain ↔ Entity)
    │
    └── presentation/            ← DELIVERY: Entry points
        └── controller/
            └── ProductController.java  (@RestController, endpoints)
```

---

## 🔑 Key Rules

### Rule 1: Dependencies Flow INWARD
```
❌ WRONG: Domain knows about Presentation
✅ RIGHT: Presentation knows about Domain
```

### Rule 2: Domain is Pure Java
```java
❌ WRONG:
@Entity                          // NO Spring annotations!
public class Product { }

✅ RIGHT:
public class Product {           // Pure Java
    private Long id;
    private String name;
}
```

### Rule 3: Repository is Interface in Domain
```java
❌ WRONG (Domain layer):
public class ProductJpaRepository { }  // Implementation here!

✅ RIGHT (Domain layer):
public interface IProductRepository {  // Interface only
    Product save(Product product);
    Optional<Product> findById(Long id);
}

// Implementation goes in infrastructure layer:
@Repository
public class ProductJpaRepository implements IProductRepository {
    // Implementation here
}
```

### Rule 4: DTOs for API Input/Output
```java
❌ WRONG:
@PostMapping
public Product createProduct(@RequestBody Product product) {
    // API gets access to all Product fields!
}

✅ RIGHT:
@PostMapping
public ProductResponse createProduct(@RequestBody ProductCreateRequest request) {
    // API only sees what we want to expose
}
```

---

## 📝 Layer Checklist

### Domain Layer (business logic)
- [ ] Pure Java classes (no Spring annotations)
- [ ] No @Entity annotations
- [ ] Only interfaces (no implementations)
- [ ] Business rules and validation logic
- [ ] No database queries
- [ ] No HTTP handling

### Application Layer (orchestration)
- [ ] @Service annotation
- [ ] Depends on domain repository interfaces
- [ ] Handles validation
- [ ] Converts domain to/from DTOs
- [ ] No HTTP handling
- [ ] No database queries (delegates to repository)

### Infrastructure Layer (implementation)
- [ ] @Entity and JPA annotations
- [ ] Implements domain repository interfaces
- [ ] @Repository annotation
- [ ] Mappers for domain ↔ entity conversion
- [ ] Database queries and persistence
- [ ] External API calls (payment, email, etc.)

### Presentation Layer (API)
- [ ] @RestController
- [ ] @PostMapping, @GetMapping, etc.
- [ ] Accepts DTOs
- [ ] Returns DTOs
- [ ] No business logic here
- [ ] No database access

---

## 🔄 Request/Response Flow

```
HTTP Request
    │
    ▼
┌──────────────────────────────┐
│ Controller (Presentation)     │ ← Accepts DTO
├──────────────────────────────┤
│ 1. Validate input (optional)  │
│ 2. Call Service              │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Service (Application)         │ ← Business logic
├──────────────────────────────┤
│ 1. Validate business rules    │
│ 2. Create domain entity       │
│ 3. Call repository            │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Repository (Infrastructure)   │ ← Database access
├──────────────────────────────┤
│ 1. Convert domain to entity   │
│ 2. Persist to database        │
└────────────┬─────────────────┘
             │
             ▼
         Database

(Same flow back up for response)
```

---

## 💻 Code Example: Complete Flow

### 1. Controller Layer
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    public ResponseEntity<ProductResponse> create(
        @Valid @RequestBody ProductCreateRequest request
    ) {
        // 1. Receive DTO
        // 2. Call service
        Product product = productService.createProduct(request);
        
        // 3. Convert to response DTO
        ProductResponse response = toResponse(product);
        
        // 4. Return with 201 status
        return ResponseEntity.status(201).body(response);
    }
}
```

### 2. Service Layer (Application)
```java
@Service
public class ProductService {
    
    private final IProductRepository repository;
    
    public Product createProduct(ProductCreateRequest request) {
        // 1. Validate
        if (request.getPrice() <= 0) {
            throw new ValidationException("Price must be > 0");
        }
        
        // 2. Create domain entity
        Product product = Product.create(
            request.getName(),
            request.getPrice(),
            request.getDescription()
        );
        
        // 3. Persist (call repository)
        return repository.save(product);
    }
}
```

### 3. Domain Layer
```java
public class Product {
    
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    
    public static Product create(String name, BigDecimal price, String description) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be > 0");
        }
        
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.description = description;
        return product;
    }
}
```

### 4. Repository Interface (Domain)
```java
public interface IProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
}
```

### 5. Infrastructure Layer
```java
@Component
public class ProductMapper {
    public Product toDomain(ProductEntity entity) {
        // Convert ProductEntity to Product
    }
    
    public ProductEntity toEntity(Product domain) {
        // Convert Product to ProductEntity
    }
}

@Repository
public interface ProductJpaRepository extends 
    JpaRepository<ProductEntity, Long>,
    IProductRepository {
    
    @Override
    default Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private String description;
}
```

---

## 🧪 Testing Strategy by Layer

### Domain Layer Tests
```java
// Test pure logic (no mocking needed)
@Test
void testProductCreation() {
    Product product = Product.create("Laptop", new BigDecimal("999.99"), "Gaming laptop");
    assertEquals("Laptop", product.getName());
}
```

### Service Layer Tests
```java
// Mock repository, test business logic
@Test
void testCreateProduct() {
    when(repository.save(any())).thenReturn(product);
    Product result = service.createProduct(request);
    verify(repository).save(any());
}
```

### Controller Layer Tests
```java
// Mock service, test endpoints
@Test
void testCreateEndpoint() {
    mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(request)))
        .andExpect(status().isCreated());
}
```

---

## 🚫 Common Mistakes to Avoid

| Mistake | Why Bad | Solution |
|---------|---------|----------|
| Using @Entity in domain | Couples to database | Use pure Java |
| Repository impl in domain | Violates DDD | Interface only in domain |
| Service returns Entity | API exposes internals | Return DTOs |
| No validation in service | Invalid data persisted | Validate before save |
| Circular dependencies | Modules tightly coupled | Follow layer rules |
| No tests | Code breaks easily | Test each layer |
| Too much logic in controller | Hard to test | Logic in service |

---

## 📊 Dependency Summary

```
Layer                   Depends On          Imports From
─────────────────────────────────────────────────────────
Presentation            Application ✅       modules.*.application
                        Domain ✅             modules.*.domain

Application             Domain ✅             modules.*.domain
                        Infrastructure ✅    modules.*.infrastructure

Infrastructure          Domain ✅             modules.*.domain
                        
Domain                  Nothing              (pure Java)
```

---

## 🎯 When Stuck

1. **Ask:** Which layer does this code belong in?
2. **Ask:** Am I following the dependency direction?
3. **Ask:** Is domain pure Java (no Spring)?
4. **Ask:** Does service validate before persisting?
5. **Ask:** Does controller convert to/from DTOs?

**If stuck on implementation:** Copy the pattern from User module!

---

**Remember:** Clean Architecture is a habit. The first module (User) takes time, but every module after follows the same pattern. You'll get faster! 🚀

