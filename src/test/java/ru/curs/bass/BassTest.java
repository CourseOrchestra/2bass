package ru.curs.bass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.jdbc.SqlUtils;
import ru.curs.celesta.dbutils.meta.DbColumnInfo;
import ru.curs.celesta.dbutils.meta.DbFkInfo;
import ru.curs.celesta.dbutils.meta.DbPkInfo;
import ru.curs.celesta.dbutils.meta.DbSequenceInfo;
import ru.curs.celesta.score.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;

public abstract class BassTest {

    Bass bass;
    final MockConsoleHelper ch = new MockConsoleHelper();

    @BeforeEach
    void beforeEach() throws Exception {
        bass = new Bass(getProperties(), ch);
    }

    @AfterEach
    void afterEach() throws Exception {
        bass.close();
        bass = null;
        assertEquals(0, ch.activePhaseCount);
    }

    AppProperties getProperties() {
        AppProperties properties = new AppProperties();
        String scorePath1 = getClass().getResource("appTestScores/applyScore/s1").getPath();
        String scorePath2 = getClass().getResource("appTestScores/applyScore/s2").getPath();
        properties.setScorePath(scorePath1 + File.pathSeparator + scorePath2);
        return properties;
    }


    @Test
    void testInit() throws Exception {
        bass.initSystemSchema();
        DBAdaptor dbAdaptor = bass.getDbAdaptor();
        assertSysSchema(dbAdaptor);
    }

    @Test
    void testApply() throws Exception {
        this.bass.updateDb();
        DBAdaptor dbAdaptor = bass.getDbAdaptor();
        assertSysSchema(dbAdaptor);
        assertSchema(dbAdaptor);

        //Test records in schema table
        try (
                Connection conn = bass.getConnectionPool().get();
                CallContext callContext = new CallContext(dbAdaptor, conn, bass.getScore());
        ) {
            SchemaDataAccessor accessor = new SchemaDataAccessor(callContext, false);
            assertAll(
                    () -> assertTrue(accessor.nextInSet()),
                    () -> assertEquals(Score.SYSTEM_SCHEMA_NAME, accessor.getId()),
                    () -> assertTrue(accessor.nextInSet()),
                    () -> assertEquals("market", accessor.getId()),
                    () -> assertFalse(accessor.nextInSet())
            );
        }
    }

    @Test
    void testPlan() throws Exception {
        bass.initSystemSchema();
        DBAdaptor dbAdaptor = bass.getDbAdaptor();
        assertSysSchema(dbAdaptor);
        bass.close();

        AppProperties appProperties = getProperties();
        appProperties.setCommand(App.Command.PLAN);
        bass = new Bass(appProperties, ch);
        bass.outputDdlScript();

        ConsoleDdlConsumer ddlConsumer = (ConsoleDdlConsumer) bass.getDdlConsumer();

        dbAdaptor = bass.getDbAdaptor();
        try (Connection conn = bass.getConnectionPool().get()) {
            for (String sql : ddlConsumer.getAllStatements()) {
                if ("commit".equalsIgnoreCase(sql)) {
                    conn.commit();
                } else {
                    SqlUtils.executeUpdate(conn, sql);
                }
            }

        }

        assertSchema(dbAdaptor);

        //Test that no new records were created in schemas table.
        try (
                Connection conn = bass.getConnectionPool().get();
                CallContext callContext = new CallContext(dbAdaptor, conn, bass.getScore());
        ) {
            SchemaDataAccessor accessor = new SchemaDataAccessor(callContext, false);
            assertAll(
                    () -> assertTrue(accessor.nextInSet()),
                    () -> assertFalse(accessor.nextInSet())
            );
            accessor.get(Score.SYSTEM_SCHEMA_NAME);
            assertEquals(Score.SYSTEM_SCHEMA_NAME, accessor.getId());
        }
    }

    private void assertSysSchema(DBAdaptor dbAdaptor) throws Exception {
        try (Connection conn = bass.getConnectionPool().get()) {
            assertTrue(dbAdaptor.tableExists(conn, "bass", "schemas"));
        }
    }

    private void assertSchema(DBAdaptor dbAdaptor) throws Exception {
        String schemaName = "market";
        Score s = this.bass.getScore();
        Grain g = s.getGrain(schemaName);
        try (Connection conn = bass.getConnectionPool().get()) {
            assertAll(
                    () -> assertTrue(dbAdaptor.sequenceExists(conn, schemaName, "customers numerator")),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "customers")),
                    () -> assertTrue(dbAdaptor.sequenceExists(conn, schemaName, "shops_numerator")),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "shops")),
                    () -> assertTrue(dbAdaptor.sequenceExists(conn, schemaName, "products_numerator")),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "products")),
                    () -> assertTrue(dbAdaptor.sequenceExists(conn, schemaName, "orders_numerator")),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "orders")),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "get_orders_stats")),
                    () -> assertEquals(Arrays.asList("get not exp ordrs"), dbAdaptor.getViewList(conn, g)),
                    () -> assertEquals(Arrays.asList("getOrdCountPerCus"), dbAdaptor.getParameterizedViewList(conn, g)),
                    () -> assertTrue(dbAdaptor.tableExists(conn, schemaName, "get_orders_stats"))
            );

            //test customers.sql
            DbSequenceInfo customersNumerator = dbAdaptor.getSequenceInfo(
                    conn, g.getElement("customers numerator", SequenceElement.class)
            );
            assertAll(
                    () -> assertEquals(1, customersNumerator.getIncrementBy()),
                    () -> assertEquals(1, customersNumerator.getMinValue()),
                    () -> assertEquals(Long.MAX_VALUE, customersNumerator.getMaxValue()),
                    () -> assertFalse(customersNumerator.isCycle())
            );

            Table customers = g.getElement("customers", Table.class);

            DbColumnInfo customersId = dbAdaptor.getColumnInfo(conn, customers.getColumn("id"));
            DbColumnInfo customersName = dbAdaptor.getColumnInfo(conn, customers.getColumn("name"));
            DbPkInfo customersPk = dbAdaptor.getPKInfo(conn, customers);

            assertAll(
                    //"id"
                    () -> assertEquals("id", customersId.getName()),
                    () -> assertEquals(IntegerColumn.class, customersId.getType()),
                    () -> assertFalse(customersId.isNullable()),
                    () -> assertEquals("NEXTVAL(customers numerator)", customersId.getDefaultValue()),
                    () -> assertEquals(0, customersId.getLength()),
                    () -> assertFalse(customersId.isMax()),
                    //"name"
                    () -> assertEquals("name", customersName.getName()),
                    () -> assertEquals(StringColumn.class, customersName.getType()),
                    () -> assertTrue(customersName.isNullable()),
                    () -> assertEquals("", customersName.getDefaultValue()),
                    () -> assertEquals(30, customersName.getLength()),
                    () -> assertFalse(customersId.isMax()),
                    //"pk_customers"
                    //for dbs that do not support schemas
                    () -> assertTrue(Arrays.asList("pk_customers", "pk_customers_market")
                            .contains(customersPk.getName())),
                    () -> assertEquals(Arrays.asList("id"), customersPk.getColumnNames())
            );

            //test z_products.sql
            Table products = g.getElement("products", Table.class);

            DbColumnInfo productsShopId = dbAdaptor.getColumnInfo(conn, products.getColumn("shop_id"));
            DbColumnInfo productsCost = dbAdaptor.getColumnInfo(conn, products.getColumn("cost"));
            DbColumnInfo productsCount = dbAdaptor.getColumnInfo(conn, products.getColumn("count"));
            DbFkInfo productsShopIdFk = dbAdaptor.getFKInfo(conn, g).stream().filter(fk -> fk.getTableName().equals("products")).findFirst().get();

            assertAll(
                    //"shop_id"
                    () -> assertEquals("shop_id", productsShopId.getName()),
                    () -> assertEquals(IntegerColumn.class, productsShopId.getType()),
                    () -> assertTrue(productsShopId.isNullable()),
                    () -> assertEquals("", productsShopId.getDefaultValue()),
                    () -> assertEquals(0, productsShopId.getLength()),
                    () -> assertFalse(productsShopId.isMax()),
                    //"cost"
                    () -> assertEquals("cost", productsCost.getName()),
                    () -> assertEquals(FloatingColumn.class, productsCost.getType()),
                    () -> assertTrue(productsCost.isNullable()),
                    () -> assertEquals("", productsCost.getDefaultValue()),
                    () -> assertEquals(0, productsCost.getLength()),
                    () -> assertFalse(productsCost.isMax()),
                    //"count"
                    () -> assertEquals("count", productsCount.getName()),
                    () -> assertEquals(IntegerColumn.class, productsCount.getType()),
                    () -> assertTrue(productsCount.isNullable()),
                    () -> assertEquals("", productsCount.getDefaultValue()),
                    () -> assertEquals(0, productsCount.getLength()),
                    () -> assertFalse(productsCount.isMax()),
                    //"pk_customers"
                    () -> assertEquals("products", productsShopIdFk.getTableName()),
                    () -> assertEquals("market", productsShopIdFk.getRefGrainName()),
                    () -> assertEquals("shops", productsShopIdFk.getRefTableName()),
                    () -> assertEquals(Arrays.asList("shop_id"), productsShopIdFk.getColumnNames())
            );


            //test orders.sql
            Table orders = g.getElement("orders", Table.class);

            DbColumnInfo ordersOrderDate = dbAdaptor.getColumnInfo(conn, orders.getColumn("order_date"));
            DbColumnInfo ordersExpireDate = dbAdaptor.getColumnInfo(conn, orders.getColumn("expire_date"));

            MaterializedView mv = g.getElement("get_orders_stats", MaterializedView.class);

            DbColumnInfo mvOrderDate = dbAdaptor.getColumnInfo(conn, mv.getColumn("order_date"));
            DbColumnInfo mvCount = dbAdaptor.getColumnInfo(conn, mv.getColumn("count"));
            DbColumnInfo mvSummaryCost = dbAdaptor.getColumnInfo(conn, mv.getColumn("summary_cost"));


            assertAll(
                    //"orders" columns
                    //"order_date"
                    () -> assertEquals("order_date", ordersOrderDate.getName()),
                    () -> assertEquals(DateTimeColumn.class, ordersOrderDate.getType()),
                    () -> assertFalse(ordersOrderDate.isNullable()),
                    () -> assertEquals("", ordersOrderDate.getDefaultValue()),
                    () -> assertEquals(0, ordersOrderDate.getLength()),
                    () -> assertFalse(ordersOrderDate.isMax()),
                    //"expire_date"
                    () -> assertEquals("expire_date", ordersExpireDate.getName()),
                    () -> assertEquals(DateTimeColumn.class, ordersExpireDate.getType()),
                    () -> assertTrue(ordersExpireDate.isNullable()),
                    () -> assertEquals("", ordersExpireDate.getDefaultValue()),
                    () -> assertEquals(0, ordersExpireDate.getLength()),
                    () -> assertFalse(ordersExpireDate.isMax()),
                    //"get_orders_stats" columns
                    //"order_date"
                    () -> assertEquals("order_date", mvOrderDate.getName()),
                    () -> assertEquals(DateTimeColumn.class, mvOrderDate.getType()),
                    () -> assertFalse(mvOrderDate.isNullable()),
                    () -> assertEquals("", mvOrderDate.getDefaultValue()),
                    () -> assertEquals(0, mvOrderDate.getLength()),
                    () -> assertFalse(mvOrderDate.isMax()),
                    //"count"
                    () -> assertEquals("count", mvCount.getName()),
                    () -> assertEquals(IntegerColumn.class, mvCount.getType()),
                    () -> assertTrue(mvCount.isNullable()),
                    () -> assertEquals("", mvCount.getDefaultValue()),
                    () -> assertEquals(0, mvCount.getLength()),
                    () -> assertFalse(mvCount.isMax()),
                    //"summary_cost"
                    () -> assertEquals("summary_cost", mvSummaryCost.getName()),
                    () -> assertEquals(FloatingColumn.class, mvSummaryCost.getType()),
                    () -> assertTrue(mvSummaryCost.isNullable()),
                    () -> assertEquals("", mvSummaryCost.getDefaultValue()),
                    () -> assertEquals(0, mvSummaryCost.getLength()),
                    () -> assertFalse(mvSummaryCost.isMax())
            );
        }
    }
}
