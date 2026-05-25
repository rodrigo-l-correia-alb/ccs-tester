package pt.ptinovacao;

import org.json.JSONObject;

import java.util.Random;

public class MoneyInterval {

    private final RandomElem random;

    private final Random r;

    public MoneyInterval(JSONObject jsonObject) {
        this.random = new RandomElem(jsonObject.getJSONObject("random"));
        // Porque motivo usa o seed 42???
        //this.r = new Random(42);
        this.r = new Random();
    }

    public RandomElem getRandom() {
        return random;
    }

    public Random getR() {
        return r;
    }

    public int getRandomAmount() {
        return (int) Math.abs(Math.round(r.nextGaussian() * this.random.getStd() + this.random.getMean()));
    }

    @Override
    public String toString() {
        return "MoneyInterval{" +
                "random=" + random +
                ", r=" + r +
                '}';
    }

}
