# 🚀 START HERE - First Steps to Code

**Estimated Time:** 2-3 hours  
**Goal:** Get the foundation working before tackling modules

---

## ✅ Phase 1: Foundation Setup (Do This First!)

### Step 1: Create Base Entity Class (15 min)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\common\base\BaseEntity.java`

**Why:** All entities will inherit from this (id, createdAt, updatedAt)

**The Code:**
```java
package com.example.shopee.common.base;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**What You'll Learn:**
- @MappedSuperclass - Hibernate inheritance
- @GeneratedValue - Auto-increment ID
- Lombok @Data annotation
- LocalDateTime usage

---

### Step 2: Create Response/Error Objects (15 min)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\config\ApiResponse.java`

**Why:** All API responses will use this format (consistent)

**The Code:**
```java
package com.example.shopee.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String errorCode;
    
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message("Success")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

**What You'll Learn:**
- Builder pattern (@Builder)
- Generic types <T>
- JSON serialization customization

---

### Step 3: Create Global Exception Handler (20 min)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\config\GlobalExceptionHandler.java`

**Why:** Catch ALL exceptions and return proper HTTP responses

**The Code:**
```java
package com.example.shopee.config;

import com.example.shopee.common.exception.ApplicationException;
import com.example.shopee.common.exception.EntityNotFoundException;
import com.example.shopee.common.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(
        EntityNotFoundException ex, 
        WebRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
        ValidationException ex, 
        WebRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, 
        WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", "VALIDATION_ERROR"));
    }
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleApplicationException(
        ApplicationException ex, 
        WebRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
        Exception ex, 
        WebRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
    }
}
```

**What You'll Learn:**
- @ControllerAdvice - Global exception handling
- @ExceptionHandler - Catch specific exceptions
- HttpStatus codes
- Validation error handling

---

### Step 4: Verify It Works (10 min)
```bash
cd D:\demo\demo

# Build project
mvnw clean compile

# Run application
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Open browser
# http://localhost:8080/api/swagger-ui.html
```

**Expected Output:**
```
Started ShopeeApplication in X.XXX seconds
Tomcat started on port(s): 8080 with context path '/api'
```

---

## 🎯 Next: Start the USER Module (This Is Your Template!)

### Step 5: User Domain Model (Day 1)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\modules\user\domain\model\User.java`

**The Code:**
```java
package com.example.shopee.modules.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain Model - Pure business logic
 * No Spring annotations, no @Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method for creating new users
     */
    public static User create(String email, String name, String hashedPassword) {
        return User.builder()
            .email(email)
            .name(name)
            .password(hashedPassword)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
```

**What You'll Learn:**
- Domain model = pure Java
- Factory methods (create user without exposing constructor)
- No database details here

---

### Step 6: User Repository Interface (Day 1)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\modules\user\domain\repository\IUserRepository.java`

**The Code:**
```java
package com.example.shopee.modules.user.domain.repository;

import com.example.shopee.modules.user.domain.model.User;
import java.util.Optional;
import java.util.List;

/**
 * Domain Repository Interface
 * No implementation here! Just the contract.
 */
public interface IUserRepository {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByEmail(String email);
    
    List<User> findAll();
    
    void delete(User user);
    
    boolean existsByEmail(String email);
}
```

**What You'll Learn:**
- Repository pattern (abstract data access)
- Interface-based design
- Optional for nullable returns

---

### Step 7: User JPA Entity (Day 2)
**File:** `D:\demo\demo\src\main\java\com\example\shopee\modules\user\infrastructure\persistence\UserEntity.java`

**The Code:**
```java
package com.example.shopee.modules.user.infrastructure.persistence;

import com.example.shopee.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserEntity extends BaseEntity {
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 20)
    private String phone;
}
```

**What You'll Learn:**
- @Entity - JPA annotation
- @Table - Database table mapping
- Column constraints (unique, nullable, length)
- EqualsAndHashCode with callSuper

---

### Step 8: Continue With Mapper, Repository, Service...

Follow the pattern from the LEARNING_PLAN.md document!

---

## 📋 Quick Checklist for First Run

Before running, make sure:

- [ ] BaseEntity.java created
- [ ] ApiResponse.java created
- [ ] GlobalExceptionHandler.java created
- [ ] User domain model created
- [ ] User repository interface created
- [ ] User JPA entity created
- [ ] MySQL database created: `clone_shopee_dev`
- [ ] application-dev.properties has correct database URL
- [ ] Java 21 installed
- [ ] Maven works: `mvnw --version`

---

## 🧪 First Test Command

```bash
# Build and run tests
mvnw clean test

# You should see:
# BUILD SUCCESS (after fixing any errors)
```

---

## 📚 File Structure So Far

```
src/main/java/com/example/shopee/
├── ShopeeApplication.java           ✅ Done
├── common/
│   ├── exception/
│   │   ├── ApplicationException.java        ✅ Done
│   │   ├── EntityNotFoundException.java     ✅ Done
│   │   └── ValidationException.java         ✅ Done
│   ├── base/
│   │   └── BaseEntity.java                  ⏳ Create Now!
│   └── util/
│       ├── DateUtils.java           ✅ Done
│       └── StringUtils.java         ✅ Done
│
├── config/
│   ├── SwaggerConfig.java           ✅ Done
│   ├── ApiResponse.java             ⏳ Create Now!
│   └── GlobalExceptionHandler.java  ⏳ Create Now!
│
└── modules/
    └── user/
        ├── domain/
        │   ├── model/
        │   │   └── User.java        ⏳ Create Next!
        │   └── repository/
        │       └── IUserRepository.java  ⏳ Create Next!
        └── infrastructure/
            └── persistence/
                └── UserEntity.java  ⏳ Create Next!
```

---

## 🎬 Action Items

**Today (Right Now!):**
1. ✅ Create BaseEntity.java
2. ✅ Create ApiResponse.java
3. ✅ Create GlobalExceptionHandler.java
4. ✅ Test application starts

**Tomorrow:**
1. Create User domain model
2. Create User repository interface
3. Create User JPA entity
4. Create User mapper
5. Create User JPA repository

**This Week:**
- Complete User module (8 days total)
- All CRUD endpoints working
- Tests passing

---

**Ready? Start with Step 1: Create BaseEntity.java** 

Questions? Check CLEAN_ARCHITECTURE_CHEATSHEET.md or LEARNING_PLAN.md!

Let's build! 🚀

