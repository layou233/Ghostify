package com.launium.ghostify.client.ui.speeddial;

import com.launium.ghostify.client.ui.clickgui.fubuki.nav.NavigationView;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.font.RenderInfo;
import com.launium.ghostify.client.ui.font.RenderedText;
import com.launium.ghostify.client.util.PlayerHead;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

public class ContactDestinationView implements NavigationView {
    private static final float MEASURED_HEIGHT = 14F;

    public final ItemStack contactIcon;
    public final String contactName;
    private final String contactShortName;

    private float startX, startY;
    private final Window window;
    private final int layerDepth;

    private static String processShortName(final String originName) {
        int indexOfLastWhitespace = originName.lastIndexOf(' ');
        if (indexOfLastWhitespace < 0) return originName;
        // Fix for multi-word names
        // https://wiki.hypixel.net/Abiphone#All_Contacts
        return switch (originName) {
            case "Fear Mongerer" -> "Fear";
            case "Jotraeline Greatforge" -> "Jotraeline";
            //case "Kuudra Gatekeeper" -> "Kuudra";
            case "Lumber Merchant" -> "Lumber";
            case "St. Jerry" -> "StJerry";
            default -> originName.substring(1 + indexOfLastWhitespace); // only show the last word
        };
    }

    public ContactDestinationView(ItemStack contactIcon, String contactName, Window window, int layerDepth) {
        this.contactIcon = contactIcon;
        this.contactName = contactName;
        this.contactShortName = processShortName(contactName);
        this.window = window;
        this.layerDepth = layerDepth;
    }

    public ContactDestinationView(String skinB64, String contactName, Window window, int layerDepth) {
        this(new ItemStack(Items.PLAYER_HEAD), contactName, window, layerDepth);
        if (skinB64 != null) {
            PlayerHead.setHeadSkin(contactIcon, skinB64);
        }
    }

    public void sendCallCommand() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.sendCommand("call " + contactShortName);
        }
    }

    @Override
    public float measureHeight() {
        return MEASURED_HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        //context.fill((int) startX, (int) startY, (int) startX + 30, (int) startY + (int) MEASURED_HEIGHT, 0xAFFFFFFF);

        // render icon
        Matrix3x2fStack pose = context.pose().pushMatrix();
        pose.translate(startX + 3F, startY + 0.5F);
        pose.scale(0.8F);
        context.renderFakeItem(contactIcon, 0, 0);
        pose.popMatrix();

        // render text
        float scale = window.getGuiScale();
        RenderedText renderedText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, contactShortName, 9F), scale);
        renderedText.draw(context, startX + 18F, startY + (MEASURED_HEIGHT - (float) Math.floor(renderedText.lineHeight / scale)) * 0.5F, layerDepth, scale, 0xFFFFFFFF);
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }

    @Override
    public String name() {
        return contactName;
    }

    @Override
    public boolean navigate() {
        sendCallCommand();
        return true;
    }
}
