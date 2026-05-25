package pt.ptinovacao;

// import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Definitions {

    public static final Map<String, Integer> time_values = new HashMap<String, Integer>() {
        {
            put("S", 1);
            put("SEC", 1);
            put("SECOND", 1);
            put("SECONDS", 1);
            put("M", 60);
            put("MIN", 60);
            put("MINUTE", 60);
            put("MINUTES", 60);
            put("H", 3600);
            put("HOUR", 3600);
            put("HOURS", 3600);
            put("D", 86400);
            put("DAY", 86400);
            put("DAYS", 86400);
            put("W", 604800);
            put("WEEK", 604800);
            put("WEEKS", 604800);
            put("Y", 31536000);
            put("YEAR", 31536000);
            put("YEARS", 31536000);
        }
    };

    public static final Map<TimeUnit, Long> timeUnitValues = new HashMap<TimeUnit, Long>() {
        {
            put(TimeUnit.NANOSECONDS, 1L);
            put(TimeUnit.MICROSECONDS, 1000L);
            put(TimeUnit.MILLISECONDS, 1000000L);
            put(TimeUnit.SECONDS, 1000000000L);
            put(TimeUnit.MINUTES, 60000000000L);
            put(TimeUnit.HOURS, 3600000000000L);
            put(TimeUnit.DAYS, 86400000000000L);
        }
    };

    // public static enum TimeUnit {S,SEC,SECOND,SECONDS,
    //                               M,MIN,MINUTE,MINUTES,
    //                               H,HOUR,HOURS,D,DAY,DAYS,
    //                               W,WEEK,WEEKS,
    //                               Y,YEAR,YEARS}
    public static final Map<String, String> methodNameForServiceContextId = new HashMap<String, String>() {
        {
            // We are assuming these to be the names of the methods we are going to use to send each request
            put("topup@ptinovacao.pt", "topup");
            put("7.APC@telecom.pt", "call");
            put("8.32251@3gpp.org", "data");
            put("8.32274@3gpp.org", "sms");
        }
    };

    // public static final Map<String,String[]> requestForServiceContextId = new HashMap<String,String[]>(){
    // {
    //     put("8.32251@3gpp.org", new String[]{"onlineChargingData", "msisdn", "requestType", "session_id", "cc_request_number"});
    //     put("8.32274@3gpp.org", new String[]{"onlineChargingSMS", "msisdn", "requestType", "session_id", "cc_request_number"});
    // }};

    public static final Map<String, Map<String, Object>> requestForServiceContextId = new HashMap<String, Map<String, Object>>() {{
        put("8.32251@3gpp.org", new HashMap<String, Object>() {{
            put("method", "onlineChargingData");
            put("msisdn", String.class);
            put("requestType", String.class);
            put("session_id", String.class);
            put("cc_request_number", String.class);
        }});
        put("8.32274@3gpp.org", new HashMap<String, Object>() {{
            put("method", "onlineChargingSMS");
            put("msisdn", String.class);
            put("requestType", String.class);
            put("session_id", String.class);
            put("cc_request_number", String.class);
        }});
    }};


    public static final Map<String, String> O2CS_environment = new HashMap<String, String>() {
        {
            put("ACCOUNT", "ACC_9999840004");
            put("AGREEMENT", "ASE_AGREE_9999840004-2");
            put("MSISDN", "999840004");
            put("ACCOUNT_TO", "EST_006W");
            put("DESTINATION", "018098528639");
            put("NOTES", "");
            put("IP_SITE_A", "qnt-tst-sitea1.c.ptin.corppt.com");
            put("IP_SITE_B", "qnt-tst-siteb1.c.ptin.corppt.com");
            put("IP_SITE_CTG", "qnt-tst-ctg1.c.ptin.corppt.com");
            put("IP_SAT_1", "qnt-tst-sat1.c.ptin.corppt.com");
            put("IP_QTC", "qnt-tst-fe1.c.ptin.corppt.com:8080");
            put("IP_OMS", "qnt-tst-oms.c.ptin.corppt.com:8080");
            put("URL_TOPUP", "{{IP_TOPUP}}/ocs-topup/topup/");
            put("URL_CHARGING", "{{IP_CHARGING}}/ocs-charging/");
            put("URL_GCC", "{{IP_GCC}}/ocs-gcc/");
            put("URL_RFB", "{{IP_RFB}}/ocs-rfb/");
            put("URL_BM", "{{IP_BM}}/ocs-bm/");
            put("URL_DN", "{{IP_DN}}/ocs-spec/");
            put("URL_QTC", "{{IP_QTC}}/ocs/");
            put("URL_ASM", "{{IP_ASM}}/asm/");
            put("URL_ASM_BO", "{{IP_ASM_BO}}/ASM/");
            put("IP_TOPUP", "{{IP_SAT_1}}:9401");
            put("IP_CHARGING", "{{IP_SAT_1}}:9501");
            put("IP_GCC", "{{IP_SAT_1}}:9101");
            put("IP_RFB", "{{IP_SAT_1}}:9301");
            put("IP_DN", "{{IP_SITE_A}}:9201");
            put("IP_BM", "{{IP_SAT_1}}:9801");
            put("IP_UTILS", "{{IP_SITE_A}}:9092");
            put("CURRENT_DATE", "020-01-09T18:49:08.414Z");
            put("CURRENT_TIMESTAMP", "787584548");
            put("RQID", "OSTMAN_RQID_a4f09234-2cab-4b99-a075-bf49799bf296");
            put("CID", "OSTMAN_CID_0ab9a7d8-826b-48d9-8b3e-893835fc7f3c");
            put("SESSION_ID_DATA", "OSTMAN_DATA_4f095ad1-1bf4-4ff0-b963-c17c91e04166");
            put("SESSION_DATA_REQ_NUM", "");
            put("SESSION_ID_VOICE_MOC", "");
            put("SESSION_VOICE_MOC_REQ_NUM", "");
            put("SESSION_ID_ASM_ECUR", "");
            put("SESSION_ID_ASM_ECUR_REQ_NUM", "");
            put("CURRENT_DATE_MIDNIGHT", "");
            put("CURRENT_TIMESTAMP_MIDNIGHT", "");
            put("ADJUST_VALUE", "");
            put("AGREEMENT_CLASS", "");
            put("BALANCE_VALUE", "");
            put("BUCKET_ID", "");
            put("BUCKET_SPEC", "");
            put("CONTROL_BUCKET_LIMIT", "");
            put("CONTROL_BUCKET_SPEC", "");
            put("DATA_TO_FILE", "");
            put("END_DATE", "");
            put("END_DATE_XML", "");
            put("INITIAL_STEP", "");
            put("ITERATION", "");
            put("MONETARY_AMOUNT", "");
            put("OP_BUCKET_NAME", "");
            put("OP_MSISDN", "");
            put("OP_OFFER_NAME", "");
            put("OP_OFFER_SPEC_ID", "");
            put("OP_ZONE", "");
            put("OP_ZTE_BALANCE_ID", "");
            put("SLICE_ID", "");
            put("START_DATE", "");
            put("START_DATE_XML", "");
            put("debug_results_adjust", "");
            put("debug_results_get_acc", "");
            put("failed_requests_credit_bucket", "");
            put("failed_requests_credit_new_slice", "");
            put("failed_requests_debit_bucket", "");
            put("failed_requests_get_acc", "");
            put("failed_requests_get_agr", "");
            put("failed_requests_save_to_file", "");
            put("failed_requests_save_to_file_line", "");
            put("last_result_body_credit_bucket", "");
            put("last_result_body_credit_new_slice", "");
            put("last_result_body_debit_bucket", "");
            put("last_result_body_get_acc", "");
            put("last_result_body_get_agr", "");
            put("last_result_headers_credit_bucket", "");
            put("last_result_headers_credit_new_slice", "");
            put("last_result_headers_debit_bucket", "");
            put("last_result_headers_get_acc", "");
            put("last_result_headers_get_agr", "");
        }

    };

}
