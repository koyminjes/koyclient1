package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/** Polls mouse buttons once per rendered frame and keeps a rolling 1-second click window. */
public final class ClickTracker {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftDown, rightDown;

    private ClickTracker() {}

    public static void poll() {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        long now = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !leftDown) LEFT.addLast(now);
        if (r && !rightDown) RIGHT.addLast(now);
        leftDown = l;
        rightDown = r;
        trim(LEFT, now);
        trim(RIGHT, now);
    }

    private static void trim(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.removeFirst();
    }

    public static int leftCps() { return LEFT.size(); }
    public static int rightCps() { return RIGHT.size(); }
    public static boolean leftDown() { return leftDown; }
    public static boolean rightDown() { return rightDown; }
}
