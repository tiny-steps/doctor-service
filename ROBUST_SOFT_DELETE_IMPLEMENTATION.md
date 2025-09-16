# Robust Soft Delete Functionality for Doctor Service - Implementation Summary

## Overview

This document summarizes the implementation of a comprehensive soft delete system for the Doctor Service that handles branch-specific deactivation. The system allows doctors to be deactivated from specific branches while maintaining their association with other branches, providing fine-grained control over doctor availability.

## Key Features Implemented

### 1. Enhanced Data Model

- **Doctor Entity**: Uses `EntityStatus` enum (ACTIVE/INACTIVE) for global status
- **DoctorAddress Entity**: Uses `Status` enum (ACTIVE/INACTIVE) for branch-specific status
- **Branch-specific relationships**: Each doctor-branch association can be independently managed

### 2. Request/Response DTOs

#### DoctorBranchDeactivationRequestDto

```json
{
  \"branchIds\": [\"uuid1\", \"uuid2\"],
  \"reason\": \"Optional reason for deactivation\",
  \"updateGlobalStatus\": true
}
```

#### DoctorSoftDeleteResponseDto

```json
{
  \"doctorId\": \"doctor-uuid\",
  \"success\": true,
  \"message\": \"Operation completed successfully\",
  \"affectedBranches\": [\"branch-uuid1\", \"branch-uuid2\"],
  \"globalStatusChanged\": true,
  \"newGlobalStatus\": \"INACTIVE\",
  \"remainingActiveBranches\": 2,
  \"totalBranches\": 5,
  \"operationType\": \"BRANCH_SPECIFIC_DEACTIVATION\"
}
```

### 3. Core Service Methods

#### Branch-Specific Operations

- `deactivateDoctorFromBranches(UUID doctorId, DoctorBranchDeactivationRequestDto request)`
- `deactivateDoctorGlobally(UUID doctorId)`
- `activateDoctorInBranch(UUID doctorId, UUID branchId)`
- `getDoctorBranchStatus(UUID doctorId)`

#### Utility Methods

- `isDoctorActiveInAnyBranch(UUID doctorId)`
- `getActiveBranchCount(UUID doctorId)`
- `getActiveBranches(UUID doctorId)`

### 4. REST API Endpoints

#### Enhanced Soft Delete Endpoints

1. **Deactivate Doctor from Specific Branches**

   ```
   PUT /api/v1/doctors/{doctorId}/deactivate-branches
   ```

   - **Body**: `DoctorBranchDeactivationRequestDto`
   - **Response**: `DoctorSoftDeleteResponseDto`
   - **Authority**: `ADMIN`, `BRANCH_MANAGER`

2. **Deactivate Doctor Globally**

   ```
   PUT /api/v1/doctors/{doctorId}/deactivate-global
   ```

   - **Response**: `DoctorSoftDeleteResponseDto`
   - **Authority**: `ADMIN`

3. **Activate Doctor in Branch**

   ```
   PUT /api/v1/doctors/{doctorId}/activate-branch/{branchId}
   ```

   - **Response**: `DoctorSoftDeleteResponseDto`
   - **Authority**: `ADMIN`, `BRANCH_MANAGER`

4. **Get Doctor Branch Status**
   ```
   GET /api/v1/doctors/{doctorId}/branch-status
   ```
   - **Response**: `Map<UUID, Boolean>` (branchId -> isActive)
   - **Authority**: `DOCTOR_OWNER`, `ADMIN`, `BRANCH_MANAGER`

#### Utility Endpoints

5. **Check if Doctor is Active in Any Branch**

   ```
   GET /api/v1/doctors/{doctorId}/active-in-any-branch
   ```

6. **Get Active Branch Count**

   ```
   GET /api/v1/doctors/{doctorId}/active-branch-count
   ```

7. **Get Active Branches**
   ```
   GET /api/v1/doctors/{doctorId}/active-branches
   ```

## Business Logic Implementation

### Scenario 1: Doctor with Single Branch

When a doctor is associated with only one branch:

1. Set `DoctorAddress.status` to `INACTIVE` for that branch
2. Set global `Doctor.status` to `INACTIVE`

**Example:**

```java
// Doctor A associated with Branch X only
deactivateDoctorFromBranches(doctorA, [branchX])
// Result: DoctorAddress(doctorA, branchX).status = INACTIVE
//         Doctor(doctorA).status = INACTIVE
```

### Scenario 2a: Doctor with Multiple Branches - Partial Deactivation

When deactivating from specific branches (not all):

1. Set `DoctorAddress.status` to `INACTIVE` only for selected branches
2. Keep global `Doctor.status` as `ACTIVE`

**Example:**

```java
// Doctor B associated with Branch Y and Branch Z
deactivateDoctorFromBranches(doctorB, [branchY])
// Result: DoctorAddress(doctorB, branchY).status = INACTIVE
//         Doctor(doctorB).status = ACTIVE (still active in Branch Z)
```

### Scenario 2b: Doctor with Multiple Branches - Full Deactivation

When deactivating from all branches:

1. Set `DoctorAddress.status` to `INACTIVE` for all branches
2. Set global `Doctor.status` to `INACTIVE`

**Example:**

```java
// Doctor C associated with Branch A, B, and C
deactivateDoctorFromBranches(doctorC, [branchA, branchB, branchC])
// Result: All DoctorAddress records set to INACTIVE
//         Doctor(doctorC).status = INACTIVE
```

### Scenario 3: Reactivation Logic

When reactivating a doctor:

1. Set specific `DoctorAddress.status` to `ACTIVE` for the target branch
2. If global `Doctor.status` is `INACTIVE`, automatically set it to `ACTIVE`

**Example:**

```java
// Doctor D has global status INACTIVE
activateDoctorInBranch(doctorD, branchX)
// Result: DoctorAddress(doctorD, branchX).status = ACTIVE
//         Doctor(doctorD).status = ACTIVE
```

## Enhanced Repository Methods

### New Query Methods in DoctorAddressRepository

```java
// Find by multiple branch IDs
List<DoctorAddress> findByDoctorIdAndAddressIdIn(UUID doctorId, List<UUID> branchIds);

// Bulk status updates
void updateStatusByDoctorIdAndAddressIdIn(UUID doctorId, List<UUID> branchIds, Status status);

// Active branch queries
long countActiveBranchesByDoctorId(UUID doctorId);
List<UUID> findActiveBranchIdsByDoctorId(UUID doctorId);
boolean hasActiveBranches(UUID doctorId);

// Branch status mapping
List<Object[]> findBranchStatusByDoctorId(UUID doctorId);
```

### Updated Query Methods in DoctorRepository

- Enhanced branch queries to filter by `ACTIVE` status in `DoctorAddress` relationships
- Improved location-based queries to only include active doctor-branch associations

## Validation and Error Handling

### Input Validation

- Branch IDs list cannot be null or empty
- Maximum 50 branches per operation (prevents performance issues)
- Doctor must exist before performing operations
- Doctor must be associated with all specified branches

### Custom Exceptions

- `DoctorSoftDeleteException`: For soft delete-specific validation errors
- `DoctorNotFoundException`: For missing doctor records

### Error Response Examples

```json
{
  \"success\": false,
  \"message\": \"Doctor is not associated with branches: [branch-uuid-3]\",
  \"doctorId\": \"doctor-uuid\"
}
```

## Transaction Management

- All operations are wrapped in `@Transactional` annotations
- Rollback occurs automatically if any part of the operation fails
- Consistent state maintained across doctor and doctor-address updates

## Security Considerations

### Role-Based Access Control

- **ADMIN**: Full access to all operations
- **BRANCH_MANAGER**: Can manage doctors in their assigned branches
- **DOCTOR_OWNER**: Can view their own status information

### Branch Access Validation

- Users can only perform operations on branches they have access to
- Branch access is validated through `@securityService.hasBranchAccess()` expressions

## Performance Optimizations

### Bulk Operations

- Single query for multiple branch updates using `IN` clauses
- Efficient counting queries for branch statistics
- Optimized status checks with boolean queries

### Query Optimizations

- Updated existing queries to include `ACTIVE` status filters
- Reduced unnecessary joins by filtering at the relationship level
- Indexed queries on frequently accessed status combinations

## Testing Strategy

### Unit Tests

- Comprehensive test coverage for all scenarios
- Validation test cases for edge conditions
- DTO structure and mapping tests
- Error handling verification

### Integration Test Scenarios

- Single branch deactivation
- Multiple branch partial deactivation
- Full branch deactivation
- Reactivation workflows
- Security access control

## Backward Compatibility

### Existing Functionality

- All existing soft delete methods remain functional
- Enhanced methods provide additional capabilities without breaking changes
- Existing API endpoints continue to work as before

### Migration Considerations

- New `Status` field in `DoctorAddress` table with proper defaults
- Database migration scripts handle existing data
- Gradual adoption possible through feature flags

## Usage Examples

### Deactivate Doctor from Specific Branches

```bash
curl -X PUT \n  'https://api.tinysteps.com/api/v1/doctors/123e4567-e89b-12d3-a456-426614174000/deactivate-branches' \n  -H 'Authorization: Bearer <token>' \n  -H 'Content-Type: application/json' \n  -d '{
    \"branchIds\": [
      \"550e8400-e29b-41d4-a716-446655440001\",
      \"550e8400-e29b-41d4-a716-446655440002\"
    ],
    \"reason\": \"Doctor requested transfer\",
    \"updateGlobalStatus\": true
  }'
```

### Activate Doctor in Branch

```bash
curl -X PUT \\n  'https://api.tinysteps.com/api/v1/doctors/123e4567-e89b-12d3-a456-426614174000/activate-branch/550e8400-e29b-41d4-a716-446655440001' \\n  -H 'Authorization: Bearer <token>'
```

### Get Doctor Branch Status

```bash
curl -X GET \\n  'https://api.tinysteps.com/api/v1/doctors/123e4567-e89b-12d3-a456-426614174000/branch-status' \\n  -H 'Authorization: Bearer <token>'
```

## Conclusion

The robust soft delete functionality provides a comprehensive solution for managing doctor availability across multiple branches. The implementation maintains data integrity, provides clear audit trails, and offers flexible reactivation paths while ensuring backward compatibility with existing systems.

The system handles all the specified scenarios effectively:

- ✅ Single branch deactivation with global status update
- ✅ Multiple branch partial deactivation with status preservation
- ✅ Full branch deactivation with global status update
- ✅ Intelligent reactivation with automatic global status management
- ✅ Comprehensive validation and error handling
- ✅ Transaction management and rollback capabilities
- ✅ Role-based security and branch access control
- ✅ Performance optimization and query efficiency

This implementation provides the foundation for sophisticated doctor management workflows while maintaining system reliability and data consistency.
