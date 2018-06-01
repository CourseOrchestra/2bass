CREATE SCHEMA market version '1.0';

create sequence "customers numerator";

create table customers(
  id int DEFAULT NEXTVAL("customers numerator") not null,
  name varchar(30),
  CONSTRAINT pk_customers PRIMARY KEY (id)
);