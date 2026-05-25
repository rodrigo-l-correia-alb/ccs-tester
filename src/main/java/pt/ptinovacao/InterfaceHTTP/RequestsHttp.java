package pt.ptinovacao.InterfaceHTTP;

//import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class RequestsHttp implements Requests {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RequestsHttp.class);

    private final HttpClient httpClient;

    private final Logger logger;

    public RequestsHttp(Logger logger) {

        // new
        new Configurations();

        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        // Disable the validation of the server host name at the TLS level.
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        sslContextFactory.setTrustAll(true);

        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        this.httpClient = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        //this.httpClient = new HttpClient();

        this.logger = logger;

        try {
            this.httpClient.start();
            logger.fine("Starting http client...");
        } catch (Exception e) {
            logger.severe("Couldn't start http client. Exception thrown" + e);
        }
    }

    public void stopHttpClient() {
        // Stop HttpClient from a new thread.
        // Use LifeCycle.stop(...) to rethrow checked exceptions as unchecked.
        logger.info("[RequestHttp]: Stoping httpClient");
        new Thread(() -> LifeCycle.stop(httpClient)).start();
    }

    @Override
    public boolean createAccount(String account) {
        boolean flag = false;

        Request request = this.httpClient.POST(Configurations.urlCreateAccount());

        request.headers(httpFields -> {
            httpFields.add("X-requestTs", Configurations.miliSecondToString_UTC(Configurations.timestampSecondsMinus1Month()));
            httpFields.add("X-requestId", "POSTMAN_RQID_" + Configurations.uuid());
            httpFields.add("Content-Type", "application/json");
            httpFields.add("Accept", "*/*");
        });

        String jsonBody = Configurations.parsingCreateAccountFile(account);

        request.body(new StringRequestContent("application/json", jsonBody));

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            if (response.getStatus() == 200) flag = true;

            logger.info(String.format("createAccount(%s)", account));
            logger.info("Http response with code: " + response.getStatus() + " Message: " + res);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return false;
        }

        return flag;
    }

    @Override
    public boolean deleteAccount(String account) {
        boolean flag = false;

        Request request = this.httpClient.newRequest(Configurations.urlDeleteAccount(account));
        request.method(HttpMethod.DELETE);

        request.body(new StringRequestContent("application/json", ""));

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            if (response.getStatus() == 200) flag = true;

            logger.info(String.format("deleteAccount(%s)", account) + "\nHttp response with code: " + response.getStatus() + " Message " + res);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return false;
        }
        return flag;
    }

    @Override
    public boolean createAgreement(String account, String msisdn) {
        boolean flag = false;

        Request request = this.httpClient.POST(Configurations.urlCreateAgreement(account));

        request.headers(httpFields -> {
            //httpFields.add("X-requestTs",Configurations.miliSecondToString_UTC(Configurations.timestampSecondsMinus1Month()));
            httpFields.add("X-PCF-REQUESTID", "REQ_ID_" + Configurations.uuid());
            httpFields.add("X-PCF-CORRELATIONID", "CORR_ID_" + Configurations.uuid());
            httpFields.add("X-PCF-REQUESTDATE", Configurations.miliSecondToString_UTC(Configurations.timestampSecondsMinus1Month()));
            httpFields.add("Content-Type", "application/json");
            httpFields.add("Accept", "*/*");
        });


        String jsonBody = Configurations.parsingCreateAgreementFile(account, msisdn);

        request.body(new StringRequestContent("application/json", jsonBody));


        //logger.info("jsonBody:\n"+jsonBody);

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            if (response.getStatus() == 200) flag = true;

            logger.info(String.format("createAgreement(%s, %s)", account, msisdn) + "\nHttp response with code: " + response.getStatus() + "\nMessage " + res);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return false;
        }

        return flag;
    }


    @Override
    public HashMap<String, String> onlineChargingData(String msisdn, String requestType, String session_id, String cc_request_number, String used, String requested, String timestamp, String serviceContextId) {
        HashMap<String, String> resultItems = new HashMap<>();
        String jsonBody = "{}";
        Request request = null;

        if (requestType == "INITIAL_REQUEST") {
            session_id = "POSTMAN_MOC_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
            request = this.httpClient.newRequest(Configurations.urlOnlineCharging());
            request.method(HttpMethod.POST);
            request.timeout(35L, TimeUnit.SECONDS);

            jsonBody = Configurations.parsingDataPolicyFileInitial(session_id, msisdn, requestType, cc_request_number, used, requested, timestamp, serviceContextId);

            used = "0";

            //System.out.println("INITIAL");
            System.out.println("SERVICE CONTEXT ID: " + serviceContextId);
        } else if (requestType == "UPDATE_REQUEST") {
            request = this.httpClient.newRequest(Configurations.urlOnlineCharging() + "/" + session_id + "/update");
            request.method(HttpMethod.POST);
            request.timeout(35L, TimeUnit.SECONDS);

            jsonBody = Configurations.parsingDataPolicyFileUpdate(session_id, msisdn, requestType, cc_request_number, used, requested, timestamp);
            //System.out.println("UPDATE");
        } else if (requestType == "TERMINATION_REQUEST") {
            request = this.httpClient.newRequest(Configurations.urlOnlineCharging() + "/" + session_id + "/delete");
            request.method(HttpMethod.POST);
            request.timeout(35L, TimeUnit.SECONDS);

            jsonBody = Configurations.parsingDataPolicyFileTermination(session_id, msisdn, requestType, cc_request_number, used, requested, timestamp);

            //System.out.println("TERMINATION");
        } else {
            System.out.println("Unexpected requestType: " + requestType);
        }


        request.headers(httpFields -> {
            httpFields.add("X-PCF-REQUESTDATE", Configurations.miliSecondToString_UTC(timestamp));
            httpFields.add("Content-Type", "application/json");
            httpFields.add("Accept", "*/*");
        });
        request.body(new StringRequestContent("application/json", jsonBody));
        //logger.info("jsonBody:\n"+jsonBody);
        logger.fine("Request URL:\n" + request.getURI());
        logger.fine("Request Body:\n" + jsonBody);
        logger.fine("Request Headers:\n" + request.getHeaders().toString());

        //System.out.println("LOGGGER LEVEL:"+logger.getLevel());
        //logger.fine("TEST FINE");
        //logger.info("TEST INFO");
        //logger.warning("TEST WARN");


        try {
            long startTime = System.currentTimeMillis();
            ContentResponse response = request.send();
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            System.out.println("Response Time: " + responseTime + " milliseconds");


            String res = new String(response.getContent());
            String jsonString = "";

            try {
                JSONObject jsonObject = new JSONObject(res);
                jsonString = jsonObject.toString(4);
            } catch (JSONException e) {
            }

            logger.fine("Response Body:\n" + jsonString);

            HttpFields resHeaders = response.getHeaders();
            logger.fine("Response Headers:\n" + resHeaders.toString());
            String sessionid = "";
            String resultCode = String.valueOf(response.getStatus());


            if (requestType != "TERMINATION_REQUEST" && !resultCode.equals("403")) {
                resultItems = Configurations.getResultItens(res, AgreementType.DATA);
            }


            resultItems.put("resultCode", String.valueOf(response.getStatus()));

            logger.info(String.format("onlineChargingData(%s, %s, %s, %s, %s, %s, %s, %s)",
                    msisdn,
                    requestType,
                    session_id,
                    cc_request_number,
                    used,
                    requested,
                    Configurations.miliSecondToString_UTC(timestamp),
                    serviceContextId));
            logger.info("Http response with code: " + response.getStatus() + " Message " + resultItems);

            if (requestType == "INITIAL_REQUEST") {
                String location = resHeaders.get("Location");

                //logger.info("location:\n"+location);

                // Find the last index of the backslash
                int lastIndex = location.lastIndexOf('/');
                sessionid = (lastIndex != -1) ? location.substring(lastIndex + 1) : location;
                //logger.info("sessionid:\n"+sessionid);
                resultItems.put("sessionId", sessionid);
            }

        } catch (InterruptedException | TimeoutException | ExecutionException | NullPointerException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return resultItems;
        }
        return resultItems;
    }

    @Override
    public HashMap<String, String> onlineChargingSMS(String msisdn, String destination, String session_id, String timestamp) {
        HashMap<String, String> resultCode = new HashMap<>();

        Request request = this.httpClient.newRequest(Configurations.urlOnlineCharging());
        request.method(HttpMethod.PUT);

        request.headers(httpFields -> {
            httpFields.add("X-QNT-Rqts", Configurations.miliSecondToString_UTC(timestamp));
            httpFields.add("X-QNT-Rqid", "POSTMAN_RQID_" + Configurations.uuid());
            httpFields.add("X-QNT-Cid", "POSTMAN_CID_" + Configurations.uuid());
            httpFields.add("Content-Type", "application/xml");
        });

        String xmlBody = Configurations.parsingSmsChargingFile(session_id, timestamp, msisdn, destination);
        request.body(new StringRequestContent("application/xml", xmlBody));

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());
            resultCode = Configurations.getResultItens(res, AgreementType.SMS);
            logger.info(String.format("onlineChargingSMS(%s, %s, %s, %s)",
                    msisdn,
                    destination,
                    session_id,
                    Configurations.miliSecondToString_UTC(timestamp)) +
                    "\nHttp response with code: " + response.getStatus() + " Message " + resultCode);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return resultCode;
        }
        return resultCode;
    }

    @Override
    public HashMap<String, String> onlineChargingVoice(String msisdn, String requestType, String requestNumber, String session_id, String timestamp, String imsCharging, String usedTime, String requestedTime) {
        boolean flag = false;
        HashMap<String, String> resultCode = new HashMap<>();

        Request request = this.httpClient.newRequest(Configurations.urlOnlineCharging());
        request.method(HttpMethod.PUT);

        request.headers(httpFields -> {
            httpFields.add("X-QNT-Rqts", Configurations.miliSecondToString_UTC(timestamp));
            httpFields.add("X-QNT-Rqid", "POSTMAN_RQID_" + Configurations.uuid());
            httpFields.add("X-QNT-Cid", "POSTMAN_CID_" + Configurations.uuid());
            httpFields.add("Content-Type", "application/xml");
        });

        String xmlBody = Configurations.parsingVoiceChargingFile(session_id, timestamp, msisdn, requestType, requestNumber, imsCharging, usedTime, requestedTime);


        request.body(new StringRequestContent("application/xml", xmlBody));
        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            resultCode = Configurations.getResultItens(res, AgreementType.VOICE);

            logger.info(String.format("onlineChargingVoice(%s, %s, %s, %s, %s, %s, %s, %s)",
                    msisdn,
                    requestType,
                    requestNumber,
                    session_id,
                    Configurations.miliSecondToString_UTC(timestamp),
                    imsCharging,
                    usedTime,
                    requestedTime) +
                    "\nHttp response with code: " + response.getStatus() + " Message " + resultCode);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return resultCode;
        }
        return resultCode;
    }

    @Override
    public boolean creditBucket(String account, String ammount, String startDate, String endDate, AgreementType type) {
        boolean flag = false;

        Request request = this.httpClient.POST(Configurations.urlCreditBucket_v1(account, type));

        request.headers(httpFields -> {
            httpFields.add("X-QNT-Rqts", Configurations.miliSecondToString_UTC(Configurations.timestampSecondsMinus1Month()));
            httpFields.add("X-QNT-Rqid", "POSTMAN_RQID_" + Configurations.uuid());
            httpFields.add("X-QNT-Cid", "POSTMAN_CID_" + Configurations.uuid());
            httpFields.add("Content-Type", "application/xml");
            httpFields.add("Accept", "application/xml");
        });

        if (type == AgreementType.DATA) {
            ammount = Configurations.convertMoneyToVol(ammount);
        } else {
            ammount = Configurations.convertMoneyToTime(ammount);
        }

        String xmlBody = Configurations.parsingCreditBucketFile(ammount,
                Configurations.miliSecondToString_UTC(startDate),
                Configurations.miliSecondToString_UTC(endDate));

        request.body(new StringRequestContent("application/xml", xmlBody));

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());
            if (response.getStatus() == 200) flag = true;

            logger.info(String.format("creditBucket(%s, %s, %s, %s, %s)",
                    account,
                    ammount,
                    Configurations.miliSecondToString_UTC(startDate),
                    Configurations.miliSecondToString_UTC(endDate),
                    type) +
                    "\nHttp response with code: " + response.getStatus() + " Message " + res);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
            return false;
        }
        return flag;
    }

    @Override
    public double getSaldo(String account, AgreementType type) {
        double saldo = 0;

        Request request = this.httpClient.newRequest(Configurations.urlGetSaldo(account));

        request.headers(httpFields -> {
            httpFields.add("X-QNT-Rqts", Configurations.miliSecondToString_UTC(Configurations.timestampSecondsMinus1Month()));
            httpFields.add("X-QNT-Rqid", "POSTMAN_RQID_" + Configurations.uuid());
            httpFields.add("X-QNT-Cid", "POSTMAN_CID_" + Configurations.uuid());
        });

        request.method(HttpMethod.GET);

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            saldo = Configurations.getSaldo(res, type);
            if (type == AgreementType.DATA) {
                saldo = Configurations.convertVolToMoney(saldo);
            } else {
                saldo = Configurations.convertTimeToMoney(saldo);
            }
            logger.info(String.format("getSaldo(%s, %s)", account, type)
                    + "\nHttp response with code: " + response.getStatus() + " Saldo " + saldo);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
        }
        return saldo;
    }

    public void notifyESR(String account, double newQuota, int amount) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        Request request = this.httpClient.POST(Configurations.getUrlESR());
        request.timeout(35L, TimeUnit.SECONDS);

        request.headers(httpFields -> {
            httpFields.add("Content-Type", "application/xml");
            httpFields.add("Accept", "*/*");
        });

        String xmlBody = Configurations.parsingESRFile(account, newQuota, amount);

        request.body(new StringRequestContent("application/xml", xmlBody));

        try {
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            logger.info("ESR Notification for account: " + account + " with response code: " + response.getStatus());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.severe("Exception Thrown while trying to send request " + e);
        }


    }

}
