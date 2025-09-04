# Branch Context Implementation for Doctor Service

This document describes the branch context implementation added to the Doctor Service, enabling multi-branch support for healthcare organizations.

## Overview

The branch context feature allows doctors to be associated with specific branches of a healthcare organization. This implementation supports:

- **Single Branch Doctors**: Doctors who work at one specific branch
- **Multi-Branch Doctors**: Doctors who work across multiple branches
- **Branch-based Filtering**: Query doctors by their branch association
- **Security Integration**: Branch access control through JWT tokens

## Database Changes

New fields added to the `doctors` table:

```sql
-- Migration V4__add_branch_context_to_doctors.sql
ALTER TABLE doctors 
ADD COLUMN primary_branch_id UUID,
ADD COLUMN is_multi_branch BOOLEAN DEFAULT FALSE;
```

### Field Descriptions

- `primary_branch_id`: The UUID of the primary branch where the doctor is associated
- `is_multi_branch`: Boolean flag indicating if the doctor works across multiple branches

## Entity Changes

### Doctor Entity

Added fields:
```java
@Column(name = "primary_branch_id")
private UUID primaryBranchId;

@Column(name = "is_multi_branch")
private Boolean isMultiBranch = false;
```

### DTOs

#### DoctorRequestDto
```java
private String primaryBranchId;
private Boolean isMultiBranch;
```

#### DoctorResponseDto
```java
private String primaryBranchId;
private Boolean isMultiBranch;
```

## Repository Methods

New query methods added to `DoctorRepository`:

```java
// Find by branch
Page<Doctor> findByPrimaryBranchId(UUID primaryBranchId, Pageable pageable);
List<Doctor> findByPrimaryBranchId(UUID primaryBranchId);

// Find by branch and status
Page<Doctor> findByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status, Pageable pageable);
List<Doctor> findByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status);

// Find multi-branch doctors
Page<Doctor> findByIsMultiBranch(Boolean isMultiBranch, Pageable pageable);
List<Doctor> findByIsMultiBranch(Boolean isMultiBranch);

// Find by branch and verification status
List<Doctor> findByPrimaryBranchIdAndIsVerified(UUID primaryBranchId, Boolean isVerified);

// Count methods
long countByPrimaryBranchId(UUID primaryBranchId);
long countByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status);
```

## Service Methods

New methods added to `DoctorService`:

```java
// Count methods
long countByBranch(UUID branchId);
long countByBranchAndStatus(UUID branchId, String status);

// Find methods
Page<DoctorResponseDto> findByBranch(UUID branchId, Pageable pageable);
List<DoctorResponseDto> findByBranch(UUID branchId);

Page<DoctorResponseDto> findByBranchAndStatus(UUID branchId, String status, Pageable pageable);
List<DoctorResponseDto> findByBranchAndStatus(UUID branchId, String status);

List<DoctorResponseDto> findByBranchAndVerificationStatus(UUID branchId, Boolean isVerified);

Page<DoctorResponseDto> findMultiBranchDoctors(Pageable pageable);
List<DoctorResponseDto> findMultiBranchDoctors();

Page<DoctorResponseDto> findDoctorsByCurrentUserBranch(Pageable pageable);
List<DoctorResponseDto> findDoctorsByCurrentUserBranch();
```

## REST API Endpoints

New endpoints added to `DoctorController`:

### Get Doctors by Branch
```http
GET /api/doctors/branch/{branchId}?page=0&size=10
```
Returns paginated list of doctors for a specific branch.

### Get Doctors by Branch and Status
```http
GET /api/doctors/branch/{branchId}/status/{status}?page=0&size=10
```
Returns paginated list of doctors for a specific branch and status.

### Get Doctors by Branch and Verification Status
```http
GET /api/doctors/branch/{branchId}/verification/{isVerified}
```
Returns list of doctors for a specific branch and verification status.

### Get Multi-Branch Doctors
```http
GET /api/doctors/multi-branch?page=0&size=10
```
Returns paginated list of doctors who work across multiple branches.

### Get Doctors from Current User's Branch
```http
GET /api/doctors/my-branch?page=0&size=10
```
Returns paginated list of doctors from the current user's primary branch.

### Statistics Endpoints

#### Count Doctors by Branch
```http
GET /api/doctors/statistics/count/branch/{branchId}
```
Returns count of doctors in a specific branch.

#### Count Doctors by Branch and Status
```http
GET /api/doctors/statistics/count/branch/{branchId}/status/{status}
```
Returns count of doctors in a specific branch with a specific status.

## Security Integration

The implementation integrates with the existing `SecurityService` to:

- Get the current user's primary branch ID from JWT tokens
- Validate branch access permissions
- Filter results based on user's branch context

### JWT Claims Used

- `primaryBranchId`: User's primary branch ID
- `branchIds`: List of branch IDs the user has access to

## Usage Examples

### Creating a Single-Branch Doctor

```json
POST /api/doctors
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Dr. John Doe",
  "slug": "dr-john-doe",
  "gender": "MALE",
  "status": "ACTIVE",
  "primaryBranchId": "456e7890-e89b-12d3-a456-426614174001",
  "isMultiBranch": false
}
```

### Creating a Multi-Branch Doctor

```json
POST /api/doctors
{
  "userId": "123e4567-e89b-12d3-a456-426614174002",
  "name": "Dr. Jane Smith",
  "slug": "dr-jane-smith",
  "gender": "FEMALE",
  "status": "ACTIVE",
  "primaryBranchId": "456e7890-e89b-12d3-a456-426614174001",
  "isMultiBranch": true
}
```

### Querying Doctors by Branch

```http
GET /api/doctors/branch/456e7890-e89b-12d3-a456-426614174001?page=0&size=20
```

## Database Indexes

For optimal query performance, the following indexes are created:

```sql
CREATE INDEX idx_doctors_primary_branch_id ON doctors(primary_branch_id);
CREATE INDEX idx_doctors_is_multi_branch ON doctors(is_multi_branch);
CREATE INDEX idx_doctors_primary_branch_status ON doctors(primary_branch_id, status);
CREATE INDEX idx_doctors_primary_branch_verified ON doctors(primary_branch_id, is_verified);
```

## Testing

Comprehensive integration tests are provided in `BranchContextIntegrationTest.java` covering:

- Creating doctors with branch context
- Querying doctors by branch
- Filtering by branch and status
- Multi-branch doctor functionality
- Count operations
- Security integration

## Migration Guide

To apply the branch context changes:

1. Run the database migration: `V4__add_branch_context_to_doctors.sql`
2. Update existing doctor records to set appropriate branch values
3. Deploy the updated service
4. Update client applications to use new branch-based endpoints

## Future Enhancements

Potential future improvements:

- Branch hierarchy support
- Doctor availability by branch
- Branch-specific pricing
- Cross-branch appointment scheduling
- Branch performance analytics