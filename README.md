# Flight Booking API Documentation

This document provides comprehensive API documentation for the Flight Booking Backend (flight-2306211660-be), a Spring Boot application that manages flight bookings, airplanes, flights, and billing.

## Table of Contents

- [Overview](#overview)
- [Authentication & Authorization](#authentication--authorization)
- [Base Response Format](#base-response-format)
- [API Endpoints](#api-endpoints)
  - [Airplane APIs](#airplane-apis)
  - [Flight APIs](#flight-apis)
  - [Booking APIs](#booking-apis)
  - [Bill APIs](#bill-apis)
- [Error Handling](#error-handling)

## Overview

The Flight Booking API provides endpoints for managing:
- Airplanes (CRUD operations)
- Flights (CRUD operations with filtering and reminders)
- Bookings (CRUD operations with payment confirmation)
- Bills (CRUD operations with payment processing)

All APIs return responses wrapped in a `BaseResponseDTO` format.

## Authentication & Authorization

The API uses JWT-based authentication with role-based access control. Available roles:
- `SUPERADMIN`: Full access to all endpoints
- `FLIGHT_AIRLINE`: Access to airplane and flight management
- `CUSTOMER`: Limited access to booking and bill operations

Endpoints specify required roles using `@PreAuthorize` annotations.

## Base Response Format

All API responses follow this structure:

```json
{
  "status": 200,
  "message": "Success message",
  "timestamp": "2023-11-30T07:58:15.191+07:00",
  "data": { ... } // Response data or null
}
```

- `status`: HTTP status code
- `message`: Descriptive message
- `timestamp`: Response timestamp in Asia/Jakarta timezone
- `data`: Actual response data (varies by endpoint)

## API Endpoints

### Airplane APIs

#### GET /api/airplane/all (or /api/airplanes/all)

Retrieve all airplanes with optional filtering.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Query Parameters:**
- `isDeleted` (boolean, optional): Filter by deletion status
- `search` (string, optional): Search by ID, model, or airline ID (case-insensitive)
- `airlineId` (string, optional): Filter by airline ID
- `model` (string, optional): Filter by model (case-insensitive partial match)
- `manufactureYear` (integer, optional): Filter by manufacture year

**Response:**
```json
{
  "status": 200,
  "message": "Data Airplane Berhasil Ditemukan",
  "data": [
    {
      "id": "string",
      "airlineId": "string",
      "model": "string",
      "seatCapacity": 150,
      "manufactureYear": 2020,
      "createdAt": "2023-01-01T00:00:00",
      "updatedAt": "2023-01-01T00:00:00",
      "isDeleted": false
    }
  ]
}
```

#### GET /api/airplane/{id}

Retrieve a specific airplane by ID.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Path Parameters:**
- `id` (string): Airplane ID

**Response:** Single `AirplaneResponseDTO` object

#### POST /api/airplane/create

Create a new airplane.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Request Body:**
```json
{
  "airlineId": "string",
  "model": "string",
  "seatCapacity": 150,
  "manufactureYear": 2020
}
```

**Response:** `AirplaneResponseDTO` object

#### PUT /api/airplane/update

Update an existing airplane.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Request Body:**
```json
{
  "id": "string",
  "model": "string",
  "seatCapacity": 150,
  "manufactureYear": 2020
}
```

**Response:** `AirplaneResponseDTO` object

#### DELETE /api/airplane/{id} or POST /api/airplane/{id}/delete

Soft delete an airplane.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Path Parameters:**
- `id` (string): Airplane ID

**Response:** `AirplaneResponseDTO` object

#### POST /api/airplane/{id}/activate

Activate a deleted airplane.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Path Parameters:**
- `id` (string): Airplane ID

**Response:** `AirplaneResponseDTO` object

### Flight APIs

#### GET /api/flight/all

Retrieve all flights with optional filtering.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`

**Query Parameters:**
- `originAirportCode` (string, optional): Filter by origin airport code
- `destinationAirportCode` (string, optional): Filter by destination airport code
- `airlineId` (string, optional): Filter by airline ID
- `status` (integer, optional): Filter by flight status
- `includeDeleted` (boolean, optional): Include deleted flights (restricted to SUPERADMIN/FLIGHT_AIRLINE)
- `search` (string, optional): General search term

**Response:**
```json
{
  "status": 200,
  "message": "Data Flight Berhasil Ditemukan",
  "data": [
    {
      "id": "string",
      "airlineId": "string",
      "airplaneId": "string",
      "originAirportCode": "CGK",
      "destinationAirportCode": "DPS",
      "departureTime": "2023-12-01T10:00:00",
      "arrivalTime": "2023-12-01T12:00:00",
      "terminal": "1",
      "gate": "A1",
      "baggageAllowance": 20,
      "facilities": "WiFi, Entertainment",
      "status": 1,
      "createdAt": "2023-01-01T00:00:00",
      "updatedAt": "2023-01-01T00:00:00",
      "isDeleted": false,
      "durationMinutes": 120,
      "originAirport": { ... },
      "destinationAirport": { ... },
      "classes": [ ... ]
    }
  ]
}
```

#### GET /api/flight/active/today

Get count of active flights today.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Response:**
```json
{
  "status": 200,
  "message": "Active flights for today retrieved",
  "data": 25
}
```

#### GET /api/flight/reminder (or /api/flights/reminder)

Get flight reminders for upcoming departures.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`

**Query Parameters:**
- `interval` (string, optional): Time interval (e.g., "3h", "3" for hours, default 3)
- `CustomerId` (string, optional): Customer ID (restricted for CUSTOMER role)

**Response:**
```json
{
  "status": 200,
  "message": "Upcoming flights retrieved",
  "data": [
    {
      "flightNumber": "GA-101",
      "airline": "Garuda Indonesia",
      "origin": "Jakarta",
      "destination": "Bali",
      "departureTime": "2023-12-01T10:00:00",
      "remainingTimeMinutes": 180,
      "status": 1,
      "totalPaidBookings": 120,
      "totalUnpaidBookings": 10
    }
  ]
}
```

#### GET /api/flight/{id}

Retrieve a specific flight by ID.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`

**Path Parameters:**
- `id` (string): Flight ID

**Response:** `FlightResponseDTO` object

#### POST /api/flight/create

Create a new flight.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Request Body:**
```json
{
  "airlineId": "string",
  "airplaneId": "string",
  "originAirportCode": "CGK",
  "destinationAirportCode": "DPS",
  "departureTime": "2023-12-01T10:00:00",
  "arrivalTime": "2023-12-01T12:00:00",
  "terminal": "1",
  "gate": "A1",
  "baggageAllowance": 20,
  "facilities": "WiFi",
  "classes": [
    {
      "classType": "Economy",
      "price": 1000000,
      "seatCount": 100
    }
  ]
}
```

**Response:** `FlightResponseDTO` object

#### PUT /api/flight/update

Update an existing flight.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Request Body:**
```json
{
  "id": "string",
  "departureTime": "2023-12-01T10:00:00",
  "arrivalTime": "2023-12-01T12:00:00",
  "terminal": "1",
  "gate": "A1",
  "baggageAllowance": 20,
  "facilities": "WiFi",
  "classes": [ ... ]
}
```

**Response:** `FlightResponseDTO` object

#### POST /api/flight/delete/{id}

Delete a flight.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Path Parameters:**
- `id` (string): Flight ID

**Response:** `FlightResponseDTO` object

### Booking APIs

#### GET /api/booking

Retrieve all bookings with optional filtering.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`

**Query Parameters:**
- `flightId` (string, optional): Filter by flight ID
- `includeDeleted` (boolean, optional): Include deleted bookings (restricted)
- `search` (string, optional): Search term
- `contactEmail` (string, optional): Filter by contact email
- `status` (integer, optional): Filter by booking status

**Response:**
```json
{
  "status": 200,
  "message": "Data Booking Berhasil Ditemukan",
  "data": [
    {
      "id": "string",
      "flightId": "string",
      "route": "CGK-DPS",
      "classFlightId": 1,
      "classType": "Economy",
      "contactEmail": "customer@example.com",
      "contactPhone": "+628123456789",
      "passengerCount": 2,
      "status": 1,
      "totalPrice": 2000000,
      "createdAt": "2023-01-01T00:00:00",
      "updatedAt": "2023-01-01T00:00:00",
      "isDeleted": false,
      "passengers": [ ... ],
      "seatAssignments": [ ... ]
    }
  ]
}
```

#### GET /api/booking/{id}

Retrieve a specific booking by ID.

**Authorization:** `CUSTOMER`, `SUPERADMIN`

**Path Parameters:**
- `id` (string): Booking ID

**Response:** `BookingResponseDTO` object

#### POST /api/booking/create

Create a new booking.

**Authorization:** `CUSTOMER`, `SUPERADMIN`

**Request Body:**
```json
{
  "flightId": "string",
  "classFlightId": 1,
  "contactEmail": "customer@example.com",
  "contactPhone": "+628123456789",
  "passengerCount": 2,
  "passengers": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "dateOfBirth": "1990-01-01",
      "gender": "MALE",
      "nationality": "Indonesia",
      "passportNumber": "A1234567"
    }
  ],
  "seatIds": [1, 2]
}
```

**Response:** `BookingResponseDTO` object

#### PUT /api/booking/update

Update an existing booking.

**Authorization:** `CUSTOMER`, `SUPERADMIN`

**Request Body:**
```json
{
  "id": "string",
  "contactEmail": "customer@example.com",
  "contactPhone": "+628123456789",
  "passengers": [ ... ],
  "newPassengers": [ ... ],
  "seatIds": [1, 2]
}
```

**Response:** `BookingResponseDTO` object

#### GET /api/booking/today

Get count of bookings today.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Response:**
```json
{
  "status": 200,
  "message": "Total bookings today retrieved",
  "data": 150
}
```

#### GET /api/booking/chart

Get booking chart data for a specific month and year.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`

**Query Parameters:**
- `month` (integer): Month (1-12)
- `year` (integer): Year

**Response:**
```json
{
  "status": 200,
  "message": "Booking chart computed",
  "data": {
    "chart": [ ... ],
    "summary": {
      "totalBookings": 1000,
      "totalRevenue": 100000000,
      "topPerformer": "Flight ABC"
    }
  }
}
```

#### POST /api/booking/delete/{id}

Delete a booking.

**Authorization:** `CUSTOMER`, `SUPERADMIN`

**Path Parameters:**
- `id` (string): Booking ID

**Response:** `BookingResponseDTO` object

#### POST /api/booking/payment/confirm

Confirm payment for a booking (internal callback).

**Authorization:** None (internal use)

**Request Body:**
```json
{
  "billId": "uuid",
  "customerId": "string"
}
```

**Response:** `BookingResponseDTO` object

### Bill APIs

#### POST /api/bill/create

Create a new bill.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`, `ACCOMMODATION_OWNER`, `RENTAL_VENDOR`, `INSURANCE_PROVIDER`, `TOUR_PACKAGE_VENDOR`

**Request Body:**
```json
{
  "customerId": "string",
  "serviceName": "Flight",
  "serviceReferenceId": "string",
  "description": "Flight booking payment",
  "amount": 1000000
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Bill created",
  "data": {
    "id": "uuid",
    "customerId": "string",
    "serviceName": "Flight",
    "serviceReferenceId": "string",
    "description": "Flight booking payment",
    "amount": 1000000,
    "status": "UNPAID",
    "createdAt": "2023-01-01T00:00:00",
    "updatedAt": "2023-01-01T00:00:00",
    "paymentTimestamp": null
  }
}
```

#### PUT /api/bill/update/{billId}

Update an existing bill.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`, `ACCOMMODATION_OWNER`, `RENTAL_VENDOR`, `INSURANCE_PROVIDER`, `TOUR_PACKAGE_VENDOR`

**Path Parameters:**
- `billId` (UUID): Bill ID

**Request Body:**
```json
{
  "customerId": "string",
  "serviceName": "Flight",
  "serviceReferenceId": "string",
  "amount": 1000000
}
```

**Response:** `BillResponseDTO` object

#### GET /api/bill

Get all bills (SUPERADMIN only).

**Authorization:** `SUPERADMIN`

**Query Parameters:**
- `customerId` (string, optional): Filter by customer ID
- `serviceName` (string, optional): Filter by service name
- `status` (string, optional): Filter by status

**Response:** Array of `BillResponseDTO` objects

#### GET /api/bill/customer

Get bills for the authenticated customer.

**Authorization:** `CUSTOMER`

**Query Parameters:**
- `customerId` (string, optional): Customer ID (must match authenticated user)
- `status` (string, optional): Filter by status
- `sortBy` (string, optional): Sort field
- `order` (string, optional): Sort order

**Response:** Array of `BillResponseDTO` objects

#### GET /api/bill/{serviceName}

Get bills for a specific service.

**Authorization:** `SUPERADMIN`, `FLIGHT_AIRLINE`, `ACCOMMODATION_OWNER`, `RENTAL_VENDOR`, `INSURANCE_PROVIDER`, `TOUR_PACKAGE_VENDOR`

**Path Parameters:**
- `serviceName` (string): Service name

**Query Parameters:**
- `customerId` (string, optional): Filter by customer ID
- `status` (string, optional): Filter by status

**Response:** Array of `BillResponseDTO` objects

#### GET /api/bill/detail/{billId}

Get detailed bill information.

**Authorization:** `CUSTOMER`, `SUPERADMIN`, `FLIGHT_AIRLINE`, `ACCOMMODATION_OWNER`, `RENTAL_VENDOR`, `INSURANCE_PROVIDER`, `TOUR_PACKAGE_VENDOR`

**Path Parameters:**
- `billId` (UUID): Bill ID

**Response:** `BillResponseDTO` object

#### POST /api/bill/{billId}/pay

Pay a bill.

**Authorization:** `CUSTOMER`

**Path Parameters:**
- `billId` (UUID): Bill ID

**Request Body:**
```json
{
  "billId": "uuid",
  "customerId": "string"
}
```

**Response:** `BillResponseDTO` object

## Error Handling

Common HTTP status codes:
- `200 OK`: Success
- `201 Created`: Resource created
- `400 Bad Request`: Validation errors or business rule violations
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error responses follow the base format with appropriate status codes and error messages in the `message` field.