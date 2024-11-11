CREATE SCHEMA "market" version '1.0';

create sequence "customers numerator";

create table customers(
                          id int DEFAULT NEXTVAL("customers numerator") not null,
                          name varchar(30),
                          CONSTRAINT pk_customers PRIMARY KEY (id)
);

create sequence shops_numerator;

create table shops(
                      id int DEFAULT NEXTVAL(shops_numerator) not null,
                      name varchar(30),
                      address varchar(200),
                      CONSTRAINT pk_shops PRIMARY KEY (id)
);

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

create sequence orders_numerator;

create table orders(
                       id int DEFAULT NEXTVAL(orders_numerator) not null,
                       product_id int foreign key references products("id"),
                       customer_id int foreign key references customers("id"),
                       cost real,
                       order_date datetime not null,
                       expire_date datetime,
                       CONSTRAINT pk_orders PRIMARY KEY (id)
);

create view "get not exp ordrs" AS
select id, product_id, customer_id, cost, order_date, expire_date
from "orders"
where getdate() < expire_date;

create materialized view get_orders_stats as
select order_date, count(*) as "count", sum(cost) as summary_cost
from orders
group by order_date;

create function getOrdCountPerCus(shopId int) AS
select c.name as customer_name, count(*) as "count"
from orders o
         LEFT JOIN products p ON o.product_id = p.id
         LEFT JOIN customers c ON o.customer_id = c.id
WHERE $shopId = p.shop_id
group by customer_name;

