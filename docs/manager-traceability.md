# Manager UC Traceability

Nguồn nghiệp vụ: `Team_6_SRS Document.docx` (Actors, UC table, Manager UC diagram, Screen Authorization, ERD, BR/MSG) và yêu cầu triển khai đính kèm. Nguồn kỹ thuật: Jakarta Servlet/JSP, JDBC repository/service hiện có và SQL Server schema `SingleHotelManagementDB`.

| UC | Chức năng | Màn hình / route | Service / domain | Database | Test | Trạng thái |
|---|---|---|---|---|---|---|
| UC06 | Log In | `/login` | `AuthService`, `AuthController`, `AuthFilter` | `users` | `AuthServiceTest`, `AuthControllerTest`, `ManagerLoginIntegrationTest`, `ManagerWebSmokeTest` | Hoàn thành |
| UC07 | Log Out | `/logout` | `AuthController` | Session HTTP | `AuthControllerTest`, `ManagerWebSmokeTest` | Hoàn thành |
| UC10 | View Personal Profile | `/profile` | `UserService`, `ProfileController` | `users` | `UserServiceTest`, `AuthFilterTest`, `ManagerWebSmokeTest` | Hoàn thành |
| UC11 | Update Personal Profile | `/profile` POST | `UserService`, `ProfileController` | `users.address`, `users.identification_number` | `UserServiceTest` | Hoàn thành |
| UC48 | Manage Housekeeping Tasks | `/manager/housekeeping`, `/staff/housekeeping` | `ManagerService`, `HousekeepingController` | `housekeeping_tasks`, `rooms`, `users` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest`, `AuthFilterTest` | Hoàn thành |
| UC54 | Manage Maintenance Issues | `/manager/maintenance`, `/staff/maintenance` | `ManagerService`, `MaintenanceController` | `maintenance_tickets`, `rooms`, `users` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest`, `AuthFilterTest` | Hoàn thành |
| UC57 | Resolve Maintenance Issue | `/manager/maintenance` and `/staff/maintenance` actions | `ManagerService` maintenance state transitions | `maintenance_tickets`, `rooms` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC58 | Manage Rooms | `/manager/rooms` | `ManagerService`, `ManagerRoomController` | `rooms`, `room_types` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC59 | Change Room Operational Status | `/manager/rooms` action | `ManagerService` room transition validation | `rooms` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest`, `FrontDeskServiceTest`, `RoomServiceTest` | Hoàn thành |
| UC60 | Manage Room Types | `/manager/room-types` | `ManagerService`, `RoomTypeManagementController` | `room_types` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC61 | Manage Amenities | `/manager/room-types` amenity actions | `ManagerService`, `JsonArrayUtil` | `room_types.amenities_json` | `ManagerServiceTest`, `JsonArrayUtilTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC62 | Manage Room Images | `/manager/room-types/images` | `ManagerService`, `RoomImageController`, `JsonArrayUtil` | `room_types.images_json`, local web upload directory | `ManagerServiceTest`, `RoomImageControllerTest`, `JsonArrayUtilTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC63 | Activate / Deactivate Room Type | `/manager/room-types` action | `ManagerService`; public search enforced by `RoomTypeRepository.findAllActive` | `room_types.is_active` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC64 | Manage Room Pricing | `/manager/pricing` | `ManagerService`, `RoomPricingController` | `room_types.base_price`, `room_rates` | `ManagerServiceTest`, `ManagerRepositoryIntegrationTest` | Hoàn thành |
| UC65 | View Management Dashboard | `/manager/dashboard` | `ManagerService`, `ManagerDashboardController` | live aggregates from rooms/reservations/payments/tasks/issues | `ManagerServiceTest`, `ManagerControllerTest`, `ManagerWebSmokeTest` | Hoàn thành |
| UC66 | View Reports and Statistics | `/manager/reports` | `ManagerService`, `ManagerReportController` | live period aggregates from reservations/payments/service requests | `ManagerServiceTest`, `ManagerControllerTest`, `ManagerRepositoryIntegrationTest`, `ManagerWebSmokeTest` | Hoàn thành |

## Dependency order

1. Safe additive migration and status constraints.
2. Domain DTOs and manager repository/service.
3. Authentication, RBAC and profile corrections.
4. Manager shell, rooms/types/media/pricing.
5. Housekeeping and maintenance shared workflows.
6. Dashboard/reports, tests, migration/build and smoke verification.

## SRS decisions

- `amenities_json` and `images_json` are retained because that is the current ERD/schema; no parallel amenity/image tables are introduced.
- The current room model separates `is_active`, `operational_status`, and `cleaning_status`. Deactivating a room type transactionally moves every unoccupied child room to `OUT_OF_SERVICE`; occupied rooms stay `OCCUPIED` until checkout and are then moved to `OUT_OF_SERVICE`. Inactive types and every non-`AVAILABLE` room are excluded from reserve/assign queries.
- Dashboard shows directly auditable counts and currency totals. No unsupported percentage KPI is fabricated.
- UC57 is implemented as actions in the UC54 maintenance screen, with separate service methods/tests.
- Manager has no Receptionist, Service Staff inspection, user administration, or email-template screens.
