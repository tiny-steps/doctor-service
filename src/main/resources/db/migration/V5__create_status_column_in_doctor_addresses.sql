create type doctor_status as enum ('ACTIVE','INACTIVE');


ALTER TABLE doctors.public.doctor_addresses
ADD column if not exists status doctor_status;
