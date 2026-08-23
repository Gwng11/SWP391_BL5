# Hotel Management System - Codex Instructions

## Project Goal

This repository contains an existing Hotel Management System developed by our team.

The project is already partially implemented.

Your goal is NOT to redesign or recreate the project from scratch.

Your goal is to inspect the existing implementation and incrementally complete, fix, or refactor it so that the system conforms to the Software Requirements Specification located at:

`docs/Team_6_SRS_Document.docx`

The SRS is the primary source of truth for business requirements.

The existing source code is the primary source of truth for:
- project architecture
- framework choices
- package organization
- coding conventions
- existing database access approach
- controller/service/DAO structure
- DTO/entity conventions
- JSP/UI conventions
- error handling conventions

Do not introduce a new architecture unless explicitly requested.


## Important Working Principle

Before implementing or modifying any feature:

1. Read the relevant section of the SRS.
2. Inspect the existing implementation of the related module.
3. Inspect related:
    - models/entities
    - DTOs
    - DAO interfaces and implementations
    - services
    - servlets/controllers
    - JSP pages
    - filters
    - database schema or SQL scripts
4. Trace the current request flow end-to-end.
5. Identify the smallest set of changes required.
6. Preserve existing architecture and naming conventions.
7. Implement the change.
8. Run available tests/build checks.
9. Review the diff for unrelated changes.

Do not start coding before understanding the existing implementation.


## Business Scope

This is a single-hotel Hotel Management System.

Main actors:

- Guest
- Customer
- Receptionist
- Service Staff
- Manager
- System Administrator
- Payment Gateway
- Email Service

System Administrator is responsible for system/account administration and does not automatically perform hotel operational duties.

Manager is responsible for hotel operational management and configuration.

Receptionist handles reservations, customers, check-in, stays, room assignments, checkout, payments, and service-request coordination.

Service Staff handles service requests, housekeeping, room inspection, and maintenance work.


## Major Business Modules

The system includes:

- Authentication and account verification
- Personal profile management
- Hotel information
- Room type and room management
- Room availability search
- Room pricing
- Customer management
- Reservation management
- Deposit payments
- Room assignment
- Guest check-in
- Guest stay management
- Hotel service requests
- Housekeeping
- Room inspection
- Maintenance
- Checkout
- Invoice and final payment
- Management dashboard
- Reports and statistics
- Employee account administration
- Email notifications


## Critical Business Rules

Unless an existing approved implementation defines a more specific behavior, follow the SRS.

Important rules include:

- Check-out date must be later than check-in date.
- Room availability must be checked before reservation creation or modification.
- Overlapping active bookings must not exceed available room capacity.
- Required reservation deposit is 20% of the estimated reservation total.
- A reservation becomes confirmed only when the required deposit has been successfully recorded.
- The 20% deposit is non-refundable when the customer cancels.
- Only confirmed reservations may proceed to check-in.
- A physical room must be assigned before check-in is completed.
- Additional service charges must appear in the final invoice.
- Outstanding balances must be settled before checkout can be completed.
- After checkout, a room requires housekeeping.
- A room requiring cleaning must not be assignable until housekeeping is completed.
- Rooms with inappropriate operational status must not be reserved or assigned.
- Access must follow role-based authorization.
- Customer accounts must be email verified before authenticated functions are available.
- Accounts are temporarily locked after five consecutive failed login attempts.


## Reservation and Room Principle

Do not confuse room-type reservation with physical room assignment.

A reservation may reserve room-type capacity first.

Physical rooms are assigned later through the room-assignment/check-in process.

Preserve this distinction when modifying availability or reservation logic.


## Housekeeping Principle

Checkout must not immediately make the room available.

Expected conceptual lifecycle:

CHECKED_OUT
→ room requires cleaning
→ housekeeping processing
→ inspection
→ ready/available

If a maintenance issue prevents room use, the room remains unavailable until the issue is resolved.


## Payment and Invoice Principle

Payment and Invoice are separate concepts.

Payment represents financial transactions.

Invoice represents billing and contains invoice items such as:
- room charges
- service charges
- additional charges

Do not merge these concepts unless the existing architecture already intentionally models them differently.


## External Services

The SRS does not select a specific Payment Gateway provider or Email provider.

Do not hard-code a provider unless explicitly requested.

Prefer the abstraction already present in the codebase.

If no integration exists yet, preserve extensibility and use mock/dev implementations when appropriate.


## Do Not Invent Requirements

If a required behavior is not defined by:
1. the SRS,
2. the existing code,
3. the database schema,
4. or an explicit user instruction,

do not silently invent the business rule.

Examples:
- tax percentage
- cancellation deadline
- refund policy beyond the documented deposit rule
- unsupported reservation statuses
- payment provider-specific behavior

Report the ambiguity before implementing behavior that would materially change business logic.


## Code Change Rules

Prefer minimal, focused changes.

Do not:
- rewrite unrelated modules
- rename packages without necessity
- change public interfaces unnecessarily
- create duplicate services or DAOs
- introduce a new framework
- replace existing persistence technology
- redesign all JSP pages while fixing one feature
- remove working functionality without explicit reason

Reuse existing utilities, DTOs, exceptions, database helpers, JSP fragments, filters, and conventions whenever possible.


## Layer Responsibilities

Follow the architecture already present in the repository.

In general, preserve separation between:

Controller / Servlet:
- HTTP request handling
- session handling
- parameter extraction
- response routing

Service:
- business rules
- workflow validation
- transaction orchestration

DAO:
- persistence and SQL

DTO / Model:
- data transport/domain representation

JSP:
- presentation only

Do not move business rules into JSP pages.


## Database Changes

Before changing the database:

1. inspect the current schema
2. inspect related Java mappings and SQL
3. determine whether the feature can be completed without schema modification

If schema changes are necessary:
- make them explicit
- keep them backward-compatible where practical
- update related DAO/model code
- provide the required migration SQL


## Authorization

Do not rely only on hiding UI buttons.

Protected operations must also be enforced server-side.

A user must not be able to access another role's protected operation simply by manually entering its URL.


## Definition of Done

A task is successful only when:

- the requested business behavior matches the relevant SRS requirement
- the existing project architecture is preserved
- affected code paths are consistent
- compilation/build succeeds
- available tests pass
- no unrelated features are broken
- server-side authorization is preserved
- database operations remain consistent
- error handling follows existing conventions
- changed JSP/UI pages remain consistent with the backend behavior
- no obsolete UI action remains for removed backend states
- the final diff contains only changes relevant to the task


## When Finishing a Task

Report:

1. What you found in the existing implementation.
2. What was inconsistent with the requirement.
3. Files changed.
4. Business behavior implemented.
5. Database changes, if any.
6. Tests/build commands executed.
7. Remaining ambiguity or follow-up work.

Do not claim success if the project does not compile or the workflow has not been checked.