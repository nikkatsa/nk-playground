package com.nikoskatsanos;

import com.nikoskatsanos.jutils.core.CloseableUtils;
import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import org.hibernate.tool.ant.Hbm2JavaExporterTask;
import org.hibernate.tool.ant.HibernateToolTask;
import org.hibernate.tool.ant.JDBCConfigurationTask;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>Simple example for programmatically generating JPA entity classes using <a href="http://hibernate.org/tools">Hibernate
 * Tools</a>. In this example an in-memory HSQL database is started with two simple tables. Those tables are generated
 * as JPA entity classes under <b>target/entities</b></p>
 *
 * @author nikkatsa
 */
public class HibernateToolsExampleRunner {
    private static final com.nikoskatsanos.nkjutils.yalf.YalfLogger log = com.nikoskatsanos.nkjutils.yalf.YalfLogger.getLogger(HibernateToolsExampleRunner.class);

    private static final String PACKAGE_NAME = "com.nikoskatsanos.entities";

    public static void main(final String... args) throws ClassNotFoundException, SQLException, ExecutionException, InterruptedException {
        log.info("**** Generating entity classes ****");

        ExecutorService dbRunnerDaemon = null;
        InMemoryDBRunner dbRunner = null;
        try {
            dbRunner = new InMemoryDBRunner("example_db");
            dbRunnerDaemon = Executors.newSingleThreadExecutor(new NamedThreadFactory("db-runner-daemon", true));
            final Connection dbConnection = dbRunnerDaemon.submit(dbRunner).get();

            final File destDir = new File("target/entities");
            final File hibernateConfig = new File("src/main/resources/hibernate.cfg.xml");

            final HibernateToolTask entityGenerator = new HibernateToolTask();
            entityGenerator.setDestDir(destDir);

            final JDBCConfigurationTask jdbcConfiguration = entityGenerator.createJDBCConfiguration();
            jdbcConfiguration.setConfigurationFile(hibernateConfig);
            jdbcConfiguration.setDetectManyToMany(true);
            jdbcConfiguration.setDetectOneToOne(true);
            jdbcConfiguration.setPackageName(PACKAGE_NAME);
            jdbcConfiguration.execute();

            final Hbm2JavaExporterTask hbm2Java = (Hbm2JavaExporterTask) entityGenerator.createHbm2Java();
            hbm2Java.setEjb3(true);
            hbm2Java.setDestdir(destDir);
            hbm2Java.validateParameters();
            hbm2Java.execute();

            log.info("**** Finished generating entity classes (%s)", destDir.getAbsolutePath());
        } finally {
            CloseableUtils.close(dbRunner);
            if (Objects.nonNull(dbRunnerDaemon) && !dbRunnerDaemon.isTerminated()) {
                dbRunnerDaemon.shutdownNow();
                dbRunnerDaemon.awaitTermination(2000L, TimeUnit.MILLISECONDS);
            }

            System.exit(0);
        }
    }
}
