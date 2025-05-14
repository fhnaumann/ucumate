package org.example;

import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Demo {

    public static void main(String[] args) throws SQLException {
        /*
        /*
        UcumateStore.storageProvider = new RelationalDatabaseStorageProvider(DriverManager.getConnection(
                "jdbc:mariadb://127.0.0.1:3306/ucumate-demo",
                "root",
                "root"
        ));
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        UcumateStore.storageProvider = new DocumentDatabaseStorageProvider(client, "ucum", "expressions");
        UCUMRegistry registry = UCUMRegistry.getInstance();
        Expression.Term meter = SoloTermBuilder.builder()
                                               .withoutPrefix(registry.getUCUMUnit("m").orElseThrow())
                                               .asComponent()
                                               .withExponent(3)
                                               .withAnnotation("abc")
                                               .asTerm()
                                               .build();
        Expression.Term feet = SoloTermBuilder.builder()
                                              .withoutPrefix(registry.getUCUMUnit("[ft_i]").orElseThrow())
                                              .noExpNoAnnot()
                                              .asTerm()
                                              .build();

        Expression.Term term = (Expression.Term) Main.visitTerm("m3{abc}/(cs{d}.[ft_i]-2)");

        Canonicalizer canonicalizer = new Canonicalizer();
        org.example.model.Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalizeNoSpecialUnitAllowed(
                term);
        System.out.println(result);

         */

    }
}
