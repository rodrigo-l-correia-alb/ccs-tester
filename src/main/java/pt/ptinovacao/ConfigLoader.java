package pt.ptinovacao;

// import java.io.FileInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConfigLoader {

    private static final boolean verb = false;

    private static String convertYamlToJson(String yaml) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static JSONObject load(URL path_to_schema, String target_jsonString) throws IOException {
        try (InputStream is = path_to_schema.openStream()) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(is));
            Schema schema = SchemaLoader.load(rawSchema);
            JSONObject target_json = new JSONObject(target_jsonString);

            //Overwrite usage_pattern with their actual values (not their names)
            for (Object usage_pattern : target_json.getJSONArray("usage_patterns")) {

                for (Object client_group : target_json.getJSONArray("client_groups")) {
                    for (Object service : ((JSONObject) client_group).getJSONObject("client_type").getJSONArray("services")) {
                        if (((JSONObject) service).get("usage_pattern") instanceof String &&
                                ((JSONObject) service).getString("usage_pattern").equals(((JSONObject) usage_pattern).getString("name"))) {
                            ((JSONObject) service).put("usage_pattern", usage_pattern);
                        }
                    }
                    JSONObject topup = ((JSONObject) client_group).getJSONObject("client_type").getJSONObject("topup");

                    if (topup.get("usage_pattern") instanceof String &&
                            topup.getString("usage_pattern").equals(((JSONObject) usage_pattern).getString("name"))) {
                        topup.put("usage_pattern", usage_pattern);
                    }

                }

            }
            target_json.remove("usage_patterns");
            // System.out.println(target_json.toString(4));
            schema.validate(target_json); // throws a ValidationException if this object is invalid
            return target_json;
        }
    }

    public static JSONObject loadConfig(URL path_to_schema, URL path_to_target) throws URISyntaxException {
        try {
            String content = "";
            // content = new String(Files.readAllBytes(Paths.get(
            // content = path_to_target.openStream();
            content = new String(path_to_target.openStream().readAllBytes(), StandardCharsets.UTF_8);
            if (verb) {
                // System.out.println("*********Content from YAML File ****************");
                // System.out.println(content);
            }
            String json_str = convertYamlToJson(content);
            if (verb) {
                // System.out.println("*********Cnverted JSON from YAML File ****************");
                // System.out.println(json_str);
            }
            return load(path_to_schema, json_str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // ...

}
