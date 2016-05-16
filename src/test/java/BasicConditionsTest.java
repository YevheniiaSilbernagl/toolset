import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Created by evgeniyat on 16.05.16
 */
public class BasicConditionsTest {
    Wait shortTime = new Wait(1L, 200L);
    Wait longTime = new Wait(10L);
    Wait defaultTime = new Wait();

    @Test
    public void trueBooleanCondition() {
        Assert.assertTrue(longTime.until(() -> true));
    }

    @Test(expected = TimeoutException.class)
    public void falseBooleanCondition() {
        shortTime.until(() -> false);
    }

    @Test
    public void emptyStringCondition() {
        Assert.assertTrue(defaultTime.until(() -> "").isEmpty());
    }

    @Test
    public void nonEmptyStringCondition() {
        Assert.assertEquals("test", longTime.until(() -> "test"));
    }

    @Test(expected = TimeoutException.class)
    public void nullStringCondition() {
        Supplier<String> stringSupplier = () -> null;
        shortTime.until(stringSupplier);
    }


    @Test
    public void trueBooleanPossibleCondition() {
        Assert.assertTrue(longTime.possible(() -> true));
    }

    @Test
    public void falseBooleanPossibleCondition() {
        Assert.assertFalse(shortTime.possible(() -> false));
    }

    @Test
    public void emptyStringPossibleCondition() {
        Assert.assertTrue(defaultTime.possible(() -> "").isEmpty());
    }

    @Test
    public void nonEmptyStringPossibleCondition() {
        Assert.assertEquals("test", longTime.possible(() -> "test"));
    }

    @Test
    public void nullStringPossibleCondition() {
        Assert.assertNull(shortTime.possible(() -> null));
    }
}
