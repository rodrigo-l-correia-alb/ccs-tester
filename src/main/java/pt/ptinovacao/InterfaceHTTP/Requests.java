package pt.ptinovacao.InterfaceHTTP;

import java.util.HashMap;

public interface Requests {

    boolean createAccount(String account);

    boolean deleteAccount(String account);

    boolean createAgreement(String account, String msisdn);

    HashMap<String, String> onlineChargingData(String msisdn, String requestType, String session_id, String cc_request_number, String used, String requested, String timestamp, String serviceContextId);

    HashMap<String, String> onlineChargingSMS(String msisdn, String destination, String session_id, String timestamp);

    HashMap<String, String> onlineChargingVoice(String msisdn, String requestType, String requestNumber, String session_id, String timestamp, String imsCharging, String usedTime, String requestedTime);

    boolean creditBucket(String account, String amount, String startDate, String endDate, AgreementType type);

    double getSaldo(String account, AgreementType type);

}
