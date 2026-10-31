# Course Registration Service - Design Specification

## 1. Service Boundary

The Course Registration Service provides enrollment, dropping, and waitlist management for a single course offering. The service is bounded to the following operations:

- **Enroll**: Register a learner for a course if capacity is available and prerequisites are met
- **Drop**: Remove a learner from a course, potentially triggering waitlist promotion
- **Waitlist Management**: Automatically manage FIFO queue when course is at capacity

The service does NOT handle:
- Course catalog management
- Multi-term registration
- Payment processing
- Identity/authentication
- Frontend UI
- Multi-course offering coordination

## 2. Data Model

### 2.1 Course Entity
```java
- id: Long (PK)
- code: String (unique identifier)
- name: String
- prerequisite: String (course code required)
- capacity: Integer (max enrollment)
- enrolledCount: Integer (current enrollment)
```

### 2.2 Learner Entity
```java
- id: Long (PK)
- email: String (unique identifier)
- name: String
```

### 2.3 Enrollment Entity
```java
- id: Long (PK)
- course: Course (FK)
- learner: Learner (FK)
- enrolledAt: Long (timestamp)
```

### 2.4 WaitlistEntry Entity
```java
- id: Long (PK)
- course: Course (FK)
- learner: Learner (FK)
- joinedAt: Long (timestamp)
- position: Integer (FIFO order)
```

## 3. API Design

### 3.1 Enrollment Endpoint
```
POST /api/registration/enroll
Request Body:
{
  "courseCode": "CS201",
  "learnerEmail": "learner@example.com"
}

Response:
{
  "status": "SUCCESS|WAITLISTED|ERROR",
  "message": "description",
  "waitlistPosition": null | Integer
}
```

### 3.2 Drop Endpoint
```
POST /api/registration/drop
Request Body:
{
  "courseCode": "CS201",
  "learnerEmail": "learner@example.com"
}

Response:
{
  "status": "SUCCESS|ERROR",
  "message": "description",
  "waitlistPosition": null
}
```

## 4. Enrollment and Capacity Invariants

1. **Capacity Constraint**: `enrolledCount <= capacity` must always hold
2. **Uniqueness Constraint**: A learner can have at most one enrollment OR one waitlist entry per course
3. **Atomic Updates**: Enrollment count and enrollment record must be updated in a single transaction
4. **Waitlist Position**: Waitlist positions must be sequential starting from 1 with no gaps

## 5. Waitlist Promotion Rule

When a seat opens (via drop operation):
1. Retrieve waitlist entries ordered by position (FIFO)
2. For each waitlisted learner in order:
   - Check prerequisite eligibility
   - If eligible: promote to enrollment, remove from waitlist, reorder remaining entries, STOP
   - If ineligible: remove from waitlist, reorder remaining entries, CONTINUE to next
3. Process continues until either a learner is promoted or waitlist is exhausted

**Key Assumption**: Ineligible learners at the front of the queue are skipped and removed. They must re-request waitlisting if they become eligible later.

## 6. Concurrency and Persistence Approach

### 6.1 Persistence
- **Database**: MySQL for production, H2 for testing
- **ORM**: Spring Data JPA with Hibernate
- **Transaction Management**: `@Transactional` on service methods ensures atomicity
- **Schema Generation**: `ddl-auto=update` for development, `create-drop` for tests

### 6.2 Concurrency
- **Optimistic Locking**: Not implemented (assumes low concurrency for this bounded context)
- **Pessimistic Locking**: Not implemented
- **Transaction Isolation**: Default READ_COMMITTED
- **Tradeoff**: For a production system with high concurrency, database-level locking or optimistic versioning would be needed. This implementation focuses on correctness within a single transaction boundary.

## 7. Prerequisite Validation

### 7.1 Implementation
Prerequisite checking is simplified for this bounded context:
- Learner email must contain `"completed_" + prerequisiteCourseCode`
- Example: Course CS201 requires CS101, learner email "learner1_completed_CS101@example.com" is eligible

### 7.2 Production Consideration
In a real system, this would query a `CompletedCourse` table or external service to verify prerequisite completion.

## 8. Key Assumptions and Tradeoffs

### 8.1 Assumptions
1. **Single Course Offering**: Implementation handles one course at a time
2. **Email-based Prerequisite Check**: Simplified validation based on email pattern
3. **No Concurrent Enrollment**: Assumes sequential requests within transaction boundaries
4. **In-memory Waitlist Reordering**: Waitlist positions are recalculated after each change
5. **No Waitlist Expiration**: Waitlist entries persist indefinitely until promoted or manually removed

### 8.2 Tradeoffs
1. **Simplicity vs. Flexibility**: Fixed prerequisite rule vs. configurable policy engine
2. **Performance vs. Correctness**: In-memory reordering vs. database-level position management
3. **Scope vs. Completeness**: Bounded feature vs. full registration platform
4. **Testing Strategy**: H2 in-memory database vs. MySQL for all tests

### 8.3 Known Limitations
- No support for concurrent enrollment requests (race conditions possible under load)
- No waitlist capacity limit
- No waitlist expiration or cleanup
- No audit trail for enrollment changes
- No batch operations
- Prerequisite check is not realistic for production use

## 9. Error Handling

The service returns clear error messages for:
- Course not found
- Learner not found
- Already enrolled
- Already on waitlist
- Prerequisite not met
- Not enrolled (for drop operation)

All errors return HTTP 400 with a descriptive message in the response body.

## 10. Testing Strategy

Tests cover:
- Successful enrollment with available capacity
- Prerequisite failure
- Waitlist addition when full
- Duplicate enrollment prevention
- Duplicate waitlist prevention
- Ineligible learner waitlist rejection
- Drop with waitlist promotion
- Drop with ineligible waitlist learner (skip behavior)
- Duplicate drop prevention
- FIFO waitlist ordering
- Invalid request handling (course/learner not found)

All tests use H2 in-memory database for isolation and speed.
