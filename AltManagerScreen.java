package im.expensive.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.utils.client.ClientUtil;
import im.expensive.utils.client.IMinecraft;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class AltManagerScreen extends Screen implements IMinecraft {

    private final Screen parent;

    public AltManagerScreen(Screen parent) {
        super(ITextComponent.getTextComponentOrEmpty(""));
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
    }

    private String nickInput = "";
    private boolean typing;

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        DisplayUtils.drawShadow(0, 0, width, height, 10, 0xAA000000);
        Fonts.montserrat.drawCenteredText(stack, "Alt Manager", ClientUtil.calc(width) / 2f, 20, -1, 8);

        // Delegate list and controls to existing widget for unified style
        Expensive.getInstance().getAltWidget().render(stack, mouseX, mouseY);

        float btnW = 100;
        float btnH = 18;
        float x = ClientUtil.calc(width) / 2f - btnW / 2f;
        float y = height - 40;

        // Input field
        float inputW = 200;
        float inputH = 18;
        float inputX = ClientUtil.calc(width) / 2f - inputW / 2f;
        float inputY = y - 26;
        int inputColor = typing ? 0xFF3A3A42 : 0xFF2E2E36;
        DisplayUtils.drawRoundedRect(inputX, inputY, inputW, inputH, 3, inputColor);
        String shown = nickInput.isEmpty() && !typing ? "Введите ник" : nickInput;
        int shownColor = nickInput.isEmpty() && !typing ? 0x77FFFFFF : 0xFFFFFFFF;
        Fonts.montserrat.drawText(stack, shown, inputX + 6, inputY + 5, shownColor, 6);

        // Buttons
        DisplayUtils.drawRoundedRect(x, y, btnW, btnH, 3, 0xFF2E2E36);
        Fonts.montserrat.drawCenteredText(stack, "Random", x + btnW / 2f, y + 5, -1, 6);

        float addW = 100;
        float addX = x + btnW + 6;
        DisplayUtils.drawRoundedRect(addX, y, addW, btnH, 3, 0xFF2E2E36);
        Fonts.montserrat.drawCenteredText(stack, "Add", addX + addW / 2f, y + 5, -1, 6);

        float backW = 100;
        float backX = x - backW - 6;
        DisplayUtils.drawRoundedRect(backX, y, backW, btnH, 3, 0xFF2E2E36);
        Fonts.montserrat.drawCenteredText(stack, "Back", backX + backW / 2f, y + 5, -1, 6);
    }

    private boolean isMouseDown;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isMouseDown = true;
        Expensive.getInstance().getAltWidget().click((int) mouseX, (int) mouseY, button);

        float btnW = 100;
        float btnH = 18;
        float x = ClientUtil.calc(width) / 2f - btnW / 2f;
        float y = height - 40;
        float backW = 100;
        float backX = x - backW - 6;
        float addW = 100;
        float addX = x + btnW + 6;
        float inputW = 200;
        float inputH = 18;
        float inputX = ClientUtil.calc(width) / 2f - inputW / 2f;
        float inputY = y - 26;

        // Click handling once per click
        if (isHovered((int) mouseX, (int) mouseY, x, y, btnW, btnH)) {
            String nick = generateNick();
            Expensive.getInstance().getAltWidget().alts.add(new Alt(nick));
            AltConfig.updateFile();
            return true;
        }
        if (isHovered((int) mouseX, (int) mouseY, addX, y, addW, btnH)) {
            if (nickInput != null && nickInput.length() >= 3) {
                if (nickInput.length() > 16) nickInput = nickInput.substring(0, 16);
                Expensive.getInstance().getAltWidget().alts.add(new Alt(nickInput));
                AltConfig.updateFile();
                nickInput = "";
                typing = false;
            }
            return true;
        }
        if (isHovered((int) mouseX, (int) mouseY, backX, y, backW, btnH)) {
            mc.displayGuiScreen(parent);
            return true;
        }
        // Focus input
        typing = isHovered((int) mouseX, (int) mouseY, inputX, inputY, inputW, inputH);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isMouseDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isHovered(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            if (Character.isLetterOrDigit(codePoint) || codePoint == '_' || codePoint == '-') {
                if (nickInput.length() < 16) nickInput += codePoint;
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            // Backspace
            if (keyCode == 259 && !nickInput.isEmpty()) {
                nickInput = nickInput.substring(0, nickInput.length() - 1);
                return true;
            }
            // Enter
            if (keyCode == 257) {
                if (nickInput.length() >= 3) {
                    if (nickInput.length() > 16) nickInput = nickInput.substring(0, 16);
                    Expensive.getInstance().getAltWidget().alts.add(new Alt(nickInput));
                    AltConfig.updateFile();
                    nickInput = "";
                    typing = false;
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String generateNick() {
        // length 7..16
        int len = ThreadLocalRandom.current().nextInt(7, 17);
        // pick a style
        String[] prefixes = new String[]{"Vasya", "pro", "Ultra", "Best", "xX", "Noob", "God", "q", "r"};
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        String base = RandomStringUtils.randomAlphanumeric(Math.max(3, len - prefix.length()));
        String nick = (prefix + base);
        if (nick.length() > 16) nick = nick.substring(0, 16);
        return nick;
    }
}


