package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class ArmorModule extends KoyModule {
    public ArmorModule() {
        super("armor", "Armor Status", "Shows armor pieces with durability.", true, 4, 164);
    }

    @Override
    public int getWidth() {
        return 40;
    }

    @Override
    public int getHeight() {
        return 4 * 18;
    }

    @Override
    protected void render(DrawContext ctx, TextRenderer font) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        for (int i = 0; i < 4; i++) {
            ItemStack s = p.getInventory().getArmorStack(3 - i); // helmet first
            if (s.isEmpty()) continue;
            int y = i * 18;
            ctx.drawItem(s, 0, y);
            if (s.isDamageable()) {
                int left = s.getMaxDamage() - s.getDamage();
                ctx.drawText(font, String.valueOf(left), 20, y + 4, s.getItemBarColor() | 0xFF000000, true);
            }
        }
    }
}
