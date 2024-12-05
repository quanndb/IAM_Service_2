# IAM Service 2

## Overview

IAM Service 2 is a robust Identity and Access Management system designed for flexible authentication and user management.
It supports Keycloak integration for enterprise-level SSO (Single Sign-On) and self-hosted IDP (Identity Provider)
for standalone operations. The system provides comprehensive Role-Based Access Control (RBAC), soft deletion, auditing,
and logging to meet modern application security requirements.

---

## Key Features

### Authentication Modes

- **Keycloak Integration**:
  - Redirects users to the Keycloak login page.
  - Verifies Bearer tokens for every request and synchronizes user data with an internal database.
  - Includes APIs for register, logout, and refresh token operations.
- **Self-hosted IDP**:
  - Uses the internal database for user authentication.
- **Easy to extend IDP**:
  - The system supports seamless integration with various IDPs, allowing easy addition or replacement of authentication mechanisms with minimal configuration.

### Tracking user behaviors

- The system tracks user actions to prevent abuse, such as spamming the "forgot password" feature. Users who exceed a configurable number of failed login attempts are temporarily blocked, enhancing security and flexibility.

### User Management APIs

- Create user.
- Soft delete user.
- Lock/Unlock user.
- Reset password.
- View user list with pagination and search.
- View user details.
- Assign roles, permissions to users.

### RBAC (Role-Based Access Control)

- Manage permissions:
  - CRUD operations for roles and permissions.
  - Assign permissions to roles.
  - Assign roles to users.

### Soft Delete

- All deletions are soft deletions, utilizing a `deleted` field.

### AuditorAware

- Tracks the user responsible for system changes.

### Pagination

- Supports pagination for list APIs (users, roles, and permissions).

### API Documentation with Swagger

- Integrated Swagger for API documentation.
- Includes project details in the GitHub `README.md`.

### Logging

- Logs requests, responses, and exceptions with daily log rotation (log rolling).
- Excludes sensitive information like passwords.

### Default Roles

- **User Manager**: Access to user management APIs.
- **System Admin**: Access to role and permission management APIs.

### Optional Enhancements

- Allow users to change passwords when using Keycloak.
- Integrate Google SSO.

---

## Technologies Used

### Core Frameworks and Libraries

- **Spring Boot**: Backend framework.
- **Spring Security**: For authentication and authorization.
- **Keycloak**: Identity and Access Management integration.
- **Swagger**: API documentation.

### Infrastructure and Tools

- **Docker Compose**: For container orchestration.
- **Redis**: For caching and session management.
- **PostgreSQL**: As the primary database.

### Additional Features

- **Hibernate**: ORM for database interaction.
- **AuditorAware**: For tracking changes.
- **Pagination**: Using Spring Data.

### Logging

- **Logback**: For structured logging with log rolling.

---

## Setup Instructions

### Prerequisites

- Docker and Docker Compose.
- JDK 23.
- Gradle.

### Steps

1. Clone the repository.
   ```bash
   git clone https://github.com/quanndb/IAM_Service_2.git
   cd iam-service-2
   ```
2. Configure the database, Redis, and Keycloak settings in `application.yml`.
3. Build the Docker containers.
   ```bash
   docker-compose up --build
   ```
4. Access the application on the port `2818`.

---

## Contribution Guidelines

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Create a pull request.

---

## License

This project is licensed under the MIT License.
