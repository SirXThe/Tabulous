package com.nxtdelivery.tabulous.mixin;


import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import com.nxtdelivery.tabulous.Tabulous;
import com.nxtdelivery.tabulous.config.TabulousConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.List;
import java.util.regex.Pattern;

@Mixin(value = GuiPlayerTabOverlay.class, priority = 990)
public abstract class GuiPlayerTabOverlayMixin {
    private float percentComplete = 0f;
    private boolean retract = false;
    private String playerName = "i am not null!";
    private int rowRight = 0;
    private boolean shouldRenderHeads = true;
    private int width = 0;
    private List<String> headerList = null;
    private boolean inGame = false;
    private static final Pattern validMinecraftUsername = Pattern.compile("\\w{1,16}");
    private static final Pattern skyblockNameRegex = Pattern.compile("![A-D]-[a-v]");
    private NetworkPlayerInfo currentInfo;
    private int winTop = TabulousConfig.topPosition;

    @Final
    @Shadow
    private Minecraft mc;

    @Shadow
    private IChatComponent footer;
    @Shadow
    private IChatComponent header;

    @Shadow
    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        throw new MixinException("something went wrong");
    }

    @Shadow
    private void drawScoreboardValues(ScoreObjective p_175247_1_, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo p_175247_6_) {
        throw new MixinException("something went wrong");
    }

    @Shadow
    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
        throw new MixinException("something went wrong");
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE"))
    public List<NetworkPlayerInfo> removeTabEntries(List<NetworkPlayerInfo> list) {
        if (TabulousConfig.alwaysAtTop && !Tabulous.isSkyblock) {
            for (NetworkPlayerInfo info : list) {
                if (info.getGameProfile().getName().equals(mc.getSession().getUsername())) {
                    list.remove(info);
                    list.add(0, info);
                    break;
                }
            }
        }
        if (TabulousConfig.hideNPCs && HypixelUtils.INSTANCE.isHypixel()) {
            try {
                list.removeIf(info -> getPlayerName(info).startsWith("\u00A78[NPC]") || getPlayerName(info).startsWith("\u00a7e[NPC]") || getPlayerName(info).startsWith("\u00a75[NPC]"));
                if (Tabulous.hideWhiteNames && !inGame) Tabulous.hideWhiteNames = false;
                if (!Tabulous.hideWhiteNames)
                    list.removeIf(info -> !getPlayerName(info).startsWith("\u00a7"));
            } catch (Exception ignored) {
            }
        }
        if (TabulousConfig.hideInvalidNames || (TabulousConfig.defaultSkyBlockTab && Tabulous.isSkyblock)) {
            try {
                list.removeIf(info -> !validMinecraftUsername.matcher(info.getGameProfile().getName()).matches());
            } catch (Exception ignored) {
            }
        }
        if (TabulousConfig.defaultSkyBlockTab) {
            try {
                list.removeIf(info -> skyblockNameRegex.matcher(getPlayerName(info)).matches());
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    @ModifyArg(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I", ordinal = 0))
    public String getPlayerName(String args) {
        playerName = args;
        return args;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private int changeWidth(int k) {
        int strWidth;
        if (playerName.contains(mc.getSession().getUsername())) {
            if (!TabulousConfig.myNameText.isEmpty()) {
                strWidth = mc.fontRendererObj.getStringWidth(TabulousConfig.myNameText);
            } else {
                strWidth = k;
            }
        } else {
            strWidth = k;
        }
        return strWidth;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    public int setTop(int top) {
        ScaledResolution resolution = new ScaledResolution(mc);
        switch (TabulousConfig.position) {
            default:
                winTop = TabulousConfig.topPosition;
                break;
            case 3:
                winTop = resolution.getScaledHeight() / 3;
                break;
            case 4:
                winTop = (int) (resolution.getScaledHeight() / 1.5);
                break;
        }
        return winTop;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At("STORE"))
    public boolean setFlag(boolean flag) {
        if (!TabulousConfig.showHeads) flag = false;
        shouldRenderHeads = flag;
        return flag;
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0))
    public void cancelHeaderRect(int i, int i1, int i2, int i3, int i4) {

    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 3))
    public void cancelFooterRect(int i, int i1, int i2, int i3, int i4) {

    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 0))
    public int cancelOldHeader(FontRenderer instance, String text, float x, float y, int color) {
        return 0;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1))
    public void redrawRect(Args args) {
        args.set(4, TabulousConfig.tabColor.getRGB());
        int top = args.get(1);
        int bottom = args.get(3);
        if (header != null) {
            headerList = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);
            top -= (headerList.size() * 10);
        }
        if (footer != null) {
            List<String> list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);
            bottom += (list2.size() * 10);
        }
        args.set(0, (int) args.get(0) - TabulousConfig.marginFix);
        args.set(2, (int) args.get(2) + TabulousConfig.marginFix);
        bottom = bottom - top;
        args.set(1, top);
        if (TabulousConfig.animations) {
            args.set(3, top + (int) (percentComplete * bottom));
        } else args.set(3, top + bottom);
    }


    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    public void cancelUntilReadyAndReRenderHeader(CallbackInfo ci) {
        if (TabulousConfig.animations) {
            if (percentComplete < 0.9f) {
                if (Tabulous.isTabHeightAllow() && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && GuiIngameForge.renderBossHealth) {
                    GlStateManager.popMatrix();
                }
                ci.cancel();
            } else {
                if (headerList != null) {
                    int top2 = winTop;
                    for (String s : headerList) {
                        int strWidth = this.mc.fontRendererObj.getStringWidth(s);
                        drawString(mc.fontRendererObj, s, (float) (this.width / 2 - strWidth / 2), (float) top2, -1);
                        top2 += 10;
                    }
                }
            }
        } else {
            if (headerList != null) {
                int top2 = winTop;
                for (String s : headerList) {
                    int strWidth = this.mc.fontRendererObj.getStringWidth(s);
                    drawString(mc.fontRendererObj, s, (float) (this.width / 2 - strWidth / 2), (float) top2, -1);
                    top2 += 10;
                }
            }
        }
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawScoreboardValues(Lnet/minecraft/scoreboard/ScoreObjective;ILjava/lang/String;IILnet/minecraft/client/network/NetworkPlayerInfo;)V", ordinal = 0))
    public void redirectScoreBoardRender(GuiPlayerTabOverlay instance, ScoreObjective j1, int f1, String i1, int s, int f, NetworkPlayerInfo j) {
        if (TabulousConfig.renderScoreboardValues) {
            drawScoreboardValues(j1, f1, i1, s, f, j);
        }
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
    public void renderNames(Args args) {
        if (args.get(0).toString().contains(mc.getSession().getUsername())) {
            if (!TabulousConfig.myNameText.isEmpty()) {
                if (inGame && Tabulous.isBedWars && TabulousConfig.customNameBW) {
                    String name = TabulousConfig.myNameText;
                    if (TabulousConfig.myNameText.startsWith("\u00a7")) {
                        name = TabulousConfig.myNameText.substring(2);
                    }
                    if (name.startsWith("[")) {
                        name = name.substring(name.indexOf("]") + 2);
                    }
                    if (!Character.isLetterOrDigit(args.get(0).toString().charAt(2))) {
                        name = args.get(0).toString().substring(0, 10) + name;
                        args.set(0, name);
                    } else args.set(0, TabulousConfig.myNameText);
                } else if (!TabulousConfig.hideCustomNameIngame) args.set(0, TabulousConfig.myNameText);
                else if (!inGame) args.set(0, TabulousConfig.myNameText);

            }
        }
        if (TabulousConfig.hideGuilds && HypixelUtils.INSTANCE.isHypixel()) {
            try {
                if (args.get(0).toString().charAt(args.get(0).toString().length() - 1) == ']') {
                    args.set(0, args.get(0).toString().substring(0, args.get(0).toString().lastIndexOf("\u00A7")));
                }
            } catch (Exception ignored) {
            }
        }
        if (TabulousConfig.hidePlayerRanksInTab && args.get(0).toString().startsWith("[", 2) && HypixelUtils.INSTANCE.isHypixel()) {
            String color = "\u00a7" + args.get(0).toString().charAt(1);
            args.set(0, color + args.get(0).toString().substring(args.get(0).toString().indexOf("]") + 2));
        }
        if (TabulousConfig.headPos) {
            args.set(1, (float) args.get(1) - 8f);
        }
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    public int drawString(FontRenderer instance, String text, float x, float y, int color) {
        if (TabulousConfig.textShadow) {
            instance.drawStringWithShadow(text, x, y, color);
        } else {
            instance.drawString(text, (int) x, (int) y, color);
        }
        return 0;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE"), ordinal = 0)
    public NetworkPlayerInfo getPlayerInfo(NetworkPlayerInfo info) {
        currentInfo = info;
        return info;
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 0))
    public void setPositionHead(Args args) {
        if (TabulousConfig.headPos) {
            args.set(0, rowRight - 8);
        }
        if (TabulousConfig.cleanerSkyBlockTabInfo && HypixelUtils.INSTANCE.isHypixel()) {
            try {
                if (Tabulous.isSkyblock) {
                    if (skyblockNameRegex.matcher(currentInfo.getGameProfile().getName()).matches() &&
                            !validMinecraftUsername.matcher(currentInfo.getDisplayName().getUnformattedText()).matches()) {
                        args.set(0, -1000);         // shut up it works okay
                        args.set(1, -1000);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1))
    public void setPositionHat(Args args) {
        if (TabulousConfig.headPos) {
            args.set(0, rowRight - 8);
        }
        if (TabulousConfig.cleanerSkyBlockTabInfo && HypixelUtils.INSTANCE.isHypixel()) {
            try {
                if (Tabulous.isSkyblock) {
                    if (skyblockNameRegex.matcher(currentInfo.getGameProfile().getName()).matches() &&
                            !validMinecraftUsername.matcher(currentInfo.getDisplayName().getUnformattedText()).matches()) {
                        args.set(0, -1000);
                        args.set(1, -1000);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @ModifyArgs(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V", ordinal = 2))
    public void setColor(Args args) {
        rowRight = args.get(2);
        //int left = ((int) args.get(0) / 5) * TabulousConfig.marginInternal;       TODO something like this ig
        //int i1 = (int) args.get(2) - (int) args.get(0);
        //int right = left + i1;
        //args.set(0,left);
        //args.set(2,right);
        args.set(4, TabulousConfig.tabItemColor.getRGB());
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawPing(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V"))
    public void renderPing(GuiPlayerTabOverlay instance, int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
        if (TabulousConfig.renderPing && !TabulousConfig.headPos) {
            if (shouldRenderHeads) p_175245_1_ += 9;
            inGame = networkPlayerInfoIn.getResponseTime() == 1;
            if ((TabulousConfig.hidePingInGame || TabulousConfig.cleanerSkyBlockTabInfo) && networkPlayerInfoIn.getResponseTime() != 1 && HypixelUtils.INSTANCE.isHypixel())
                drawPing(p_175245_1_, p_175245_2_ - (shouldRenderHeads ? 9 : 0), p_175245_3_, networkPlayerInfoIn);
        }
    }

    @Inject(method = "renderPlayerlist", at = @At("HEAD"), cancellable = true)
    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (TabulousConfig.disabled) {
            if (Tabulous.isTabHeightAllow() && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && GuiIngameForge.renderBossHealth) {
                GlStateManager.popMatrix();
            }
            ci.cancel();
        }
        percentComplete = clamp(easeOut(percentComplete, retract ? 0f : 1f));
        if (retract && Keyboard.isKeyDown(mc.gameSettings.keyBindPlayerList.getKeyCode())) {   // im so smart
            retract = false;
        }
        if (retract && percentComplete == 0f) {
            retract = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindPlayerList.getKeyCode(), false);
        }
        if (mc.currentScreen != null && TabulousConfig.closeInGUIs) retract = true;
        this.width = width;
        modifyFooter();
        modifyHeader();
    }

    private void modifyFooter() {
        if (!TabulousConfig.footerText.isEmpty()) {
            footer = new ChatComponentText(TabulousConfig.footerText);
        }
        if (!TabulousConfig.showFooter) {
            footer = null;
        }
    }

    private void modifyHeader() {
        if (!TabulousConfig.headerText.isEmpty()) {
            header = new ChatComponentText(TabulousConfig.headerText);
        }
        if (!TabulousConfig.showHeader) {
            header = null;
            headerList = null;
        }
    }

    @Inject(method = "updatePlayerList", at = @At("HEAD"))
    public void updatePlayerList(CallbackInfo ci) {
        if (TabulousConfig.animations) {
            if (percentComplete != 0f && !mc.gameSettings.keyBindPlayerList.isKeyDown()) {
                retract = true;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindPlayerList.getKeyCode(), true);
            }
        }
    }

    private static float clamp(float number) {
        return number < (float) 0.0 ? (float) 0.0 : Math.min(number, (float) 1.0);
    }

    private static float easeOut(float current, float goal) {
        if (Math.floor(Math.abs(goal - current) / (float) 0.01) > 0) {
            return current + (goal - current) / TabulousConfig.animSpeed;
        } else {
            return goal;
        }
    }
}
