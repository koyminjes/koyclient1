package net.koyclient.module;

public class CpsModule extends TextModule {
    public CpsModule() {
        super("cps", "CPS", "Shows left | right clicks per second.", true, 4, 20);
    }

    @Override
    protected String text() {
        return "CPS: " + ClickTracker.leftCps() + " | " + ClickTracker.rightCps();
    }
}
