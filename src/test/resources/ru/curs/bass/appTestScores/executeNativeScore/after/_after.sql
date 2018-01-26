CREATE SCHEMA after version '1.0';

EXECUTE NATIVE H2 AFTER --{{
  create table "after"."t"(
    val BIGINT
  );
--}};