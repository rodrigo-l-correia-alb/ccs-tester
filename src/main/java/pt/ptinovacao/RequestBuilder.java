package pt.ptinovacao;

// Java program to demonstrate
// Logger.log(Level level, String msg)  method

import java.util.logging.Level;
import java.util.logging.Logger;


public class RequestBuilder {


    public static void credit_bucket(String string, int ammount) {
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "credit_bucket");
        // System.out.println("credit_bucket");

    }

    public static void create_agreement(String string, int i, String agreement_type) {
        // System.out.println("create_agreement");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "create_agreement");

    }

    public static void create_account(String string) {
        // System.out.println("create_account");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "create_account");

    }

    public static void topup(int account, int msisdn, String agreement) {
        // System.out.println("Sent topup.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "topup");


    }

    public static void call_initial(int account, int msisdn, String agreement) {
        // System.out.println("Sent call_initial.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "call_initial");

    }

    public static void call_update(int account, int msisdn, String agreement) {
        // System.out.println("Sent call_update.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "call_update");

    }

    public static void call_termination(int account, int msisdn, String agreement) {
        // System.out.println("Sent call_terminate.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "call_termination");

    }

    public static void data_initial(int account, int msisdn, String agreement) {
        // System.out.println("Sent data_initial.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "data_initial");

    }

    public static void data_update(int account, int msisdn, String agreement) {
        // System.out.println("Sent data_update.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "data_update");

    }

    public static void data_termination(int account, int msisdn, String agreement) {
        // System.out.println("Sent data_terminate.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "data_termination");

    }

    public static void sms(int account, int msisdn, String agreement) {
        // System.out.println("Sent sms.");
        Logger logger = Logger.getLogger("RequestBuilder");
        logger.log(Level.INFO, "sms");

    }

}
