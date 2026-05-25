package pt.ptinovacao;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TimeInterval {

    private final RandomElem random;

    private final TimeUnit unit;

    private final Random r;

    ChiSquaredDistribution dist;

    public TimeInterval(JSONObject jsonObject) {
        this.random = new RandomElem(jsonObject.getJSONObject("random"));
        this.unit = TimeUnit.valueOf(jsonObject.getString("unit"));
        //this.r = new Random(42);
        this.r = new Random();
        if (this.random.getDf() > 0) {
            RandomGenerator rng = new Well19937c();
            this.dist = new ChiSquaredDistribution(rng, this.random.getDf());
        }

    }

    public RandomElem getRandom() {
        return random;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public Random getR() {
        return r;
    }

    public long getMiliseconds() {
        long unit_value = Definitions.timeUnitValues.get(this.unit);
        return (long) Math.abs(((r.nextGaussian() * this.random.getStd() + this.random.getMean()) * unit_value / Definitions.timeUnitValues.get(TimeUnit.MILLISECONDS)));

    }

    public long getSeconds() {
        long unit_value = Definitions.timeUnitValues.get(this.unit);
        long value = (long) ((r.nextGaussian() * this.random.getStd() + this.random.getMean()) * unit_value / Definitions.timeUnitValues.get(TimeUnit.SECONDS));
        return value > 0 ? value : 0;
    }

    public long getSeconds_Chi() {
        long secs = 0;

        while (secs <= 0) {
            secs = Math.round(this.dist.sample() * this.random.getScale() + this.random.getLoc());
        }
        return secs;
    }

    public double getSaldo() {
        double value = r.nextGaussian() * this.random.getStd() + this.random.getMean();
        return value >= 0 ? value : 0;
    }


    @Override
    public String toString() {
        return "TimeInterval{" +
                "random=" + random +
                ", unit=" + unit +
                ", r=" + r +
                '}';
    }

}
