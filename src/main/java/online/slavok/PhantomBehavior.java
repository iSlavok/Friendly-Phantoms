package online.slavok;

/**
 * Test seam for {@link online.slavok.mixin.PhantomMixin}.
 *
 * <p>When {@code friendly} is {@code false} the mixin stops suppressing phantom
 * targeting, which lets the game tests assert vanilla behaviour as a control
 * (i.e. prove the mixin is what changes the outcome). It is always {@code true}
 * in production - there is no config and the value is only ever flipped, in
 * memory, by the test harness.
 */
public final class PhantomBehavior {
    public static boolean friendly = true;

    private PhantomBehavior() {
    }
}
