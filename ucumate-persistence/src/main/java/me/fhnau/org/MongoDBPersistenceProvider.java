package me.fhnau.org;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.funcs.printer.Printer;
import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.persistence.PersistenceProvider;
import me.fhnau.org.util.PreciseDecimal;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author Felix Naumann
 */
public class MongoDBPersistenceProvider implements PersistenceProvider {

    private final MongoCollection<Document> canonicalColl;
    private final MongoCollection<Document> validationColl;

    public MongoDBPersistenceProvider(MongoClient client, String dbName) {
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
    public void saveValidated(String key, Validator.ValidationResult value) {
        Document doc = new Document("key", key)
                .append("valid", value instanceof Validator.Success);
        validationColl.replaceOne(eq("key", key), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public Validator.ValidationResult getValidated(String key) {
        Document doc = validationColl.find(eq("key", key)).first();
        if (doc == null) {
            return null;
        }

        boolean valid = doc.getBoolean("valid", false);
        if (valid) {
            return new Validator.Success(Validator.parseByPassChecks(key));
        } else {
            return new Validator.Failure();
        }
    }

    @Override
    public void close() {
        // todo figure out if handling necessary
    }
}
