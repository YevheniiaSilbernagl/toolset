import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by evgeniyat on 16.05.16
 */
public class ExtendedDemoTests {
    Wait shortTime = new Wait(1L, 200L);
    Wait longTime = new Wait(10L, 2L);
    Random rand = new Random();

    @Test(expected = TimeoutException.class)
    public void list() {
        List<String> list = Collections.singletonList("object");
        shortTime.until(() -> list.size() > 1);
    }

    @Test
    public void listModification() {
        List<String> list = new ArrayList<String>() {{
            add("object");
        }};
        executeInParallel(
                () -> {
                    waitBeforeSuccess(5L);
                    list.add("newObject");
                },
                () -> longTime.until(() -> list.size() > 0)
        );
    }

    @Test
    public void listOfEven() {
        List<Integer> list = new ArrayList<Integer>() {{
            addAll(Arrays.asList(1, 3, 5, 7, 9, 11));
        }};

        executeInParallel(
                () -> {
                    waitBeforeSuccess(2L);
                    list.add(4);
                },
                () -> longTime.until(() -> containsEven.test(list))
        );
    }


    @Test
    public void listOfLargeEven() {
        List<Integer> list = new ArrayList<Integer>() {{
            addAll(Arrays.asList(1, 3, 5, 7, 9, 11));
        }};
        executeInParallel(
                () -> {
                    waitBeforeSuccess(1L);
                    list.add(4);
                    waitBeforeSuccess(2L);
                    list.add(40);
                },
                () -> longTime.until(() -> containsEven.and(containsLarge).test(list))
        );
        Assert.assertEquals(Collections.singletonList(40),
                list.stream().filter(large).filter(even).collect(Collectors.toList()));
    }

    @Test
    public void largePower() {
        new Wait(5L, 200L).until(new Supplier<Double>() {
            Double lastNumber = null;

            @Override
            public String toString() {
                return "number more than 20.0, last one is " + lastNumber;
            }

            @Override
            public Double get() {
                lastNumber = power.apply(rand.nextInt(11), 2);
                return lastNumber > 20.0 ? lastNumber : null;
            }
        });
    }

    @Test(expected = TimeoutException.class)
    public void largePowerNegative() {
        new Wait(5L, 200L).until(new Supplier<Double>() {
            Double lastNumber = null;

            @Override
            public String toString() {
                return "number more than 100, last one is " + lastNumber;
            }

            @Override
            public Double get() {
                lastNumber = power.apply(rand.nextInt(4), 2);//is never more than 20
                return lastNumber > 20.0 ? lastNumber : null;
            }
        });
    }

    @Test(expected = TimeoutException.class)
    public void stringsAreEqual() {
        new Wait(5L, 200L).until(equals("test1", "test2"));
    }


    public static Supplier<Boolean> equals(final String str1, final String str2) {
        return new Supplier<Boolean>() {

            @Override
            public String toString() {
                return String.format("string %s to be equal to %s", str1, str2);
            }


            @Override
            public Boolean get() {
                return str1 == null ? str2 == null : str1.equals(str2);
            }
        };
    }

    private static BiFunction<Integer, Integer, Double> power = Math::pow;

    private static Predicate<Integer> even = integer -> integer % 2 == 0;
    private static Predicate<Integer> large = (num) -> num > 10;

    private static Predicate<List<Integer>> containsEven = new Predicate<List<Integer>>() {
        @Override
        public boolean test(List<Integer> integers) {
            return integers.stream().filter(even).count() > 0;
        }

        @Override
        public String toString() {
            return "list to contain even number";
        }
    };
    private static Predicate<List<Integer>> containsLarge = new Predicate<List<Integer>>() {
        @Override
        public boolean test(List<Integer> integers) {
            return integers.stream().filter(large).count() > 0;
        }

        @Override
        public String toString() {
            return "list to contain even number";
        }
    };

    private void executeInParallel(Runnable modification, Runnable waitCondition) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(modification);
        executor.execute(waitCondition);
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(1);// Wait until all threads are finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitBeforeSuccess(Long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
