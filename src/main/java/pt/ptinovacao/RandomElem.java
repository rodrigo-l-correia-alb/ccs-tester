package pt.ptinovacao;

import org.json.JSONObject;

public class RandomElem {

    private final int mean;

    private final int std;

    private final double df;

    private final double loc;

    private final double scale;


    public RandomElem(JSONObject jsonObject) {
        this.mean = jsonObject.has("mean") ? jsonObject.getInt("mean") : 0;
        this.std = jsonObject.has("std") ? jsonObject.getInt("std") : 0;
        this.df = jsonObject.has("df") ? jsonObject.getDouble("df") : 0;
        this.loc = jsonObject.has("loc") ? jsonObject.getDouble("loc") : 0;
        this.scale = jsonObject.has("scale") ? jsonObject.getDouble("scale") : 0;
    }

    public int getMean() {
        return mean;
    }

    public int getStd() {
        return std;
    }

    public double getDf() {
        return df;
    }

    public double getLoc() {
        return loc;
    }

    public double getScale() {
        return scale;
    }

    @Override
    public String toString() {
        return "RandomElem{" +
                "mean=" + mean +
                ", std=" + std +
                '}';
    }

}
