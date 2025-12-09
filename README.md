# BookMyPlay - Sports Ground Booking System

A modern Android application for booking sports grounds and facilities. BookMyPlay allows users to browse, search, and book sports grounds while providing administrators with a comprehensive dashboard to manage grounds and bookings.

![App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

---

## 📱 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Backend Setup](#-backend-setup)
- [Project Structure](#-project-structure)
- [Usage Guide](#-usage-guide)
- [API Endpoints](#-api-endpoints)
- [Build Instructions](#-build-instructions)
- [Configuration](#-configuration)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 👤 User Features

- **🔐 Authentication**
  - User registration with email and password
  - Secure login with JWT token authentication
  - Password change functionality
  - Session management

- **🔍 Browse & Search**
  - Browse all available sports grounds
  - Search grounds by name, location, or type
  - View ground images and details
  - Filter grounds by category

- **📅 Booking System**
  - View available time slots for each ground
  - Select date and time for booking
  - Real-time slot availability display
  - Book multiple slots
  - View booking confirmation

- **📋 My Bookings**
  - View all active/upcoming bookings
  - View complete booking history
  - Cancel bookings (if not in past)
  - Booking status tracking (Pending, Confirmed, Cancelled)

- **👤 Profile Management**
  - View user profile information
  - Edit name and phone number
  - Change password
  - Secure logout

### 👨‍💼 Admin Features

- **🏟️ Ground Management**
  - Create new sports grounds
  - Upload ground images
  - Edit ground details (name, location, type, price, description)
  - Delete grounds
  - View all created grounds

- **📊 Dashboard & Analytics**
  - View all bookings from admin's grounds
  - Filter bookings by date range
  - View booking statistics:
    - Total bookings
    - Total revenue
    - Active bookings count
    - Cancelled bookings count

- **👥 Booking Management**
  - View all bookings with customer information
  - See customer details (name, email, phone)
  - Approve or reject pending bookings
  - View booking details (ground, date, time, price, status)

- **🔐 Admin Authentication**
  - Secure admin login
  - Role-based access control
  - Admin-only features protection

---

## 📸 Screenshots

### User Interface
- **Home Screen**: Browse available grounds with search functionality
- **Ground Details**: View comprehensive ground information with images
- **Booking Screen**: Select date and time slots with visual availability
- **My Bookings**: Manage all your bookings in one place
- **Profile**: Edit your personal information

### Admin Interface
- **Admin Dashboard**: Overview of all bookings and statistics
- **Create Ground**: Upload images and add ground details
- **Manage Grounds**: Edit or delete existing grounds
- **Booking Management**: View and manage all bookings

---

## 🛠️ Technology Stack

### Frontend (Android)
- **Language**: Java
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVC (Model-View-Controller)
- **UI Framework**: Material Design Components
- **Key Libraries**:
  - Retrofit 2.9.0 - REST API client
  - Gson - JSON serialization
  - Glide 4.16.0 - Image loading and caching
  - OkHttp 4.11.0 - HTTP client with logging
  - Material Design Components - Modern UI components
  - RecyclerView - Efficient list rendering
  - CardView - Material card layouts

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: Supabase (PostgreSQL)
- **Storage**: Supabase Storage (for images)
- **Authentication**: JWT (JSON Web Tokens)
- **File Upload**: Multer
- **Password Hashing**: bcrypt

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio** (Latest version recommended)
  - Android SDK 24 or higher
  - Gradle 8.1.0 or higher
  - JDK 8 or higher

- **Node.js** (v14 or higher)
  - npm or yarn package manager

- **Supabase Account**
  - Create a free account at [supabase.com](https://supabase.com)
  - Create a new project
  - Get your project URL and API key

- **Physical Device or Emulator**
  - Android 7.0 (API 24) or higher
  - Internet connection for API calls

---

## 🚀 Installation

### Step 1: Clone the Repository

git clone <repository-url>
cd GroundBookingSystem
### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the `GroundBookingSystem` folder
4. Click **OK**

### Step 3: Sync Gradle

1. Android Studio will automatically sync Gradle
2. If not, click **File → Sync Project with Gradle Files**
3. Wait for dependencies to download

### Step 4: Configure API Endpoint

1. Open `app/src/main/java/com/example/groundbookingsystem/api/ApiClient.java`
2. Update the `BASE_URL` with your backend server URL:
   
   private static final String BASE_URL = "https://your-backend-url.com/";
   ### Step 5: Build and Run

1. Connect your Android device or start an emulator
2. Click **Run → Run 'app'** or press `Shift + F10`
3. Wait for the app to build and install

---

## 🔧 Backend Setup

### Step 1: Install Dependencies

cd <backend-directory>
npm install express @supabase/supabase-js bcrypt jsonwebtoken cors multer dotenv
### Step 2: Configure Environment Variables

Create a `.env` file in your backend directory:

SUPABASE_URL=your_supabase_project_url
SUPABASE_KEY=your_supabase_anon_key
JWT_SECRET=your_jwt_secret_key_change_this
PORT=3000### Step 3: Set Up Supabase Database

Run these SQL commands in your Supabase SQL Editor:
ql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Grounds table
CREATE TABLE grounds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url TEXT,
    created_by UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Bookings table
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) NOT NULL,
    ground_id UUID REFERENCES grounds(id) NOT NULL,
    booking_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Blocked slots table
CREATE TABLE blocked_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ground_id UUID REFERENCES grounds(id) NOT NULL,
    booking_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    UNIQUE(ground_id, booking_date, time_slot)
);### Step 4: Create Storage Bucket

1. Go to Supabase Dashboard → Storage
2. Create a new bucket named `ground-images`
3. Set it to **Public**
4. Create another bucket named `user-profiles` (optional, for future use)

### Step 5: Start Backend Server

node api.jsThe server should start on `http://localhost:3000` (or your configured PORT)

---


