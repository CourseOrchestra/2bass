SET SCHEMA market;

create sequence products_numerator;

create table products(
  id int DEFAULT NEXTVAL(products_numerator) not null,
  shop_id int foreign key references shops("id"),
  name varchar(30),
  description varchar(500),
  cost real,
  "count" int,
  CONSTRAINT pk_products PRIMARY KEY (id)
);