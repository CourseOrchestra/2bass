SET SCHEMA "market";

create sequence shops_numerator;

create table shops(
  id int DEFAULT NEXTVAL(shops_numerator) not null,
  name varchar(30),
  address varchar(200),
  CONSTRAINT pk_shops PRIMARY KEY (id)
);