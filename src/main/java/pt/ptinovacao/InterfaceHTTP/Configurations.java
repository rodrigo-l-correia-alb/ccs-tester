package pt.ptinovacao.InterfaceHTTP;


import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pt.ptinovacao.App;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class Configurations {

    private static Boolean externalclients;

    private static String urlCreateAccount;

    private static String urlDeleteAccount; // = "https://pcf-orchprovisioning-service.ccpcicdsubs.apps2.ocp.dev.alticelabs.com:443/account/";

    private static String urlCreateAgreement; // = "https://pcf-orchprovisioning-service.ccpcicdsubs.apps2.ocp.dev.alticelabs.com:443/product";

    private static String urlCreateClientLdap; // = "ldap://10.112.232.158:1389";

    //   private static String urlOnlineCharging; // = "http://pcf-smpolicycontrol-orch.ccpcicdsubs.apps2.ocp.dev.alticelabs.com:80/npcf-smpolicycontrol/v1/sm-policies";      //ccpcicdsubs
    private static String urlOnlineCharging; // = "http://pcf-smpolicycontrol-orch.phr.apps2.ocp.dev.alticelabs.com:80/npcf-smpolicycontrol/v1/sm-policies";                // phr

    private static String urlCreditBucket; // = "http://qnt-dev-sat1.c.ptin.corppt.com:9101/ocs-gcc/customer-accounts/";

    private static String urlGetSaldo; // = "http://qnt-dev-sat1.c.ptin.corppt.com:9101/ocs-gcc/customer-accounts/";

    private static String urlLDAP;

    private static String urlESR;


    // Default constructor
    public Configurations() {
        // Change URL between PHR and CCPCICDSUBS
        URL path_to_conf_env = App.class.getClassLoader().getResource("conf_env_phr.json");

        try {
            // Read the content of the file into a String
            String content;
            content = new String(Files.readAllBytes(Paths.get(path_to_conf_env.toURI())));

            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            System.out.println("Loaded JSON: " + jsonObject.toString(4)); // Pretty print JSON 

            externalclients = jsonObject.getBoolean("externalclients");
            urlDeleteAccount = jsonObject.getJSONObject("endpoints").getString("urlDeleteAccount");
            urlCreateAccount = jsonObject.getJSONObject("endpoints").getString("urlCreateAccount");
            urlCreateAgreement = jsonObject.getJSONObject("endpoints").getString("urlCreateAgreement");
            urlCreateClientLdap = jsonObject.getJSONObject("endpoints").getString("urlCreateClientLdap");
            urlOnlineCharging = jsonObject.getJSONObject("endpoints").getString("urlOnlineCharging");
            urlCreditBucket = jsonObject.getJSONObject("endpoints").getString("urlCreditBucket");
            urlGetSaldo = jsonObject.getJSONObject("endpoints").getString("urlGetSaldo");
            urlLDAP = jsonObject.getJSONObject("endpoints").getString("urlCreateClientLdap");
            urlESR = jsonObject.getJSONObject("endpoints").getString("urlESR");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String parsingCreateAccountFile(String account) {
        String jsonString = "";
        //String fileCreateAccount = "src/main/resources/createAccount.xml";
        String fileCreateAccount = "src/main/resources/createAccount.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileCreateAccount)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars
            jsonObject.put("id", account);
            jsonObject.put("billingAccountId", account);

            JSONArray jsonArrayCharacteristics = jsonObject.getJSONArray("characteristics");
            JSONObject characteristics0 = jsonArrayCharacteristics.getJSONObject(0);
            characteristics0.put("value", "Account Name " + account);

            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        return jsonString;
    }


    public static String parsingCreateAgreementFile(String account, String msisdn) {
        String jsonString = "";
        String fileCreateAgreement = "src/main/resources/createAgreement.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileCreateAgreement)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars    
            //Setting Agreement & Account
            String agreementId = createAgreement(account);
            jsonObject.put("id", agreementId);
            jsonObject.put("accountId", account);

            //Setting Session Policies
            JSONArray jsonArrayProductPolicies = jsonObject.getJSONArray("productPolicies");
            JSONObject productPolicy0 = jsonArrayProductPolicies.getJSONObject(0);
            productPolicy0.put("id", "65f090ce4169065b15f891bf-" + msisdn);
            JSONArray jsonArrayProductPolicyCharacteristics = productPolicy0.getJSONArray("productPolicyCharacteristics");
            JSONObject productPolicyCharacteristics0 = jsonArrayProductPolicyCharacteristics.getJSONObject(0);
            productPolicyCharacteristics0.put("value", msisdn);

            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }


    public static String parsingCreditBucketFile(String amount, String start_Date, String end_Date) {
        String xmlString;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String fileCreditBucket = "src/main/resources/creditBucket.xml";

        try (InputStream is = new FileInputStream(fileCreditBucket)) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            //Setting Amount
            NodeList amountNode = doc.getElementsByTagName("amount");
            amountNode.item(0).setTextContent(amount);

            Element startDate = (Element) doc.getElementsByTagName("start_date").item(0);
            startDate.setTextContent(start_Date);
            Element endDate = (Element) doc.getElementsByTagName("end_date").item(0);
            endDate.setTextContent(end_Date);


            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            xmlString = writer.getBuffer().toString();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return xmlString;
    }

    public static String parsingCreditBucketFile_v2(String amount) {
        String xmlString;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String fileCreditBucket = "src/main/resources/creditBucket.xml";

        try (InputStream is = new FileInputStream(fileCreditBucket)) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            //Setting Amount
            NodeList amountNode = doc.getElementsByTagName("amount");
            amountNode.item(0).setTextContent(amount);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            xmlString = writer.getBuffer().toString();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return xmlString;
    }


    public static String parsingDataPolicyFileInitial(String rqid, String msisdn, String cc_request_type, String cc_request_number, String used, String requested, String timestamp, String service) {
        String jsonString = "";
        String fileOnlineCharging = "src/main/resources/policyInitial.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileOnlineCharging)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars    

            //Setting SERVICE
            jsonObject.put("dnn", service);

            //Setting gpsi
            jsonObject.put("gpsi", msisdn);
            //Setting notificationUri
            UUID uuid = UUID.randomUUID();
            jsonObject.put("notificationUri", "http://127.0.0.1:8089/notification/v1/sm-policies/" + uuid);
            // Setting pei
            jsonObject.put("pei", "imeisv-" + msisdn);
            // Setting supi
            jsonObject.put("supi", "imsi-" + msisdn);

            // userLocationInfoTime
            jsonObject.put("userLocationInfoTime", Instant.ofEpochSecond(Long.valueOf(timestamp) - 2208988800L));


            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static String parsingDataPolicyFileUpdate(String rqid, String msisdn, String cc_request_type, String cc_request_number, String used, String requested, String timestamp) {
        String jsonString = "";
        String fileOnlineCharging = "src/main/resources/policyUpdate.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileOnlineCharging)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars    
            // userLocationInfoTime
            jsonObject.put("userLocationInfoTime", Instant.ofEpochSecond(Long.valueOf(timestamp) - 2208988800L));
            //accuUsageReports
            JSONArray accuUsageReports = jsonObject.getJSONArray("accuUsageReports");
            JSONObject accuUsageReports0 = accuUsageReports.getJSONObject(0);
            // volUsage
            accuUsageReports0.put("volUsage", used);
            // volUsageUplink
            accuUsageReports0.put("volUsageUplink", 0);
            // volUsageDownlink
            accuUsageReports0.put("volUsageDownlink", used);

            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }


    public static String parsingDataPolicyFileTermination(String rqid, String msisdn, String cc_request_type, String cc_request_number, String used, String requested, String timestamp) {
        String jsonString = "";
        String fileOnlineCharging = "src/main/resources/policyTermination.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileOnlineCharging)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars    
            // userLocationInfoTime
            jsonObject.put("userLocationInfoTime", Instant.ofEpochSecond(Long.valueOf(timestamp) - 2208988800L));
            //accuUsageReports
            JSONArray accuUsageReports = jsonObject.getJSONArray("accuUsageReports");
            JSONObject accuUsageReports0 = accuUsageReports.getJSONObject(0);
            // volUsage
            accuUsageReports0.put("volUsage", used);
            // volUsageUplink
            accuUsageReports0.put("volUsageUplink", 0);
            // volUsageDownlink
            accuUsageReports0.put("volUsageDownlink", used);


            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }


    public static String parsingDataChargingFile(String rqid, String msisdn, String cc_request_type, String cc_request_number, String used, String requested, String timestamp) {
        String jsonString = "";
        String fileOnlineCharging = "src/main/resources/onlineChargingInitial.json";

        try {
            // Read the content of the file into a String
            String content = new String(Files.readAllBytes(Paths.get(fileOnlineCharging)));
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(content);

            //replace vars    
            //Setting gpsi
            jsonObject.put("gpsi", msisdn);
            //Setting notificationUri
            UUID uuid = UUID.randomUUID();
            jsonObject.put("notificationUri", "http://127.0.0.1:8089/notification/v1/sm-policies/" + uuid);
            // Setting pei
            jsonObject.put("pei", "imeisv-" + msisdn);
            // Setting supi
            jsonObject.put("supi", "imsi-" + msisdn);


            // Print the JSON object
            jsonString = jsonObject.toString(2);
            //System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
/* 
            //Setting amount
            Element multipleServices = (Element) doc.getElementsByTagName("multiple-services-credit-control").item(0);
            Element requestedServiceUnit = (Element) multipleServices.getElementsByTagName("requested-service-unit").item(0);
            Element ccTotal_v1 = (Element) requestedServiceUnit.getElementsByTagName("cc-total-octets").item(0);
            ccTotal_v1.setTextContent(requested);

            Element userServiceUnit = (Element) multipleServices.getElementsByTagName("used-service-unit").item(0);
            Element ccTotal_v2 = (Element) userServiceUnit.getElementsByTagName("cc-total-octets").item(0);
            ccTotal_v2.setTextContent(used);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
*/

    }

    public static String parsingSmsChargingFile(String rqid, String timestamp, String msisdn, String destination) {
        String xmlString;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String fileOnlineCharging = "src/main/resources/sms_charging.xml";

        try (InputStream is = new FileInputStream(fileOnlineCharging)) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            //Setting RQID aka session-id
            NodeList sessionId = doc.getElementsByTagName("session-id");
            sessionId.item(0).setTextContent(rqid);

            //Setting cc_request_type
            NodeList ccCorrelationId = doc.getElementsByTagName("cc-correlation-id");
            ccCorrelationId.item(0).setTextContent("CID_POSTMAN_CID_" + uuid());

            //Setting cc_request_number
            NodeList eventTimestamp = doc.getElementsByTagName("event-timestamp");
            eventTimestamp.item(0).setTextContent(timestamp);

            //Setting msisdn
            Element subscriptionId = (Element) doc.getElementsByTagName("subscription-id").item(0);
            Element subscriptionIdData = (Element) subscriptionId.getElementsByTagName("subscription-id-data").item(0);
            subscriptionIdData.setTextContent(msisdn);

            Element serviceMsisdn = (Element) doc.getElementsByTagName("service-parameter-info").item(5);
            serviceMsisdn.getElementsByTagName("service-parameter-value").item(0).setTextContent("1" + msisdn);

            //Setting destination
            Element service = (Element) doc.getElementsByTagName("service-parameter-info").item(0);
            Element serviceValue = (Element) service.getElementsByTagName("service-parameter-value").item(0);
            serviceValue.setTextContent("1" + destination);

            Element serviceDestination = (Element) doc.getElementsByTagName("service-parameter-info").item(6);
            serviceDestination.getElementsByTagName("service-parameter-value").item(0).setTextContent("1" + destination);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            xmlString = writer.getBuffer().toString();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return xmlString;
    }

    public static String parsingVoiceChargingFile(String sessionId, String timestamp, String msisdn, String requestType, String requestNumber, String imsCharging, String usedTime, String requestedTime) {
        String xmlString;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String fileOnlineCharging = "";

        if (requestType.equals("UPDATE_REQUEST") || requestType.equals("TERMINATION_REQUEST")) {
            fileOnlineCharging = "src/main/resources/voice_charging_ut.xml";
        } else {
            fileOnlineCharging = "src/main/resources/voice_charging.xml";
        }

        try (InputStream is = new FileInputStream(fileOnlineCharging)) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            Element session_id = (Element) doc.getElementsByTagName("session-id").item(0);
            session_id.setTextContent(sessionId);

            Element cc_request_type = (Element) doc.getElementsByTagName("cc-request-type").item(0);
            cc_request_type.setTextContent(requestType);

            Element cc_request_number = (Element) doc.getElementsByTagName("cc-request-number").item(0);
            cc_request_number.setTextContent(requestNumber);

            Element cc_correlation_id = (Element) doc.getElementsByTagName("cc-correlation-id").item(0);
            cc_correlation_id.setTextContent(sessionId);

            Element event_timestamp = (Element) doc.getElementsByTagName("event-timestamp").item(0);
            event_timestamp.setTextContent(timestamp);

            Element subscriptionId = (Element) doc.getElementsByTagName("subscription-id").item(0);
            Element subscriptionIdData = (Element) subscriptionId.getElementsByTagName("subscription-id-data").item(0);
            subscriptionIdData.setTextContent(msisdn);

            Element service_information = (Element) doc.getElementsByTagName("service-information").item(0);
            Element ims_information = (Element) service_information.getElementsByTagName("ims-information").item(0);
            Element calling_party_address = (Element) ims_information.getElementsByTagName("calling-party-address").item(0);
            calling_party_address.setTextContent(msisdn);

            Element ims_charging_identifier = (Element) doc.getElementsByTagName("ims-charging-identifier").item(0);
            ims_charging_identifier.setTextContent(imsCharging);

            // Requested-service-unit
            Element multiple_services_credit_control = (Element) doc.getElementsByTagName("multiple-services-credit-control").item(0);
            Element requested_service_unit = (Element) multiple_services_credit_control.getElementsByTagName("requested-service-unit").item(0);
            Element cc_time = (Element) requested_service_unit.getElementsByTagName("cc-time").item(0);
            cc_time.setTextContent(requestedTime);

            //Used-service-unit
            if (requestType.equals("UPDATE_REQUEST") || requestType.equals("TERMINATION_REQUEST")) {
                Element used_service = (Element) multiple_services_credit_control.getElementsByTagName("used-service-unit").item(0);
                cc_time = (Element) used_service.getElementsByTagName("cc-time").item(0);
                cc_time.setTextContent(usedTime);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            xmlString = writer.getBuffer().toString();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return xmlString;
    }

    public static String urlCreateAccount() {
        URL url;
        try {
            url = new URL(urlCreateAccount);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(url);
    }

    public static String urlOnlineCharging() {
        URL url;
        try {
            url = new URL(urlOnlineCharging);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(url);
    }

    public static String getUrlLDAP() {
        return urlLDAP;
    }

    public static String urlCreateAgreement(String clientId) {
        String newURL = urlCreateAgreement + "/agreements";
        URL url;

        try {
            url = new URL(newURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(url);
    }

    public static String urlCreditBucket_v1(String account, AgreementType type) {
        String agreementType = "1";

        if (type == AgreementType.DATA) {
            agreementType = "0";
        }

        String newURL = urlCreditBucket + account + "/buckets/" + agreementType + "/credit";
        URL url;
        try {
            url = new URL(newURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(url);
    }

    public static String urlDeleteAccount(String account) {
        String newURL = urlDeleteAccount + account;
        URL url;
        try {
            url = new URL(newURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(url);
    }


    public static String urlGetSaldo(String account) {
        return urlGetSaldo + account + "/all";
    }

    public static String uuid() {
        String init = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
        char[] arrayChars = init.toCharArray();
        String result = "";

        for (int i = 0; i < arrayChars.length; i++) {
            int rand = (int) (Math.random() * 16) | 0;

            if (arrayChars[i] == '-') {
                result = result + arrayChars[i];
            } else {
                if (arrayChars[i] == 'y') rand = rand & 0x3 | 0x8;
                result = result + Integer.toHexString(rand);
            }
        }
        return result;
    }

    public static String createAgreement(String account) {
        String agreement = "";

        int rand = (int) Math.floor(Math.random() * 1000);
        agreement = agreement + account + "_" + rand;

        return agreement;
    }

    public static String currentTimeStamp() {
        OffsetDateTime offset = OffsetDateTime.now(UTC);
        return offset.format(ISO_INSTANT);
    }

    public static String timestampSeconds() {
        String timeS = "";
        Date date = new Date();
        System.out.println(date);

        long time = date.getTime();

        long timestamp = (time / 1000) + 2208988800L;

        return Long.toString(timestamp);
    }

    public static String timestampSecondsMinus1Month() {
        String timeS = "";
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.MONTH, -1);
        cal.add(Calendar.MONTH, -8);

        Date date = cal.getTime();

        long time = date.getTime();
        long timestamp = (time / 1000) + 2208988800L;

        return Long.toString(timestamp);
    }

    public static String timestampSecondsMinus1MonthPlus1s() {
        String timeS = "";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date date = cal.getTime();

        long time = date.getTime();
        long timestamp = (time / 1000) + 2208988800L + 1000;

        return Long.toString(timestamp);
    }

    public static String timestampSecondsMinus1Month(long plusTime) {
        String timeS = "";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date date = cal.getTime();

        long time = date.getTime();
        long timestamp = (time / 1000) + 2208988800L + plusTime;

        return Long.toString(timestamp);
    }

    public static String timestampSecondsPlus(long plusTime) {
        String timeS = "";
        Date date = new Date();
        System.out.println(date);

        long time = date.getTime();

        long timestamp = (time / 1000) + 2208988800L + plusTime;

        return Long.toString(timestamp);
    }

    public static String miliSecondToString_UTC(String date) {
        long time = Long.parseLong(date);

        long timestamp_v2 = (time - 2208988800L) * 1000 + 1;

        OffsetDateTime offsetTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp_v2), ZoneOffset.UTC);

        return offsetTime.format(ISO_INSTANT);
    }

    public static String timeStampToString(String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-Mm-yyyyThh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = formatter.parse(str);

        long time = date.getTime();
        long timestamp = (time / 1000) + 2208988800L;

        return Long.toString(timestamp);
    }

    public static String longTimeStampToString(Long time) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        //Date date = formatter.parse(str);

        //long time = date.getTime();
        long timestamp = (time / 1000) + 2208988800L;

        return Long.toString(timestamp);
    }

    public static String getResultCode(String response) {
        String responseCode = "";
        //System.out.println(response);
        if (response != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;

            try {
                builder = factory.newDocumentBuilder();

                //Parse the content to Document object
                Document doc = builder.parse(new InputSource(new StringReader(response)));
                doc.getDocumentElement().normalize();

                Element result_code = (Element) doc.getElementsByTagName("result-code").item(0);
                if (result_code == null) {
                    responseCode = "";
                } else {
                    responseCode = result_code.getTextContent();
                }
                //System.out.println(responseCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseCode;
    }

    public static Double getSaldo(String response, AgreementType type) {
        double saldo = 0.0;

        if (response != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;

            try {
                builder = factory.newDocumentBuilder();

                //Parse the content to Document object
                Document doc = builder.parse(new InputSource(new StringReader(response)));
                doc.getDocumentElement().normalize();

                Element buckets = (Element) doc.getElementsByTagName("buckets").item(0);
                NodeList miiBucketsList = buckets.getElementsByTagName("mii:bucket");

                for (int i = 0; i < miiBucketsList.getLength(); i++) {
                    Element miiEntry = (Element) miiBucketsList.item(i);
                    String specId = miiEntry.getElementsByTagName("spec_id").item(0).getTextContent();
                    String total_ammount = miiEntry.getElementsByTagName("total_amount").item(0).getTextContent();
                    String confirmed_amount = miiEntry.getElementsByTagName("confirmed_amount").item(0).getTextContent();

                    Double totalAmount = Double.valueOf(total_ammount) - Double.valueOf(confirmed_amount);
                    if (type.equals(AgreementType.DATA)) {
                        if (specId.equals("56b3263c705297722300158b")) return totalAmount;
                    } else {
                        if (specId.equals("56eaf53d70529789d400dced")) return totalAmount;
                    }
                }
            } catch (Exception e) {
                return saldo;
            }
        }
        return saldo;
    }

    public static String getGrantedService(String response) {
        String responseCode = "";

        if (response != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;

            try {
                builder = factory.newDocumentBuilder();

                //Parse the content to Document object
                Document doc = builder.parse(new InputSource(new StringReader(response)));
                doc.getDocumentElement().normalize();

                Element result_code = (Element) doc.getElementsByTagName("result-code").item(0);
                responseCode = result_code.getTextContent();
                //System.out.println(responseCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseCode;
    }

    public static HashMap getResultItens(String response, AgreementType type) {
        HashMap<String, String> responseParams = new HashMap<>();

        try {
            // Create a JSONObject from the String content
            JSONObject jsonObject = new JSONObject(response);
            //System.out.println("Reply JSON: " + jsonObject.toString(4)); // Pretty print JSON 

            JSONObject umDecs = jsonObject.getJSONObject("umDecs");

            Iterator<String> itUmDecs = umDecs.keys();

            while (itUmDecs.hasNext()) {
                String mk = itUmDecs.next();
                JSONObject mkJSONObject = umDecs.getJSONObject(mk);

                if (mkJSONObject.has("volumeThreshold")) {
                    BigInteger volumeThreshold = mkJSONObject.getBigInteger("volumeThreshold");
                    responseParams.put("grantedUnit", String.valueOf(volumeThreshold));
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block

        }
        return responseParams;
    }

    public static String convertMoneyToVol(String ammount) {
        double saldo = Double.valueOf(ammount);

        double bytes = 5368709120L * saldo;

        bytes = bytes / 200;

        // Remove decimal part
        Double d = Math.floor(bytes);
        // Do not use exponential notation
        BigDecimal bd = new BigDecimal(d);
        String bytesplain = bd.toPlainString(); // This avoids scientific notation

        return bytesplain;
    }

    public static String convertMoneyToTime(String ammount) {
        double saldo = Double.valueOf(ammount);

        double time = 200 * saldo;

        time = time / 200;

        return String.valueOf(time);
    }

    public static double convertTimeToMoney(double time) {

        double money = time * 200;

        money = money / 200;

        return money;
    }

    public static double convertVolToMoney(double bytes) {

        double money = bytes * 200;

        money = money / 5368709120L;

        return money;
    }

    public static boolean getexternalclients() {
        return externalclients;
    }

    public static String getUrlESR() {
        return urlESR;
    }

    public static String parsingESRFile(String account, double newQuota, int amount)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {

        String newMsgID = "id_" + account;
        String newServiceDn = "pcsServiceId=100,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        String beforeQuotaStr = decimalFormat.format(newQuota - amount);
        String afterQuotaStr = decimalFormat.format(newQuota);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Path path = Paths.get("src/main/resources/esrBodyRequest.xml");
        Document doc;
        try (InputStream is = Files.newInputStream(path)) {
            doc = db.parse(is);
        }
        doc.getDocumentElement().normalize();

        NodeList msgIdNodes = doc.getElementsByTagNameNS("urn:headerblock", "msgId");
        msgIdNodes.item(0).setTextContent(newMsgID);

        NodeList objectNodes = doc.getElementsByTagNameNS("http://www.apertio.com/pgw/trigger", "object");
        Element objectElem = (Element) objectNodes.item(0);
        objectElem.setAttribute("DN", newServiceDn);

        NodeList beforeValueNodes = doc.getElementsByTagNameNS("http://www.apertio.com/pgw/trigger", "beforeValue");
        beforeValueNodes.item(0).setTextContent(beforeQuotaStr);


        NodeList afterValueNodes = doc.getElementsByTagNameNS("http://www.apertio.com/pgw/trigger", "afterValue");
        afterValueNodes.item(0).setTextContent(afterQuotaStr);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

}
