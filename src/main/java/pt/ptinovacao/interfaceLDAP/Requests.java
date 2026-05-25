package pt.ptinovacao.interfaceLDAP;


public interface Requests {

    boolean createAccount(String account);

    boolean deleteAccount(String account);
    //public boolean createAccount(String account, String msisdn);
    //public boolean deleteAccount(String account, String msisdn);

    double getSaldo(String account);

    boolean creditBucket(String account, String amount);

}