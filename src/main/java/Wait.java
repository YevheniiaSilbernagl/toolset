import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * Created by TJ on 21.03.2016 for automation of budget energie tests
 */
public class Wait {
    private Duration timeout;
    private Duration interval;

    public Wait() {
        this.timeout = Duration.of(150, ChronoUnit.SECONDS);
        this.interval = Duration.of(500, ChronoUnit.MILLIS);
    }

    public Wait(Long timeoutInSeconds) {
        this.timeout = Duration.of(timeoutInSeconds, ChronoUnit.SECONDS);
        this.interval = Duration.of(500, ChronoUnit.MILLIS);
    }

    public Wait(Long timeoutInSeconds, Long intervalInMillis) {
        this.timeout = Duration.of(timeoutInSeconds, ChronoUnit.SECONDS);
        this.interval = Duration.of(intervalInMillis, ChronoUnit.MILLIS);
    }

    public <V> V until(Supplier<V> isTrue) {
        LocalTime end = LocalTime.now().plus(this.timeout);
        Throwable lastException = null;

        while (true) {
            try {
                V e = isTrue.get();
                if (e != null) {
                    if (Boolean.class.equals(e.getClass())) {
                        if (Boolean.TRUE.equals(e)) {
                            return e;
                        }
                    } else {
                        return e;
                    }
                }
            } catch (Throwable var9) {
                lastException = var9;
            }
            if (!LocalTime.now().isBefore(end)) {
                String timeoutMessage = String.format("Timed out after %d seconds waiting for %s", this.timeout.getSeconds(), isTrue.toString());
                throw new TimeoutException(timeoutMessage + (lastException == null ? "" : ("\n" + lastException.getMessage())));
            }
            try {
                Thread.sleep(this.interval.toMillis());
            } catch (InterruptedException var8) {
                Thread.currentThread().interrupt();
                throw new AssertionError(var8);
            }
        }
    }


    public <V> V possible(Supplier<V> isTrue) {
        LocalTime end = LocalTime.now().plus(this.timeout);
        while (true) {
            try {
                V e = isTrue.get();
                if (e != null) {
                    if (Boolean.class.equals(e.getClass())) {
                        if (Boolean.TRUE.equals(e)) {
                            return e;
                        }
                    } else {
                        return e;
                    }
                }
                if (!LocalTime.now().isBefore(end)) {
                    return e;
                }
            } catch (Throwable var8) {/**/}
            try {
                Thread.sleep(this.interval.toMillis());
            } catch (InterruptedException var8) {
                Thread.currentThread().interrupt();
                throw new AssertionError(var8);
            }
        }
    }

    public void setTimeout(Long timout) {
        this.timeout = Duration.of(timout, ChronoUnit.SECONDS);
    }
}

