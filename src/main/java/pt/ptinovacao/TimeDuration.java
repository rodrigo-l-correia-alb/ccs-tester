package pt.ptinovacao;


import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class TimeDuration {

    private final int ammount;

    private final TimeUnit unit;

    public TimeDuration(JSONObject jsonObject) {
        this.ammount = jsonObject.getInt("ammount");
        this.unit = TimeUnit.valueOf(jsonObject.getString("unit"));
    }

    public int getAmmount() {
        return ammount;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long getMiliseconds() {
        long unit_value = Definitions.timeUnitValues.get(unit);
        return ((long) this.ammount * unit_value / Definitions.timeUnitValues.get(TimeUnit.MILLISECONDS));
    }

    public long getSeconds() {
        long unit_value = Definitions.timeUnitValues.get(unit);
        return ((long) this.ammount * unit_value / Definitions.timeUnitValues.get(TimeUnit.SECONDS));
    }

    @Override
    public String toString() {
        return "TimeDuration{" +
                "ammount=" + ammount +
                ", unit=" + unit +
                '}';
    }

}
