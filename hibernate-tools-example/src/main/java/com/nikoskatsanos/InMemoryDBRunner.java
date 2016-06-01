package com.nikoskatsanos;

import com.nikoskatsanos.jutils.core.CloseableUtils;
import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import org.hsqldb.server.Server;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>Class which starts an in memory db server and creates some tables</p>
 *
 * @author nikkatsa
 */
public class InMemoryDBRunner implements Callable<Connection>, Closeable {
    private static final YalfLogger log = YalfLogger.getLogger(InMemoryDBRunner.class);

    private final String dbName;
    private final String dbUrl;
    private Connection dbConnection;

    private final ExecutorService serverExecutor;

    public InMemoryDBRunner(final String dbName) {
        this.dbName = dbName;
        this.dbUrl = String.format("jdbc:hsqldb:hsql://localhost/%s", dbName);
        this.serverExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("HQSL-Server", true));
    }

    @Override
    public Connection call() throws Exception {

        this.serverExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Server.main(new String[]{"--database.0", String.format("file:%s", dbName), "--dbname.0", dbName});
                log.info("HQSL server started");
                return null;
            }
        }).get();


        this.dbConnection = DriverManager.getConnection(this.dbUrl, "SA", "");

        this.createDBTables();

        return this.dbConnection;
    }

    private void createDBTables() throws SQLException {

        final Statement statement = this.dbConnection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS Users ( " +
                "UserId INTEGER IDENTITY NOT NULL, " +
                "Name VARCHAR(32) NOT NULL," +
                "PRIMARY KEY (UserId) )");
        CloseableUtils.close(statement);
        log.info("Users table created");

        final Statement addressesStmt = this.dbConnection.createStatement();
        addressesStmt.execute("CREATE TABLE IF NOT EXISTS Address ( " +
                "AddressId INTEGER IDENTITY NOT NULL, " +
                "AddressLine1 VARCHAR(32) NOT NULL, " +
                "AddressLine2 VARCHAR(32), " +
                "City VARCHAR(32) NOT NULL, " +
                "PostCode VARCHAR(10) NOT NULL, " +
                "UserId INTEGER NOT NULL, " +
                "PRIMARY KEY (AddressId), " +
                "FOREIGN KEY (UserId) REFERENCES Users(UserId) )");
        CloseableUtils.close(addressesStmt);
        log.info("Address table created");
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.close(this.dbConnection);

        if (!this.serverExecutor.isShutdown()) {
            this.serverExecutor.shutdownNow();
            try {
                this.serverExecutor.awaitTermination(2000L, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                log.warn("Could not shutdown HQSL server thread", e);
            }
        }
    }

    public static void main(final String... args) throws InterruptedException {
        final InMemoryDBRunner dbRunner = new InMemoryDBRunner("example_db");
        Executors.newSingleThreadExecutor().submit(dbRunner);
        Thread.currentThread().join();
    }
}
