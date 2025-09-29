package com.prosoft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


@Component
public class CassandraSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CassandraSchemaInitializer.class);

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspace;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing Cassandra schema for keyspace: {}", keyspace);

        executeCqlScript("db/schema.cql");
        executeCqlScript("db/data.cql");

        logger.info("Cassandra schema initialization completed.");
    }

    private void executeCqlScript(String scriptPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder query = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                query.append(line);

                if (line.endsWith(";")) {
                    String cql = query.toString().trim();
                    logger.info("Executing CQL: {}", cql);
                    cassandraTemplate.getCqlOperations().execute(cql);
                    query.setLength(0);
                } else {
                    query.append(" ");
                }
            }
        }
    }
}
