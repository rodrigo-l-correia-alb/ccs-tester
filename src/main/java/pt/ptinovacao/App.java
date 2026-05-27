package pt.ptinovacao;

import org.json.JSONObject;

import java.net.URL;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;

public class App {

    public static void main(String[] args) throws Exception {

        // Validate and load configuration yaml
        URL path_to_schema = App.class.getClassLoader().getResource("conf_schema.json");
        URL path_to_target = null;
        if (args.length > 0) {
            path_to_target = App.class.getClassLoader().getResource(args[0]);
        } else {
            path_to_target = App.class.getClassLoader().getResource("conf_DADOS.yaml");
        }

        JSONObject config = ConfigLoader.loadConfig(path_to_schema, path_to_target);

        // Setup and run teste
        boolean delayedRequests = false;
        ConfigSetup configSetup = new ConfigSetup(config, delayedRequests);
        //configSetup.setupPHR();
        //configSetup.setupExtractor();
        //configSetup.setupGateway();

        long start = System.currentTimeMillis();

        configSetup.setupSim();
        //configSetup.deleteAccounts();
        OffsetDateTime init = OffsetDateTime.now(UTC);


        configSetup.testStart();

        configSetup.stopHttpClient();
        configSetup.closeLDAP();

        long end = System.currentTimeMillis();
        long interval = end - start;
        long minutes = (interval / 1000) / 60;
        System.out.println("Time elapsed " + interval / 1000 + " s; " + minutes + " minutes");
        System.out.println("init: " + init + " end: " + OffsetDateTime.now(UTC));
        System.out.println("NumberOfRequests: " + configSetup.getNumberOfRequests());
        System.out.println("Number of getSaldo Requests " + configSetup.getNumberOfAmountRequests());
    }

}
