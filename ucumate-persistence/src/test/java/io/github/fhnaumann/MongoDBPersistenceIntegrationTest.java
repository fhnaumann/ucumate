package io.github.fhnaumann;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.providers.MongoDBPersistenceProvider;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Felix Naumann
 */
@Testcontainers
public class MongoDBPersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    MongoClient client;

    @Override
    protected void registerPersistenceProvider() {
        client = MongoClients.create(mongo.getReplicaSetUrl());
        PersistenceRegistry.register("mongo", new MongoDBPersistenceProvider(client, "ucumate"));
    }

    @Override
    protected void clearDatabaseState() {
        var db = client.getDatabase("ucumate");
        db.getCollection("ucumate_canonical").drop();
        db.getCollection("ucumate_validate").drop();
    }
}
