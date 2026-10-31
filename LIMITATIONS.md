# Limitations and Follow-up Risks

This project implements a bounded registration workflow for one course
offering. It is not intended as a production-ready registration platform.

- **Concurrent requests:** The service uses transactions but does not lock the
  course row or implement optimistic locking. Simultaneous enrollments could
  race near capacity. Add database-level locking/versioning and a focused
  concurrency test before supporting concurrent registration traffic.
- **Prerequisite validation:** Eligibility is inferred from an email containing
  `completed_` followed by the prerequisite course code. Replace this
  demonstration rule with authoritative course-completion data in a real
  system.
- **Waitlist lifecycle:** There is no queue size limit, expiration, or learner
  notification. An ineligible learner encountered during promotion is removed
  from the waitlist and must request enrollment again if they later become
  eligible.
- **Operational and security controls:** Authentication, authorization,
  rate-limiting, monitoring, and enrollment audit history are outside this
  feature’s scope.
- **Database management:** The project uses Hibernate schema update for
  development and initializes sample data. A deployed service should use
  reviewed schema migrations and environment-specific configuration.
- **Test status:** The latest `.\mvnw.cmd test` run reported 23 tests, with no
  assertion failures and one error in `testFIFO_WaitlistOrder`: a repository
  query returned two results where one was expected. Resolve the error and
  rerun the full suite before treating the project as verified.
