# Course Registration Service

A Spring Boot service implementing course enrollment, dropping, and FIFO waitlist management for a single course offering.

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher (for production database)

## Database Setup

### MySQL Configuration

1. Create a MySQL database named `courseregistration`:
```sql
CREATE DATABASE courseregistration;
```

2. Update database credentials in `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/courseregistration
spring.datasource.username=root
spring.datasource.password=tiger
```

## Project Structure

```
course-registration/
├── src/
│   ├── main/
│   │   ├── java/com/assessment/courseregistration/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── dto/                 # Request/Response objects
│   │   │   ├── model/               # JPA entities
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── service/             # Business logic
│   │   │   └── CourseRegistrationApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql             # Sample data initialization
│   └── test/
│       ├── java/com/assessment/courseregistration/
│       │   └── service/             # Service layer tests
│       └── resources/
│           └── application-test.properties
├── pom.xml
├── DESIGN_SPECIFICATION.md
├── AI_USAGE_RECORD.md
└── README.md
```

## Running the Application

### Using Maven

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will start on port 8085.

### Using the Maven Wrapper

1. Build the project:
```bash
./mvnw clean install
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

## Running Tests

### Run All Tests

```bash
mvn test
```

Or using the Maven wrapper:
```bash
./mvnw test
```

### Run Specific Test Class

```bash
mvn test -Dtest=CourseRegistrationServiceTest
```

### Test Coverage

The test suite currently contains 23 test methods covering:
- Available capacity enrollment
- Full capacity waitlist behavior
- Duplicate enrollment prevention
- Duplicate waitlist prevention
- Prerequisite validation
- FIFO waitlist ordering
- Waitlist promotion on drop
- Ineligible learner skip behavior
- Invalid request handling
- Duplicate drop prevention

Tests use H2 in-memory database for isolation and speed.
The latest recorded test run found one error in `testFIFO_WaitlistOrder`; see
`LIMITATIONS.md` and `AI_USAGE_RECORD.md`. Rerun `.\mvnw.cmd test` after
addressing it.

## API Endpoints

### Enroll in a Course

**Endpoint**: `POST /api/registration/enroll`

**Request Body**:
```json
{
  "courseCode": "CS201",
  "learnerEmail": "learner1_completed_CS101@example.com"
}
```

**Response**:
```json
{
  "status": "SUCCESS",
  "message": "Enrolled successfully",
  "waitlistPosition": null
}
```

Possible status values:
- `SUCCESS`: Learner enrolled successfully
- `WAITLISTED`: Learner added to waitlist (includes `waitlistPosition`)
- `ERROR`: Enrollment failed (check message for details)

### Drop a Course

**Endpoint**: `POST /api/registration/drop`

**Request Body**:
```json
{
  "courseCode": "CS201",
  "learnerEmail": "learner1_completed_CS101@example.com"
}
```

**Response**:
```json
{
  "status": "SUCCESS",
  "message": "Dropped successfully",
  "waitlistPosition": null
}
```

Possible status values:
- `SUCCESS`: Learner dropped successfully
- `ERROR`: Drop failed (check message for details)

## Sample Data

The application includes sample data in `src/main/resources/data.sql`:

**Course**:
- Code: CS201
- Name: Advanced Programming
- Prerequisite: CS101
- Capacity: 3

**Learners**:
- learner1_completed_CS101@example.com (eligible)
- learner2_completed_CS101@example.com (eligible)
- learner3_completed_CS101@example.com (eligible)
- learner4_completed_CS101@example.com (eligible)
- learner5_no_prereq@example.com (ineligible)
- learner6_completed_CS101@example.com (eligible)

## Prerequisite Rule

A learner is eligible to enroll if their email contains `"completed_" + prerequisiteCourseCode`.

Example:
- Course CS201 requires CS101
- Learner with email "learner1_completed_CS101@example.com" is eligible
- Learner with email "learner5_no_prereq@example.com" is not eligible

## Waitlist Policy

When a course is full:
- Eligible learners are added to the waitlist in FIFO order
- Ineligible learners cannot join the waitlist

When a seat opens (via drop):
- The earliest waitlisted learner is checked for eligibility
- If eligible: promoted to enrollment, removed from waitlist
- If ineligible: removed from waitlist (must re-request if they become eligible)
- Process continues until a learner is promoted or waitlist is empty

## Design Documentation

- **DESIGN_SPECIFICATION.md**: Detailed design specification including service boundaries, data model, API design, invariants, and assumptions
- **AI_USAGE_RECORD.md**: Record of AI assistance used during development
- **LIMITATIONS.md**: In-scope limitations and follow-up risks

## Limitations

See **LIMITATIONS.md** for known limitations and follow-up risks.

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:
1. Ensure MySQL is running
2. Verify the database `courseregistration` exists
3. Check credentials in `application.properties`
4. Ensure MySQL is accessible on localhost:3306

### Port Already in Use

If port 8085 is already in use, change it in `application.properties`:
```properties
server.port=8086
```

### Test Failures

If tests fail:
1. Ensure no other MySQL instances are interfering
2. Check that H2 dependency is in pom.xml
3. Verify application-test.properties is configured correctly
