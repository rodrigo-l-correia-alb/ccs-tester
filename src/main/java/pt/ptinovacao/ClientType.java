package pt.ptinovacao;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClientType {

    private int id;

    private String account;

    private int msisdn;

    private int ammount;

    private String agreement_type;

    private final List<Service> services = new ArrayList<>();

    private Service topup;

    private MoneyInterval initialAmount;

    // public ClientType(int ammount, String agreement_type, List<Service> services, Service topup) {
    //     this.ammount = ammount;
    //     this.agreement_type = agreement_type;
    //     this.services = services;
    //     this.topup = topup;
    // }

    private void constructor_aux(JSONObject jsonObject) {
        this.ammount = jsonObject.getInt("ammount");

        this.agreement_type = jsonObject.has("agreement_type")
                ? jsonObject.getString("agreement_type")
                : null;

        this.topup = jsonObject.has("topup")
                ? new Service(jsonObject.getJSONObject("topup"))
                : null;
        this.initialAmount = jsonObject.has("initial_amount")
                ? new MoneyInterval(jsonObject.getJSONObject("initial_amount"))
                : null;

        jsonObject.getJSONArray("services")
                .forEach((service) -> services.add(new Service((JSONObject) service)));
    }

    public ClientType(JSONObject jsonObject) {
        constructor_aux(jsonObject);
    }

    public MoneyInterval getInitialAmount() {
        return initialAmount;
    }

    public ClientType(JSONObject jsonObject, int id) {
        constructor_aux(jsonObject);
        this.id = id;
    }

    public ClientType(JSONObject jsonObject, int id, String initialAccount, int initialMsisdn) {
        constructor_aux(jsonObject);
        this.id = id;
        this.account = String.valueOf(Integer.parseInt(initialAccount) + id);
        this.msisdn = initialMsisdn + id;
    }

    public ClientType(JSONObject jsonObject, String account, int msisdn) {
        constructor_aux(jsonObject);
        this.account = account;
        this.msisdn = msisdn;
    }

    public void linkPatterns(List<UsagePattern> usage_patterns) {
        services.forEach(service -> service.linkPattern(usage_patterns));
    }

    public int getAmmount() {
        return ammount;
    }

    public String getAgreement_type() {
        return agreement_type;
    }

    public List<Service> getServices() {
        return services;
    }

    public Service getTopup() {
        return topup;
    }

    public int getId() {
        return id;
    }

    public String getAccount() {
        return this.account;
    }

    public int getMsisdn() {
        return this.msisdn;
    }

    public boolean isReactive() {
        return this.topup.getUsagePattern().isReactive();
    }

    public int getTopupAmmount() {
        return this.getTopup().getUsagePattern().getAmmount();
    }

    @Override
    public String toString() {
        return "ClientType{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", msisdn=" + msisdn +
                ", ammount=" + ammount +
                ", agreement_type='" + agreement_type + '\'' +
                ", services=" + services +
                ", topup=" + topup +
                '}';
    }

}
