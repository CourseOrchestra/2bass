SET SCHEMA market;

create sequence orders_numerator;

create table orders(
  id int DEFAULT NEXTVAL(products_numerator) not null,
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