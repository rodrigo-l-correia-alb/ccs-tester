package pt.ptinovacao;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import pt.ptinovacao.InterfaceHTTP.AgreementType;
import pt.ptinovacao.InterfaceHTTP.Configurations;
import pt.ptinovacao.InterfaceHTTP.Requests;
import pt.ptinovacao.InterfaceHTTP.RequestsHttp;
import pt.ptinovacao.interfaceLDAP.RequestsLDAP;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;


public class ConfigSetup {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConfigSetup.class);

    // private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConfigSetup.class);
    private JSONObject config;

    private List<ClientGroup> client_groups;

    private TimeDuration test_duration;

    private long testEndSeconds;

    private Requests requests;

    private Logger logger;

    private boolean delay;

    private int numberOfRequests = 0;

    private int numberOfAmountRequests = 0;

    private RequestsLDAP requestsLDAP;

    public ConfigSetup(JSONObject config, boolean delay) {
        ConfigSetupWrapped(config);
        this.delay = delay;
    }

    public ConfigSetup(JSONObject config) {
        ConfigSetupWrapped(config);
    }

    public void ConfigSetupWrapped(JSONObject config) {
        this.config = config;
        this.test_duration = new TimeDuration(config.getJSONObject("test_duration"));
        this.client_groups = new ArrayList<ClientGroup>();

        // Setup logging
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT.%1$tL] [%4$-7s] %5$s %n");
        this.logger = Logger.getLogger("ConfigSetup");

        //    Handler systemOut = new ConsoleHandler();

        Handler fileHandler = null;
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            fileHandler = new FileHandler("logs/phrTester_run_" + timestamp + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.setLevel(Level.INFO);
        fileHandler.setFormatter(new SimpleFormatter());

        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
        this.requests = new RequestsHttp(logger);
        this.requestsLDAP = new RequestsLDAP(logger);

    }


    public void closePHR() {
        conect_ssh("padmin", "padmin", "10.112.27.31", 22, "cd Heuristic-Response && python3 stop.py");
    }

    public void setupSim() {
        this.config.getJSONArray("client_groups").forEach(client_group -> {
            this.client_groups.add(new ClientGroup((JSONObject) client_group));
        });

        // In client_groups are created number_of_clients individual ClientType objects
        this.client_groups.forEach(client_group -> client_group.create_clients());

        // Provision clients
        this.client_groups.forEach(client_group -> provision(client_group));
    }

    public void deleteAccounts() {

        this.config.getJSONArray("client_groups").forEach(client_group -> {
            this.client_groups.add(new ClientGroup((JSONObject) client_group));
        });

        // In client_groups are created number_of_clients individual ClientType objects
        this.client_groups.forEach(client_group -> client_group.create_clients());

        this.client_groups.forEach(clientGroup -> deleteAccountsLDAP(clientGroup));
    }

    public void deleteAccountsLDAP(ClientGroup client_group) {

        // Clientes externos - LDAP
        client_group.getClients().forEach(client -> {
            requestsLDAP.deleteAccount(client.getAccount());
            requestsLDAP.deleteAccount(client.getAccount());
        });

    }

    private void provision(ClientGroup client_group) {
        String now = Configurations.timestampSecondsMinus1Month();
        String aYearFromNow = Configurations.timestampSecondsMinus1Month(31536000L);

        if (Configurations.getexternalclients()) {
            // Clientes externos - LDAP
            client_group.getClients().forEach(client -> {
                requestsLDAP.deleteAccount(client.getAccount());
                requestsLDAP.deleteAccount(client.getAccount());

                requestsLDAP.createAccount(client.getAccount());

//                client.getServices().forEach((service -> {
//                    switch (service.getServiceContexId()) {
//                        case "8.32251@3gpp.org", "N7", "SERVICE2", "SERVICE3":
                requestsLDAP.creditBucket(client.getAccount(),
                        String.valueOf(client.getInitialAmount().getRandomAmount()));
//                            break;
//                    }
//                }));

                //   Topup scheduling
                //tbd: avaliar
            });

        } else {
            // Clientes internos
            client_group.getClients().forEach(client -> {
                requests.deleteAccount(client.getAccount());
                requests.createAccount(client.getAccount());
                requests.createAgreement(client.getAccount(),
                        String.valueOf(client.getMsisdn()));

                client.getServices().forEach((service -> {
                    switch (service.getServiceContexId()) {
                        case "7.APC@telecom.pt":
                            requests.creditBucket(client.getAccount(),
                                    String.valueOf(client.getInitialAmount().getRandomAmount()),
                                    now,
                                    aYearFromNow,
                                    AgreementType.VOICE);
                            break;
                        case "8.32251@3gpp.org":
                            requests.creditBucket(client.getAccount(),
                                    String.valueOf(client.getInitialAmount().getRandomAmount()),
                                    now,
                                    aYearFromNow,
                                    AgreementType.DATA);
                            break;
                        case "message@huawei.com":
                            requests.creditBucket(client.getAccount(),
                                    String.valueOf(client.getInitialAmount().getRandomAmount()),
                                    now,
                                    aYearFromNow,
                                    AgreementType.SMS);
                            break;
                    }
                }));

                //   Topup scheduling
                if (!client.isReactive() && client.getTopup() != null) {
                    try {
                        runService(client, null, client.getAccount(), client.getMsisdn(), client.isReactive(), client.getTopupAmmount());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void testStart() {
        System.out.println("-------------------------------------------------------\n" +
                "T E S T   S T A R T\n" +
                "-------------------------------------------------------");
        List<Callable<Integer>> tasks = createTasks(client_groups);
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());

        try {
            executorService.invokeAll(tasks);
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopHttpClient();
        closeLDAP();

        System.out.println("-------------------------------------------------------\n" +
                "T E S T   E N D\n" +
                "-------------------------------------------------------");

    }

    private List<Callable<Integer>> createTasks(List<ClientGroup> clientGroups) {
        List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        for (ClientGroup clientGroup : clientGroups) {
            for (ClientType client : clientGroup.getClients()) {
                logger.finer(String.format("client.getAccount(): %s, client.getMsisdn(): %s", client.getAccount(), client.getMsisdn()));

                boolean isReactive = client.isReactive();
                Service topup = client.getTopup();
                int topupAmmount = topup.getUsagePattern().getAmmount();

                for (Service service : client.getServices()) {
                    tasks.add(() -> {
                        try {
                            System.out.println("Starting service for account: " + client.getAccount() + " msisdn: " + client.getMsisdn() + " serviceContextId: " + service.getServiceContexId());
                            runService(client, service, client.getAccount(), client.getMsisdn(), isReactive, topupAmmount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    });
                }
            }
        }
        return tasks;
    }

    private void runService(ClientType clientType, Service service, String account, int msisdn_int, boolean isReactive, int credit_ammount) throws InterruptedException, ParseException, ParserConfigurationException, IOException, TransformerException, SAXException {
        UsagePattern usagePattern = service.getUsagePattern();
        long startTime = Long.valueOf(Configurations.timestampSecondsMinus1Month());

        if (usagePattern.hasMaxSessionDuration()) {
            runSessionService(clientType, service, usagePattern, account, msisdn_int, startTime, isReactive, credit_ammount);
        } else if (usagePattern.hasRequest_interval()) {
            runSessionLessService(clientType, service, usagePattern, account, msisdn_int, startTime, isReactive, credit_ammount);
        }

        logger.fine("Thread terminated.\n");
    }

    private void runSessionService(ClientType clientType, Service service, UsagePattern usagePattern, String account, int msisdn_int, long currentPseudoTime, boolean isReactive, int credit_ammount) throws ParseException, ParserConfigurationException, IOException, TransformerException, SAXException {

        testEndSeconds = currentPseudoTime + test_duration.getSeconds();
        logger.info("testStartSeconds:" + Configurations.miliSecondToString_UTC(String.valueOf(currentPseudoTime)));
        logger.info("testEndSeconds:" + Configurations.miliSecondToString_UTC(String.valueOf(testEndSeconds)));

        double minimumBalanceMoney = clientType.getTopup().getUsagePattern().getRequestInterval().getSaldo();
        String minimumBalanceStr = Configurations.convertMoneyToVol(String.valueOf(minimumBalanceMoney));
        double minimumBalance = Double.valueOf(minimumBalanceStr);

        Topup topup = new Topup(0L, false, minimumBalance, currentPseudoTime);


        String msisdn = String.valueOf(msisdn_int);
        long requestInterval;
        long forseenAwake;
        Map<String, String> args = new HashMap<String, String>() {{
            put("isReactive", String.valueOf(isReactive));
            put("serviceContextId", service.getServiceContexId());
            put("msisdn", msisdn);
            put("requestType", "INITIAL_REQUEST");
            put("requestNumber", "0");
            //put("sessionId", "");            
            put("sessionId", "POSTMAN_MOC_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
            put("timestamp", "");
            put("used", "0");
            put("destination", "someDestination");
            put("imsCharging", "1");
            put("account", account);
            put("credit_ammount", String.valueOf(credit_ammount));
            put("requested", "0");
            put("usedTime", "0");
            put("gsu", "0");

        }};


        while (true) {
            long finalCurrentPseudoTime = currentPseudoTime;
            long pseudoSessionEnd = usagePattern.getMaxSessionDuration().getSeconds_Chi();


            long sessionEnd = currentPseudoTime + pseudoSessionEnd;
            logger.fine("SessionEnd = " + Configurations.miliSecondToString_UTC(String.valueOf(sessionEnd)));

            if (sessionEnd > this.testEndSeconds) {
                logger.warning("Session duration exceeds the test duration limit. Therefore, the session will not be started for accountId = {" + args.get("msisdn") + " }.");
                logger.warning("Session end timestamp = " + Configurations.miliSecondToString_UTC(String.valueOf(sessionEnd)) +
                        "> Test end timestamp = " + Configurations.miliSecondToString_UTC(String.valueOf(this.testEndSeconds)));
                break;
            }

            ResponseType sessionIsEnding = ResponseType.CONTINUE;
            int requestNumber = 1;

            args.put("requestType", "INITIAL_REQUEST");
            args.put("used", "0");
            args.put("requestNumber", String.valueOf(requestNumber));
            args.put("timestamp", String.valueOf(finalCurrentPseudoTime));

            logger.fine("pseudoSessionEnd=" + pseudoSessionEnd + ", account=" + clientType.getAccount());
            logger.fine("Session End = " + Configurations.miliSecondToString_UTC(String.valueOf(sessionEnd)) + " [" + sessionEnd + "]");
            logger.fine("Test End  = " + Configurations.miliSecondToString_UTC(String.valueOf(this.testEndSeconds)) + " [" + testEndSeconds + "]");


            sessionIsEnding = sendRequest(clientType, service, args, topup, currentPseudoTime);

            if (sessionIsEnding.equals(ResponseType.RETRY)) {

                long pseudoSessionBtwInterval = usagePattern.getSessionInterval().getSeconds_Chi();

                currentPseudoTime = currentPseudoTime + pseudoSessionBtwInterval;
                //topupInterval[0]=0L;
            }

            if (topup.isTopupExecuted() && currentPseudoTime >= topup.getTopupExecutionTimestamp()) {
                logger.info("CurrentPseudoTime = " + Configurations.miliSecondToString_UTC(String.valueOf(currentPseudoTime)));
                logger.info("TopupExecutionTimestamp = " + Configurations.miliSecondToString_UTC(String.valueOf(topup.getTopupExecutionTimestamp())));
                logger.info("Executing scheduled top-up.");
                creditBucket(clientType, topup);
            }

            requestNumber++;
            long consumptionRate = 0;

            while (true) {
                consumptionRate = usagePattern.getConsumptionRate().getSeconds_Chi();
                //logger.info("Consump"+consumptionRate);
                long GSU;

                if (args.get("gsu") != null) {
                    GSU = Long.parseLong(args.get("gsu"));
                    requestInterval = Math.round(GSU / consumptionRate);
                    args.put("used", args.get("gsu"));

                    logger.info("GSU=" + GSU + ", consumptionRate=" + consumptionRate + " bytes/s Intervalo entre pedido=" + requestInterval);
                } else {
                    forseenAwake = currentPseudoTime;
                    break;
                }


                if (requestInterval + currentPseudoTime > sessionEnd && args.get("gsu") != null) {
                    if (GSU != 0) {
                        requestInterval = sessionEnd - currentPseudoTime;
                    } else {
                        requestInterval = 0;
                    }
                    long used = consumptionRate * requestInterval;

                    args.put("used", Long.toString(used));


                    logger.info("[Truncated] GSU= " + args.get("gsu") + ", used = " + args.get("used") + " consumptionRate= " + consumptionRate + " bytes/s Intervalo entre pedido = " + requestInterval);
                }


                forseenAwake = requestInterval + currentPseudoTime;


                logger.fine("ForseenAwake = " + Configurations.miliSecondToString_UTC(String.valueOf(forseenAwake)) + " [" + forseenAwake + "]");
                logger.fine("Session End = " + Configurations.miliSecondToString_UTC(String.valueOf(sessionEnd)) + " [" + sessionEnd + "]");
                logger.fine("Test End  = " + Configurations.miliSecondToString_UTC(String.valueOf(this.testEndSeconds)) + " [" + testEndSeconds + "]");

                // Ultima sessão
                if (sessionIsEnding.equals(ResponseType.RETRY)) {

                    long pseudoSessionBtwInterval = usagePattern.getSessionInterval().getSeconds_Chi();

                    currentPseudoTime = forseenAwake + pseudoSessionBtwInterval;
                    //topupInterval[0]=0L;

                    break;
                } else if (forseenAwake >= this.testEndSeconds) {
                    args.put("requestType", "TERMINATION_REQUEST");
                    args.put("requestNumber", String.valueOf(requestNumber));
                    args.put("timestamp", String.valueOf(sessionEnd));

                    sessionIsEnding = sendRequest(clientType, service, args, topup, currentPseudoTime);

                    currentPseudoTime = this.testEndSeconds;
                    logger.info("Last session for client with accountId: {" + args.get("msisdn") + " }");
                    break;
                } else if (forseenAwake >= sessionEnd) {
                    args.put("requestType", "TERMINATION_REQUEST");
                    args.put("requestNumber", String.valueOf(requestNumber));
                    args.put("timestamp", String.valueOf(sessionEnd));


                    sessionIsEnding = sendRequest(clientType, service, args, topup, currentPseudoTime);

                    long pseudoSessionBtwInterval = usagePattern.getSessionInterval().getSeconds_Chi();

                    currentPseudoTime = sessionEnd + pseudoSessionBtwInterval;

                    forseenAwake = currentPseudoTime;
                    logger.fine("CurrentPseudoTime = " + Configurations.miliSecondToString_UTC(String.valueOf(currentPseudoTime)) + " [" + currentPseudoTime + "]");
                    logger.fine("Terminate Session!");
                    logger.fine("PseudoSessionBtwInterval = " + pseudoSessionBtwInterval);

                    break;
                }
                // Final da sessão
                else if (sessionIsEnding.equals(ResponseType.TERMINATE_SESSION)) {
                    args.put("requestType", "TERMINATION_REQUEST");
                    args.put("requestNumber", String.valueOf(requestNumber));

                    args.put("timestamp", String.valueOf(currentPseudoTime));

                    sendRequest(clientType, service, args, topup, currentPseudoTime);
                    //logger.info("RunSessionService: TOP UP INTERVAL: "+topupInterval[0]);

                    long pseudoSessionBtwInterval = usagePattern.getSessionInterval().getSeconds_Chi();

                    currentPseudoTime = forseenAwake + pseudoSessionBtwInterval;
                    //topupInterval[0]=0L;

                    break;
                } else {
                    args.put("requestType", "UPDATE_REQUEST");
                    args.put("requestNumber", String.valueOf(requestNumber));
                    args.put("timestamp", String.valueOf(forseenAwake));
                    sessionIsEnding = sendRequest(clientType, service, args, topup, currentPseudoTime);


                    currentPseudoTime = forseenAwake;

                }


                if (topup.isTopupExecuted() && currentPseudoTime >= topup.getTopupExecutionTimestamp()) {
                    logger.info("CurrentPseudoTime = " + Configurations.miliSecondToString_UTC(String.valueOf(currentPseudoTime)));
                    logger.info("TopupExecutionTimestamp = " + Configurations.miliSecondToString_UTC(String.valueOf(topup.getTopupExecutionTimestamp())));
                    logger.info("Executing scheduled top-up.");

                    creditBucket(clientType, topup);

                }

                requestNumber++;
            }

            if (topup.isTopupExecuted() && currentPseudoTime >= topup.getTopupExecutionTimestamp()) {
                logger.info("CurrentPseudoTime = " + Configurations.miliSecondToString_UTC(String.valueOf(currentPseudoTime)));
                logger.info("TopupExecutionTimestamp = " + Configurations.miliSecondToString_UTC(String.valueOf(topup.getTopupExecutionTimestamp())));
                logger.info("Executing scheduled top-up.");

                creditBucket(clientType, topup);

            }

            if (forseenAwake >= testEndSeconds) {
                logger.fine("forseenAwake>=testEndSeconds");
                logger.fine("Forseenwake = " + Configurations.miliSecondToString_UTC(String.valueOf(forseenAwake)));
                logger.info("Last session for client with accountId: {" + args.get("msisdn") + " }");
                break;
            }

        }
    }

    private void runSessionLessService(ClientType client, Service service, UsagePattern usagePattern, String account, int msisdn_int, long currentPseudoTime,
                                       boolean isReactive, int credit_ammount) throws ParserConfigurationException, IOException, TransformerException, SAXException {

        testEndSeconds = currentPseudoTime + test_duration.getSeconds();
        long[] topupInterval = {0L};
        long requestInterval;
        long forseenAwake;
        long finalCurrentPseudoTime = currentPseudoTime;
        String msisdn = String.valueOf(msisdn_int);

        double minimumBalanceMoney = client.getTopup().getUsagePattern().getRequestInterval().getSaldo();


        String minimumBalanceStr = Configurations.convertMoneyToVol(String.valueOf(minimumBalanceMoney));
        double minimumBalance = Double.valueOf(minimumBalanceStr);

        Topup topup = new Topup(0L, false, minimumBalance, currentPseudoTime);

        Map<String, String> args = new HashMap<String, String>() {{
            put("isReactive", String.valueOf(isReactive));
            put("serviceContextId", service.getServiceContexId());
            put("msisdn", msisdn);
            put("requestType", "");
            put("requestNumber", "");
            put("sessionId", "POSTMAN_MOC_" + Configurations.uuid());
            put("timestamp", String.valueOf(finalCurrentPseudoTime));
            put("used", String.valueOf(usagePattern.getAmmount()));
            put("destination", "someDestination");
            put("imsCharging", "1");
            put("account", account);
            put("credit_ammount", String.valueOf(credit_ammount));
            put("requested", String.valueOf(usagePattern.getAmmount()));
        }};
        sendRequest(client, service, args, topup, currentPseudoTime);

        while (true) {
            requestInterval = usagePattern.getRequestInterval().getSeconds();
            forseenAwake = requestInterval + currentPseudoTime;

            if (forseenAwake >= this.testEndSeconds) {
                args.put("sessionId", "POSTMAN_MOC_" + Configurations.uuid());
                args.put("timestamp", String.valueOf(this.testEndSeconds));
                sendRequest(client, service, args, topup, currentPseudoTime);

                currentPseudoTime = this.testEndSeconds;
                break;
            } else {
                args.put("sessionId", "POSTMAN_MOC_" + Configurations.uuid());
                args.put("timestamp", String.valueOf(forseenAwake));
                sendRequest(client, service, args, topup, currentPseudoTime);

                currentPseudoTime = forseenAwake;
            }
        }
    }

    private ResponseType sendRequest(ClientType client, Service service, Map<String, String> args, Topup topup, long currentPseudoTime) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        HashMap<String, String> response = new HashMap<>();
        String requestType = "";
        //String response = null;


        switch (args.get("serviceContextId")) {
            //case "8.32251@3gpp.org": // DATA
            case "8.32251@3gpp.org", "N7", "SERVICE2", "SERVICE3": // DATA                                 
                requestType = args.get("requestType");


                response = requests.onlineChargingData(args.get("msisdn"),
                        args.get("requestType"),
                        args.get("sessionId"),
                        args.get("requestNumber"),
                        args.get("used"),
                        args.get("requested"),
                        args.get("timestamp"),
                        args.get("serviceContextId"));

                if (requestType.equals("TERMINATION_REQUEST") || response.get("grantedUnit") == null) {
                    args.put("gsu", "0");
                } else {
                    args.put("gsu", response.get("grantedUnit"));
                }

                // set sessionId
                if (requestType.equals("INITIAL_REQUEST")) {
                    args.put("sessionId", response.get("sessionId"));
                    if (args.get("sessionId") == null) {
                        args.put("gsu", response.get("grantedUnit"));
                    }
                }


                this.numberOfRequests += 1;
                log.info("NUMBER OF REQUEST = ", numberOfRequests);
                if (numberOfRequests == 125) {
                    log.info("NUMBER OF REQUEST = ", numberOfRequests);
                }

                //reactive topup
                checkBalance(client, topup, currentPseudoTime);

                break;

            case "message@huawei.com": // SMS
                response = requests.onlineChargingSMS(args.get("msisdn"),
                        args.get("destination"),
                        args.get("sessionId"),
                        args.get("timestamp"));
                this.numberOfRequests += 1;
                break;


            case "7.APC@telecom.pt": // VOICE
                //double gsu = Double.parseDouble(args.get("used"));
                double interval = Double.parseDouble(args.get("usedTime"));
                logger.info(" Used: " + interval);

                response = requests.onlineChargingVoice(args.get("msisdn"),
                        args.get("requestType"),
                        args.get("requestNumber"),
                        args.get("sessionId"),
                        args.get("timestamp"),
                        args.get("imsCharging"),
                        args.get("usedTime"),
                        args.get("requested"));
                this.numberOfRequests += 1;
                //checkBalance(client,service);
                break;

            case "topup@ptinovacao.pt":
                // TODO: Diferenciar qual o tipo de serviço para o qual devemos dar topup
                String now = args.get("timestamp");
                String aYearFromNow = String.valueOf(Long.valueOf(args.get("timestamp")) + 31556926L);

                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.DATA);
                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.VOICE);
                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.SMS);

                break;
        }

        // ldrs are generated on termination so it makes sense to wait only when the ldr is being produced.
        if (this.delay) {
            if (args.get("requestType") == "TERMINATION_REQUEST" ||
                    args.get("requestType") == "UPDATE_REQUEST" ||
                    args.get("serviceContextId") == "message@huawei.com") {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return respond(client, service, ResponseType.getResponseType(response.get("resultCode"), args.get("gsu"), args.get("requestType")), args, topup, currentPseudoTime);

    }

    public void checkBalance(ClientType clientType, Topup topup, long currentPseudoTime) throws ParserConfigurationException, IOException, TransformerException, SAXException {

        double saldo = requestsLDAP.getSaldo(clientType.getAccount());
        logger.info("AvailableQuota: " + new BigDecimal(saldo));

        this.numberOfAmountRequests++;

        if (topup.getMinimumBalance() >= saldo) {
            logger.info("AvailableQuota(" + new BigDecimal(saldo) + ") < minimum availableQuota(" + new BigDecimal(topup.getMinimumBalance()) + ")");

            if (saldo <= 0 && !topup.isTopupExecuted()) {
                topup.setTopupExecuted(true);
                topup.setTopupInterval(clientType.getTopup().getUsagePattern().getTopupInterval().getSeconds());
                topup.setTopupExecutionTimestamp(currentPseudoTime);
                topup.addTopupInterval();

                logger.info("Top-up scheduled to be executed in {" + topup.getTopupInterval() + "} ms");
            } else if (!topup.isTopupExecuted()) {
                logger.info("Reactive Top-up!");
                creditBucket(clientType, topup);
            }
        }
    }

    public void creditBucket(ClientType clientType, Topup topup) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        int amount = clientType.getTopup().getUsagePattern().getTopAmount().getRandomAmount();
        String account = clientType.getAccount();

        requestsLDAP.creditBucket(account, String.valueOf(amount));
        double newQuota = requestsLDAP.getSaldo(account);
        logger.info("Credit after topup = { " + new BigDecimal(newQuota) + " }");

        // Novo valor para minimo saldo da recarga
        double minimumBalanceMoney = clientType.getTopup().getUsagePattern().getRequestInterval().getSaldo();
        String minimumBalanceStr = Configurations.convertMoneyToVol(String.valueOf(minimumBalanceMoney));
        double minimumBalance = Double.valueOf(minimumBalanceStr);

        topup.setMinimumBalance(minimumBalance);
        topup.setTopupExecuted(false);
        topup.setTopupInterval(0L);

// TODO: Habilitar quando for preciso que o PCF seja notificado a respeito da recarga. De modo a forçar a notif ao service criteria e a geração do update do tipo "reavaluate"
//        ((RequestsHttp) this.requests).notifyESR(account, newQuota, amount);
    }

    private ResponseType respond(ClientType clientType, Service service, ResponseType responseType, Map<String, String> args, Topup topup, long currentPseudoTime) {
        if (responseType != null) {
            switch (responseType) {

                // Reactive
                case TOPUP: {
                    if (args.get("isReactive").equals("true")) {
                        String now = args.get("timestamp");
                        String aYearFromNow = String.valueOf(Long.valueOf(args.get("timestamp")) + 31556926L);

                        switch (args.get("serviceContextId")) {
                            case "8.32251@3gpp.org", "N7", "SREVICE2", "SERVICE3": // DATA
                                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.DATA);
                                return ResponseType.CONTINUE;
                            case "message@huawei.com": // SMS
                                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.SMS);
                                return ResponseType.CONTINUE;
                            case "7.APC@telecom.pt": // VOICE
                                requests.creditBucket(args.get("account"), args.get("credit_ammount"), now, aYearFromNow, AgreementType.VOICE);
                                return ResponseType.CONTINUE;
                        }
                    } else {
                        //Not reactive topup so we don't send creditBucket, but we still terminate session
                        return ResponseType.CONTINUE;
                    }

                }
                case RETRY: {
                    //sendRequest(clientType, service, args, topup,currentPseudoTime);
                    return responseType;
                }
                case TERMINATE_SESSION:
                    return responseType;

                case CONTINUE: {
                    return responseType;
                }
                default:
                    return responseType;
            }
        } else {
            return responseType;
        }
    }

    private static void conect_ssh(String username, String password,
                                   String host, int port, String command) {

        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.setErrStream(responseStream);
            channel.connect();

            while (channel.isConnected()) {
                Thread.sleep(100);
            }

            String responseString = responseStream.toString();
            System.out.println(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public void stopHttpClient() {
        ((RequestsHttp) this.requests).stopHttpClient();
    }

    public void closeLDAP() {
        this.requestsLDAP.closeLDAP();
    }

    public int getNumberOfAmountRequests() {
        return numberOfAmountRequests;
    }

    public int getNumberOfRequests() {
        return this.numberOfRequests;
    }

}