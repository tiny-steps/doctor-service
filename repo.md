# Doctor Service - Comprehensive Documentation

## 🏥 Overview

The Doctor Service is a comprehensive microservice that manages all doctor-related information in the Tiny Steps healthcare platform. It handles doctor profiles, qualifications, awards, pricing, practices, and more. This service is built using Spring Boot 3.5.4 with Java 21 and PostgreSQL as the database.

## 🚀 Quick Start

### Prerequisites
- Java 21
- PostgreSQL 15+
- Maven 3.8+
- Running Eureka Service Registry (port 8761)
- Running Auth Service (port 8081)

### Running the Service
```bash
cd doctor-service
mvn spring-boot:run
```
The service will start on port **8084**.

## 🏗️ Architecture & Dependencies

### Core Dependencies

| Dependency | Purpose | Real-world Usage |
|------------|---------|------------------|
| **Spring Boot Starter Web** | REST API framework | Handles HTTP requests from mobile apps and web clients |
| **Spring Boot Starter Data JPA** | Database operations | Manages doctor data persistence and relationships |
| **PostgreSQL Driver** | Database connectivity | Stores doctor profiles, qualifications, awards, etc. |
| **Spring Boot Starter Security** | Authentication & Authorization | Ensures only authorized users can modify doctor data |
| **Spring Boot Starter OAuth2 Resource Server** | JWT token validation | Validates tokens issued by Auth Service |
| **Spring Cloud Netflix Eureka Client** | Service discovery | Allows other services to find this service automatically |
| **Flyway** | Database migrations | Manages database schema changes safely |
| **MapStruct** | Object mapping | Converts between DTOs and entities efficiently |
| **Resilience4j** | Circuit breaker, retry, timeout | Handles failures when calling other services gracefully |
| **SpringDoc OpenAPI** | API documentation | Generates interactive API documentation |

### Integration Dependencies

The service integrates with three external services:

1. **User Service** - Validates doctor user accounts
2. **Address Service** - Manages practice locations
3. **Session Service** - Handles session type pricing

## 🔐 Security Configuration

### Authentication & Authorization

The service uses **JWT-based OAuth2 authentication** with role-based access control:

- **JWT Issuer**: Auth Service (http://localhost:8081)
- **Resource Server**: Validates tokens without storing sessions
- **Roles**: `USER`, `DOCTOR`, `ADMIN`

### Security Rules

| Operation | Required Permission | Example |
|-----------|-------------------|---------|
| Create doctor profile | `USER` role | Any authenticated user can become a doctor |
| View doctor profiles | Public access | Patients can browse doctor profiles |
| Update own profile | Owner or `ADMIN` | Dr. Smith can only edit his own profile |
| Admin operations | `ADMIN` role | Only admins can verify doctors |

### Security Implementation

The `ApplicationSecurityConfig` class implements ownership-based security:

```java
// Example: Only the doctor owner or admin can update awards
@PreAuthorize("@doctorSecurity.isAwardOwner(authentication, #id) or hasRole('ADMIN')")
```

**Real-world scenario**: When Dr. Smith tries to update his award, the system checks if the JWT token's user ID matches the doctor's user ID.

## 🗄️ Database Schema

### Core Tables Overview

The service manages 11 interconnected tables:

```
doctors (main profile)
├── doctor_awards (achievements)
├── doctor_qualifications (education)
├── doctor_memberships (professional memberships)
├── doctor_organizations (work history)
├── doctor_registrations (licenses)
├── doctor_session_pricing (consultation fees)
├── doctor_specializations (medical specialties)
├── doctor_photos (profile images)
├── doctor_practices (clinic locations)
└── recommendations (reviews & ratings)
```

### Detailed Table Documentation

#### 1. `doctors` - Main Doctor Profile

| Column | Type | Purpose | Real-world Example |
|--------|------|---------|-------------------|
| `id` | UUID | Unique doctor identifier | `550e8400-e29b-41d4-a716-446655440000` |
| `user_id` | UUID | Links to User Service | References user account in auth system |
| `name` | VARCHAR(200) | Doctor's full name | "Dr. Sarah Johnson" |
| `slug` | VARCHAR(200) | URL-friendly identifier | "dr-sarah-johnson" (for profile URLs) |
| `gender` | VARCHAR(10) | Gender information | "FEMALE" (for patient preferences) |
| `summary` | TEXT | Brief professional summary | "Experienced cardiologist specializing in heart surgery" |
| `about` | TEXT | Detailed biography | Full professional background and approach |
| `image_url` | VARCHAR(255) | Profile photo URL | Link to doctor's professional headshot |
| `experience_years` | INT | Years of practice | 15 (helps patients choose experienced doctors) |
| `is_verified` | BOOLEAN | Admin verification status | true (shows verified badge to patients) |
| `rating_average` | DECIMAL(3,2) | Average patient rating | 4.75 (out of 5.0) |
| `review_count` | INT | Total number of reviews | 127 (builds trust with patients) |
| `status` | VARCHAR(20) | Account status | "ACTIVE", "INACTIVE", "SUSPENDED" |

**Real-world usage**: When a patient searches for cardiologists, they see Dr. Sarah Johnson with 4.75 stars from 127 reviews, 15 years experience, and a verified badge.

#### 2. `doctor_awards` - Professional Recognition

| Column | Purpose | Example |
|--------|---------|---------|
| `doctor_id` | Links to doctor | References Dr. Johnson's profile |
| `title` | Award name | "Best Cardiologist of the Year 2023" |
| `awarded_year` | Year received | 2023 |
| `summary` | Award description | "Recognized for excellence in patient care" |

**Real-world usage**: Patients see Dr. Johnson's awards, building confidence in her expertise.

#### 3. `doctor_qualifications` - Educational Background

| Column | Purpose | Example |
|--------|---------|---------|
| `qualification_name` | Degree/certification | "MD in Cardiology" |
| `college_name` | Educational institution | "Harvard Medical School" |
| `completion_year` | Graduation year | 2008 |

**Real-world usage**: Patients verify doctor's educational credentials before booking appointments.

#### 4. `doctor_session_pricing` - Consultation Fees

| Column | Purpose | Example |
|--------|---------|---------|
| `session_type_id` | Type of consultation | Video call, In-person, Phone |
| `custom_price` | Doctor's fee | $150.00 for video consultation |
| `is_active` | Available for booking | true |

**Real-world usage**: Patients see "Video Consultation: $150" when booking with Dr. Johnson.

#### 5. `doctor_practices` - Clinic Locations

| Column | Purpose | Example |
|--------|---------|---------|
| `practice_name` | Clinic/hospital name | "City Heart Center" |
| `practice_type` | Type of facility | "HOSPITAL" |
| `address_id` | Location reference | Links to Address Service |
| `practice_position` | Display order | 1 (primary practice) |

**Real-world usage**: Patients see "Dr. Johnson practices at City Heart Center, 123 Main St, New York".

## 🌐 API Endpoints Documentation

### 1. Doctor Management (`/api/v1/doctors`)

#### Core CRUD Operations

| Method | Endpoint | Purpose | Who Can Use | Example Usage |
|--------|----------|---------|-------------|---------------|
| `POST` | `/api/v1/doctors` | Create doctor profile | Authenticated users | Medical professional creates their profile |
| `GET` | `/api/v1/doctors/{id}` | Get doctor by ID | Anyone | Patient views doctor's profile |
| `GET` | `/api/v1/doctors` | List all doctors | Anyone | Patient browses available doctors |
| `PUT` | `/api/v1/doctors/{id}` | Update doctor profile | Owner or Admin | Doctor updates their bio |
| `PATCH` | `/api/v1/doctors/{id}` | Partial update | Owner or Admin | Doctor updates only their photo |
| `DELETE` | `/api/v1/doctors/{id}` | Delete profile | Owner or Admin | Doctor deactivates their account |

#### Search & Discovery

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `GET` | `/api/v1/doctors/search` | Advanced search | Patient searches: "cardiologists in NYC with 4+ stars" |
| `GET` | `/api/v1/doctors/search/name?name=John` | Search by name | Patient looks for "Dr. John" |
| `GET` | `/api/v1/doctors/speciality/Cardiology` | Find by specialty | Patient needs a heart specialist |
| `GET` | `/api/v1/doctors/location/{addressId}` | Find by location | Patient wants nearby doctors |
| `GET` | `/api/v1/doctors/top-rated` | Get top-rated doctors | App shows "Top Doctors" section |

#### Verification & Status

| Method | Endpoint | Purpose | Who Can Use | Example Usage |
|--------|----------|---------|-------------|---------------|
| `POST` | `/api/v1/doctors/{id}/verify` | Verify doctor | Admin only | Admin verifies doctor's credentials |
| `POST` | `/api/v1/doctors/{id}/activate` | Activate account | Admin only | Admin reactivates suspended doctor |
| `GET` | `/api/v1/doctors/{id}/verified` | Check verification | Anyone | App shows verification badge |

#### Profile Analytics

| Method | Endpoint | Purpose | Who Can Use | Example Usage |
|--------|----------|---------|-------------|---------------|
| `GET` | `/api/v1/doctors/{id}/profile-completeness` | Check profile completion | Owner or Admin | "Your profile is 75% complete" |
| `GET` | `/api/v1/doctors/{id}/missing-fields` | Get missing fields | Owner or Admin | "Add qualifications to improve profile" |

### 2. Awards Management (`/api/v1/awards`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/awards/doctor/{doctorId}` | Add award | Doctor adds "Best Doctor 2023" award |
| `GET` | `/api/v1/awards/doctor/{doctorId}` | Get doctor's awards | Patient sees all of Dr. Smith's awards |
| `PUT` | `/api/v1/awards/{id}` | Update award | Doctor corrects award year |
| `DELETE` | `/api/v1/awards/{id}` | Remove award | Doctor removes outdated award |

### 3. Qualifications Management (`/api/v1/qualifications`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/qualifications/doctor/{doctorId}` | Add qualification | Doctor adds "MD from Harvard" |
| `GET` | `/api/v1/qualifications/doctor/{doctorId}` | Get qualifications | Patient verifies doctor's education |
| `PUT` | `/api/v1/qualifications/{id}` | Update qualification | Doctor updates graduation year |

### 4. Pricing Management (`/api/v1/pricing`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/pricing/doctor/{doctorId}` | Set consultation fee | Doctor sets video call price to $150 |
| `GET` | `/api/v1/pricing/doctor/{doctorId}` | Get doctor's pricing | Patient sees "Video: $150, In-person: $200" |
| `PUT` | `/api/v1/pricing/{id}` | Update pricing | Doctor increases consultation fee |

### 5. Practice Locations (`/api/v1/practices`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/practices/doctor/{doctorId}` | Add practice location | Doctor adds new clinic location |
| `GET` | `/api/v1/practices/doctor/{doctorId}` | Get practice locations | Patient sees where doctor practices |
| `PUT` | `/api/v1/practices/{id}` | Update practice info | Doctor updates clinic hours |

### 6. Specializations (`/api/v1/specializations`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/specializations/doctor/{doctorId}` | Add specialty | Doctor adds "Pediatric Cardiology" |
| `GET` | `/api/v1/specializations/doctor/{doctorId}` | Get specialties | Patient sees doctor specializes in children's hearts |

### 7. Professional Memberships (`/api/v1/memberships`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/memberships/doctor/{doctorId}` | Add membership | Doctor adds "American Medical Association" |
| `GET` | `/api/v1/memberships/doctor/{doctorId}` | Get memberships | Patient sees professional affiliations |

### 8. Work History (`/api/v1/organizations`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/organizations/doctor/{doctorId}` | Add work experience | Doctor adds "Chief of Cardiology at City Hospital" |
| `GET` | `/api/v1/organizations/doctor/{doctorId}` | Get work history | Patient sees doctor's career progression |

### 9. Medical Licenses (`/api/v1/registrations`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/registrations/doctor/{doctorId}` | Add license | Doctor adds medical license number |
| `GET` | `/api/v1/registrations/doctor/{doctorId}` | Get licenses | Patient verifies doctor is licensed |

### 10. Photo Gallery (`/api/v1/photos`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/photos/doctor/{doctorId}` | Upload photo | Doctor uploads clinic photos |
| `GET` | `/api/v1/photos/doctor/{doctorId}` | Get photos | Patient sees doctor's clinic environment |

### 11. Reviews & Recommendations (`/api/v1/recommendations`)

| Method | Endpoint | Purpose | Example Usage |
|--------|----------|---------|---------------|
| `POST` | `/api/v1/recommendations/doctor/{doctorId}` | Add review | Patient leaves 5-star review |
| `GET` | `/api/v1/recommendations/doctor/{doctorId}` | Get reviews | New patient reads existing reviews |

## 🔄 Real-World Usage Scenarios

### Scenario 1: New Doctor Registration
1. **Dr. Emily Chen** creates account in User Service with role "DOCTOR"
2. She calls `POST /api/v1/doctors` to create her profile
3. Adds qualifications: `POST /api/v1/qualifications/doctor/{id}`
4. Sets consultation pricing: `POST /api/v1/pricing/doctor/{id}`
5. Adds practice location: `POST /api/v1/practices/doctor/{id}`
6. Profile completeness shows 80% - she needs to add specializations

### Scenario 2: Patient Finding a Doctor
1. **Sarah (patient)** searches: `GET /api/v1/doctors/search?speciality=Cardiology&minRating=4.0`
2. Views Dr. Chen's profile: `GET /api/v1/doctors/{id}`
3. Checks qualifications: `GET /api/v1/qualifications/doctor/{id}`
4. Sees pricing: `GET /api/v1/pricing/doctor/{id}` - "$150 for video consultation"
5. Checks location: `GET /api/v1/practices/doctor/{id}` - "Downtown Medical Center"
6. Reads reviews: `GET /api/v1/recommendations/doctor/{id}`

### Scenario 3: Admin Verification Process
1. **Admin** reviews new doctor applications
2. Checks profile completeness: `GET /api/v1/doctors/{id}/profile-completeness`
3. Verifies credentials and calls: `POST /api/v1/doctors/{id}/verify`
4. Doctor now shows "Verified" badge to patients

### Scenario 4: Doctor Profile Management
1. **Dr. Chen** logs in and sees: `GET /api/v1/doctors/{id}/missing-fields`
2. System suggests: "Add awards and memberships to improve profile"
3. She adds recent award: `POST /api/v1/awards/doctor/{id}`
4. Updates consultation fee: `PUT /api/v1/pricing/{id}`
5. Profile completeness increases to 95%

## 🛡️ Error Handling & Resilience

### Circuit Breaker Pattern
The service uses Resilience4j for handling external service failures:

- **User Service Integration**: 50% failure rate triggers circuit breaker
- **Address Service Integration**: 3 retry attempts with 1-second delay
- **Session Service Integration**: 10-second timeout for requests

### Common Error Responses

| Status Code | Error Type | Example |
|-------------|------------|---------|
| 400 | Bad Request | Invalid doctor data format |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Trying to edit another doctor's profile |
| 404 | Not Found | Doctor ID doesn't exist |
| 409 | Conflict | Doctor profile already exists for user |
| 500 | Internal Error | Database connection failure |

## 📊 Performance & Monitoring

### Database Optimization
- **Indexes**: Created on frequently queried fields (name, speciality, rating)
- **Pagination**: All list endpoints support pagination to handle large datasets
- **Lazy Loading**: Related entities loaded only when needed

### Caching Strategy
- **Profile Data**: Cached for 5 minutes to reduce database load
- **Search Results**: Cached for 2 minutes for popular searches
- **Static Data**: Awards, qualifications cached for 1 hour

## 🚀 Deployment & Configuration

### Environment Variables

| Variable | Purpose | Example |
|----------|---------|---------|
| `SERVER_PORT` | Service port | 8084 |
| `SPRING_DATASOURCE_URL` | Database connection | `jdbc:postgresql://localhost:5432/doctors` |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | Service registry | `http://localhost:8761/eureka/` |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Auth service | `http://localhost:8081` |

### Health Checks
- **Actuator Endpoints**: `/actuator/health`, `/actuator/info`
- **Database Health**: Monitors PostgreSQL connection
- **External Service Health**: Checks User, Address, and Session services

## 📈 Future Enhancements

### Planned Features
1. **AI-Powered Matching**: Match patients with best-suited doctors
2. **Telemedicine Integration**: Direct video consultation booking
3. **Multi-language Support**: Doctor profiles in multiple languages
4. **Advanced Analytics**: Doctor performance metrics and insights
5. **Mobile App Integration**: Push notifications for profile updates

### Scalability Considerations
- **Database Sharding**: Split data by geographic regions
- **Microservice Decomposition**: Separate search service for better performance
- **CDN Integration**: Serve doctor photos from content delivery network
- **Event-Driven Architecture**: Use message queues for profile updates

## 🤝 Contributing

### Development Setup
1. Clone repository
2. Install Java 21 and PostgreSQL
3. Run `mvn clean install`
4. Start dependent services (Eureka, Auth Service)
5. Run `mvn spring-boot:run`

### API Testing
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **Postman Collection**: Import `doctor-service-api-collection.json`
- **Integration Tests**: Run `mvn test`

---

## 📞 Support

For technical support or questions about the Doctor Service:
- **Documentation**: This README and Swagger UI
- **API Testing**: Use provided Postman collection
- **Database Schema**: Check Flyway migration files in `src/main/resources/db/migration/`

---

*This documentation covers all aspects of the Doctor Service, from basic usage to advanced configuration. Each endpoint and table serves a specific purpose in creating a comprehensive doctor management system for the Tiny Steps healthcare platform.*