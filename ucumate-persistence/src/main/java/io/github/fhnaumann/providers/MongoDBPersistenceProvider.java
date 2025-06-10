package io.github.fhnaumann.providers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.persistence.PersistenceProvider;
import io.github.fhnaumann.util.PreciseDecimal;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author Felix Naumann
 */
public class MongoDBPersistenceProvider implements PersistenceProvider {

    private final MongoClient client;
    private final MongoCollection<Document> canonicalColl;
    private final MongoCollection<Document> validationColl;

    public MongoDBPersistenceProvider(MongoClient client, String dbName) {
        this.client = client;
        MongoDatabase db = client.getDatabase(dbName);
        this.canonicalColl = db.getCollection("ucumate_canonical");
        this.validationColl = db.getCollection("ucumate_validate");
    }

    @Override
    public void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
        String keyString = UCUMService.print(key, Printer.PrintType.UCUM_SYNTAX);
        Document doc = new Document("unit_key", keyString)
                .append("magnitude", value.magnitude().toString())
                .append("cfPrefix", value.cfPrefix().toString())
                .append("term", UCUMService.print(value.term(), Printer.PrintType.UCUM_SYNTAX))
                .append("special", value.specialHandlingActive());

        if (value.specialHandlingActive()) {
            var func = value.specialFunction();
            doc.append("specialName", func.name())
                    .append("specialUnit", func.unit())
                    .append("specialValue", func.value().toString());
        }

        canonicalColl.replaceOne(eq("unit_key", keyString), doc, new ReplaceOptions().upsert(true));
    }


    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
        String keyString = UCUMService.print(key, Printer.PrintType.UCUM_SYNTAX);
        Document doc = canonicalColl.find(eq("unit_key", keyString)).first();
        if (doc == null) {
            return null;
        }
        PreciseDecimal magnitude = new PreciseDecimal(doc.getString("magnitude"));
        PreciseDecimal cfPrefix = new PreciseDecimal(doc.getString("cfPrefix"));
        UCUMExpression.Term term = Validator.parseCanonical(doc.getString("term"));
        boolean special = doc.getBoolean("special", false);
        UCUMDefinition.UCUMFunction func = null;

        if (special) {
            func = new UCUMDefinition.UCUMFunction(
                    doc.getString("specialName"),
                    new PreciseDecimal(doc.getString("specialValue")),
                    doc.getString("specialUnit")
            );
        }

        return new Canonicalizer.CanonicalStepResult(term, magnitude, cfPrefix, special, func);
    }

    @Override
    public Map<UCUMExpression, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        Map<UCUMExpression, Canonicalizer.CanonicalStepResult> resultMap = new HashMap<>();

        for (Document doc : canonicalColl.find()) {
            String unitKey = doc.getString("unit_key");
            String termStr = doc.getString("term");

            UCUMExpression key = Validator.parseByPassChecks(unitKey);
            UCUMExpression.Term term = Validator.parseCanonical(termStr);
            PreciseDecimal magnitude = new PreciseDecimal(doc.getString("magnitude"));
            PreciseDecimal cfPrefix = new PreciseDecimal(doc.getString("cfPrefix"));

            boolean special = doc.getBoolean("special", false);
            UCUMDefinition.UCUMFunction func = null;

            if(special) {
                String name = doc.getString("specialName");
                String unit = doc.getString("specialUnit");
                String value = doc.getString("specialValue");
                func = new UCUMDefinition.UCUMFunction(name, new PreciseDecimal(value), unit);
            }

            Canonicalizer.CanonicalStepResult result = new Canonicalizer.CanonicalStepResult(
                    term, magnitude, cfPrefix, special, func
            );

            resultMap.put(key, result);
        }

        return resultMap;
    }

    @Override
    public void saveValidated(String key, Validator.ValidationResult value) {
        Document doc = new Document("key", key)
                .append("valid", value instanceof Validator.Success);
        validationColl.replaceOne(eq("key", key), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public Validator.ValidationResult getValidated(String key) {
        Document doc = validationColl.find(eq("key", key)).first();
        if(doc == null) {
            return null;
        }

        boolean valid = doc.getBoolean("valid", false);
        if(valid) {
            return new Validator.Success(Validator.parseByPassChecks(key));
        } else {
            return new Validator.Failure();
        }
    }

    @Override
    public Map<String, Validator.ValidationResult> getAllValidated() {
        Map<String, Validator.ValidationResult> resultMap = new HashMap<>();

        for(Document doc : validationColl.find()) {
            String key = doc.getString("unit_key");
            boolean valid = doc.getBoolean("valid", false);

            Validator.ValidationResult result = valid
                    ? new Validator.Success(Validator.parseByPassChecks(key))
                    : new Validator.Failure();

            resultMap.put(key, result);
        }

        return resultMap;
    }

    @Override
    public void close() {
        client.close();
    }
}
