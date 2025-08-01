-- Enable UUID generation extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Core Doctor Profile Table
CREATE TABLE doctors (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID NOT NULL,               -- FK to User Service's user.id
                         name VARCHAR(200) NOT NULL,
                         slug VARCHAR(200) UNIQUE,
                         gender VARCHAR(10),
                         summary TEXT,
                         about TEXT,
                         image_url VARCHAR(255),
                         experience_years INT,
                         is_verified BOOLEAN DEFAULT FALSE,
                         rating_average DECIMAL(3,2) DEFAULT 0.0,
                         review_count INT DEFAULT 0,
                         status VARCHAR(20) DEFAULT 'ACTIVE',   -- ACTIVE, INACTIVE, SUSPENDED
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Awards linked to doctor
CREATE TABLE doctor_awards (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                               title VARCHAR(255) NOT NULL,
                               awarded_year INT,
                               summary VARCHAR(255)
);

-- Qualifications linked to doctor
CREATE TABLE doctor_qualifications (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                       qualification_name VARCHAR(100) NOT NULL,
                                       college_name VARCHAR(255),
                                       completion_year INT
);

-- Memberships linked to doctor
CREATE TABLE doctor_memberships (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                    membership_council_name VARCHAR(255) NOT NULL
);

-- Organizations linked to doctor (employment)
CREATE TABLE doctor_organizations (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                      organization_name VARCHAR(255) NOT NULL,
                                      role VARCHAR(100),
                                      city VARCHAR(100),
                                      state VARCHAR(100),
                                      country VARCHAR(100),
                                      tenure_start DATE,
                                      tenure_end DATE,
                                      summary TEXT
);

-- Registrations/licensing linked to doctor
CREATE TABLE doctor_registrations (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                      registration_council_name VARCHAR(255),
                                      registration_number VARCHAR(100),
                                      registration_year INT
);

CREATE TABLE doctor_session_pricing (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                        session_type_id UUID NOT NULL,   -- FK to session_types in Session Service (logical reference)
                                        custom_price DECIMAL(10,2),      -- Doctor’s own price for this session (NULL = use default)
                                        is_active BOOLEAN DEFAULT true,
                                        UNIQUE (doctor_id, session_type_id)
);


-- Specializations and subspecializations for doctor
CREATE TABLE doctor_specializations (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                        speciality VARCHAR(100) NOT NULL,
                                        subspecialization VARCHAR(100)
);

-- Photo gallery linked to doctor
CREATE TABLE doctor_photos (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                               photo_url VARCHAR(255) NOT NULL,
                               is_default BOOLEAN DEFAULT FALSE
);

-- Practices (clinic/hospital locations) where doctor works
CREATE TABLE doctor_practices (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                  practice_name VARCHAR(255) NOT NULL,
                                  practice_type VARCHAR(30), -- CLINIC, HOSPITAL, etc.
                                  address_id UUID NOT NULL,  -- FK to Address Service (addresses table)
                                  slug VARCHAR(200),
                                  practice_position INT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Recommendations / Reviews for doctors
CREATE TABLE recommendations (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
                                 rating DECIMAL(3,2),
                                 review VARCHAR(255),
                                 recommendation_count INT
);

-- Note:
-- Doctor Practice Timings and schedule are offloaded to Timing and Schedule services respectively.
-- So there is no doctor_practice_timings table here.
