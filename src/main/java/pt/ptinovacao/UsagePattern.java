package pt.ptinovacao;

import org.json.JSONObject;

public class UsagePattern {

    private final String name;

    private final TimeInterval requestInterval;

    private final TimeInterval sessionInterval;

    private final TimeInterval maxSessionDuration;

    private final TimeInterval topupInterval;

    private final TimeInterval consumptionRate;

    private final int ammount;

    private final boolean isReactive;

    private final MoneyInterval topAmount;

    public UsagePattern(JSONObject usagePattern) {
        this.topupInterval = usagePattern.has("topup_interval")
                ? new TimeInterval(usagePattern.getJSONObject("topup_interval"))
                : null;
        this.requestInterval = usagePattern.has("request_interval")
                ? new TimeInterval(usagePattern.getJSONObject("request_interval"))
                : null;
        this.name = usagePattern.has("name")
                ? usagePattern.getString("name")
                : null;
        this.maxSessionDuration = usagePattern.has("max_session_duration")
                ? new TimeInterval(usagePattern.getJSONObject("max_session_duration"))
                : null;
        this.consumptionRate = usagePattern.has("consumption_rate")
                ? new TimeInterval(usagePattern.getJSONObject("consumption_rate"))
                : null;
        this.sessionInterval = usagePattern.has("session_interval")
                ? new TimeInterval(usagePattern.getJSONObject("session_interval"))
                : null;
        this.ammount = usagePattern.has("ammount")
                ? usagePattern.getInt("ammount")
                : null;
        this.isReactive = usagePattern.has("is_reactive") && usagePattern.getBoolean("is_reactive");
        this.topAmount = usagePattern.has("request_ammount")
                ? new MoneyInterval(usagePattern.getJSONObject("request_ammount"))
                : null;
    }

    public String getName() {
        return name;
    }

    public TimeInterval getRequestInterval() {
        return requestInterval;
    }

    public TimeInterval getConsumptionRate() {
        return consumptionRate;
    }

    public TimeInterval getSessionInterval() {
        return sessionInterval;
    }

    public TimeInterval getMaxSessionDuration() {
        return maxSessionDuration;
    }

    public TimeInterval getTopupInterval() {
        return topupInterval;
    }

    public int getAmmount() {
        return ammount;
    }

    public MoneyInterval getTopAmount() {
        return topAmount;
    }

    public boolean hasMaxSessionDuration() {
        return this.maxSessionDuration != null;
    }

    public boolean hasRequest_interval() {
        return this.requestInterval != null;
    }


    public boolean isReactive() {
        return this.isReactive;
    }

    @Override
    public String toString() {
        return "UsagePattern{" +
                "name='" + name + '\'' +
                ", requestInterval=" + requestInterval +
                ", sessionInterval=" + sessionInterval +
                ", maxSessionDuration=" + maxSessionDuration +
                ", ammount=" + ammount +
                ", isReactive=" + isReactive +
                '}';
    }

}
