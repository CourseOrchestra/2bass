CREATE SCHEMA before version '1.0';

EXECUTE NATIVE H2 BEFORE --{{
  create table "before"."t"(
    val BIGINT
  );
--}};