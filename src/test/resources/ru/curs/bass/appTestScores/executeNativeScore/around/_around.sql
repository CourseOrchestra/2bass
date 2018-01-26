CREATE SCHEMA around version '1.0';

EXECUTE NATIVE H2 BEFORE --{{
  create table "around"."t1"(
    val BIGINT
  );
--}};

EXECUTE NATIVE H2 AFTER --{{
  alter table "around"."t1" rename to "t2"
--}};