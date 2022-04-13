package br.com.azimusb.inventorystatistics.helpers;

import com.mojang.datafixers.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class SpeedHelper {
    public static final int MAX_COUNT = 3;
    private long startTime = -1;
    private long startCount = -1;
    private long currentTime = -1;
    private long currentCount = -1;

    private SpeedUnit unit = SpeedUnit.perSecond;

    private final Queue<Pair<Long, Long>> buffer = new LinkedList<>();

    public void reset() {
        startCount = startTime = -1;
        buffer.clear();
    }

    public void add(long stackSize, long gameTime) {
        buffer.add(Pair.of(stackSize, gameTime));

        if (buffer.size() > MAX_COUNT) {
            Pair<Long, Long> p = buffer.remove();
            startCount = p.getFirst();
            startTime = p.getSecond();
        }
    }

    public void setStart(long stackSize, long gameTime) {
        startCount = stackSize;
        startTime = gameTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getItemSpeed() {
        double itemSpeed;

        if ((this.currentTime - this.startTime) == 0 || this.startTime == -1) {
            itemSpeed = 0.0;
        } else {
            itemSpeed = (double)(this.currentCount - this.startCount) / (this.currentTime - this.startTime);
        }
        return itemSpeed;
    }

    public void setStartToCurrent() {
        startCount = currentCount;
        startTime = currentTime;
    }

    public long passedTicks() {
        return currentTime - startTime;
    }

    public void setCurrent(long stackSize, long gameTime) {
        currentCount = stackSize;
        currentTime = gameTime;
    }

    public String getRenderedItemSpeed(double itemSpeed) {
        return ((itemSpeed > 0.0) ? "+" : "") + String.format("%.1f", unit.getTicks() * itemSpeed) + "/" + unit.getUnit();
    }

    public String getUnit() {
        return unit.getUnit();
    }

    public void setNextUnit() {
        unit = unit.getNext();
    }

    public String getUnitFullName() {
        return unit.getUnitFullName();
    }

    public long getStartCount() {
        return startCount;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public enum SpeedUnit {
        perTick(1, "t", "ticks"),
        perSecond(20, "s", "seconds"),
        perMinute(1200, "m", "minutes");

        private final int ticks;
        private final String unit;
        private final String unitFullName;

        private static final SpeedUnit[] vals = values();

        SpeedUnit(int ticks, String unit, String unitFullName) {
            this.ticks = ticks;
            this.unit = unit;
            this.unitFullName = unitFullName;
        }

        public int getTicks() {
            return ticks;
        }

        public String getUnit() {
            return unit;
        }

        public String getUnitFullName() {
            return unitFullName;
        }

        public SpeedUnit getNext() {
            return vals[(this.ordinal()+1) % vals.length];
        }
    }


}
