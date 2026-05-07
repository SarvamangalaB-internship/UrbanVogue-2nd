# UrbanVogue E-Commerce Platform

### Overview

UrbanVogue is a microservices-based e-commerce platform developed for UrbanVogue Apparel, a mid-sized retail company expanding its business into online shopping. The platform enables customers to browse products, place orders, make payments, and receive notifications like a real time platform.

The system is designed using modern software engineering principles with a focus on scalability, security, maintainability, and high availability. Each business functionality is implemented as an independent microservice, allowing easy deployment, monitoring, and future enhancements.
The main focus was to handle heavy traffic during end-of-season-sale. 

---

### Architecture Overview

The application follows a **Microservices Architecture** where each service handles a specific business functionality and communicates using REST APIs.

---

###  Microservices Included

| Service Name | Responsibility |
|---|---|
| API Gateway | Central entry point and request routing |
| Auth Service | Authentication and JWT token generation |
| Admin Service | Product and inventory management |
| Order Service | Order processing and order lifecycle |
| Payment Service | Payment handling and refunds |
| Notification Service | User notifications and alerts |
| User Account Service | User details and wallet/account operations |

---

### High-Level Flow

1. Client sends request through Frontend  
2. Request goes to API Gateway  
3. API Gateway routes request to appropriate microservice  
4. Services communicate using REST APIs  
5. Each service manages its own database  

---

## Technology Stack

#### Backend
- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate

#### Database
- H2 Database / MySQL

#### Authentication
- JWT (JSON Web Token)
- Spring Security

#### Frontend
- React.js (Single Page Application)

#### Communication
- RESTful APIs
- RestTemplate

### Build Tool
- Maven

  ---

### Conclusion

UrbanVogue demonstrates a scalable and secure microservices-based e-commerce architecture using Spring Boot and modern backend technologies. The project showcases independent service design, REST API communication, JWT-based security, payment workflows, and real-time notifications. The architecture is flexible and can be extended further for enterprise-level deployment.
