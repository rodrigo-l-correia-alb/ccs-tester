package pt.ptinovacao;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClientGroup {

    private final JSONObject config;

    private final String name;

    private final Integer number_of_clients;

    private ClientType client_type;

    private String client_type_name;

    private final List<ClientType> clients;

    private final String initialAccount;

    private final int initialMsisdn;

    private final MoneyInterval initialAmount;


    //If only the config is passed, the ClientGroup might be initialized with the client_type_name instead of the actual client type
    public ClientGroup(JSONObject jsonObject) {
        this.config = jsonObject;
        this.name = jsonObject.getString("name");
        this.number_of_clients = jsonObject.getInt("number_of_clients");
        this.initialAccount = jsonObject.getJSONObject("initial").getString("account");
        this.initialMsisdn = jsonObject.getJSONObject("initial").getInt("MSISDN");
        this.initialAmount = jsonObject.getJSONObject("client_type").has("initial_amount")
                ? new MoneyInterval(jsonObject.getJSONObject("client_type").getJSONObject("initial_amount"))
                : null;
        try {
            this.client_type = new ClientType(jsonObject.getJSONObject("client_type"));
        } catch (JSONException e) {
            e.printStackTrace();
            this.client_type_name = jsonObject.getString("client_type");
        }
        clients = new ArrayList<ClientType>();
    }

    public void create_clients() {
        for (int i = 0; i < this.number_of_clients; i++) {
            this.clients.add(new ClientType(this.config.getJSONObject("client_type"),
                    String.valueOf(Integer.parseInt(this.initialAccount) + i),
                    this.initialMsisdn + i));
        }
        // System.out.println(this.config.getJSONObject("client_type"));
    }


    public String getName() {
        return name;
    }

    public Integer getNumber_of_clients() {
        return number_of_clients;
    }

    public ClientType getClient_type() {
        return client_type;
    }

    public String getClient_type_name() {
        return client_type_name;
    }

    public List<ClientType> getClients() {
        return clients;
    }

    public boolean isReactive() {
        return this.client_type.isReactive();
    }


}
