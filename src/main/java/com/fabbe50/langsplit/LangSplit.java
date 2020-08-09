package com.fabbe50.langsplit;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.LanguageScreen;
import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Mod("langsplit")
public class LangSplit {
    public static final Config config = new Config();

    private static final Logger LOGGER = LogManager.getLogger();

    public LangSplit() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config.getSpec(), "LangSplit.toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tooltipEvent(ItemTooltipEvent event) {
        Language primary = Minecraft.getInstance().getLanguageManager().getCurrentLanguage();
        Language secondary = Minecraft.getInstance().getLanguageManager().getLanguage(config.getGeneral().getLanguage());

        if (secondary != null && !event.getItemStack().hasDisplayName()) {
            if (primary != secondary) {
                List<Language> list = Lists.newArrayList(secondary);
                ClientLanguageMap clientlanguagemap = ClientLanguageMap.func_239497_a_(Minecraft.getInstance().getResourceManager(), list);
                List<ITextComponent> newTooltip = Lists.newArrayList();
                int posMul = 0;
                for (ITextComponent component : event.getToolTip()) {
                    if (component instanceof TranslationTextComponent) {
                        Object[] args = new Object[((TranslationTextComponent) component).getFormatArgs().length];
                        for (int i = 0; i < args.length; i++) {
                            if (((TranslationTextComponent) component).getFormatArgs()[i] instanceof TranslationTextComponent)
                                args[i] = clientlanguagemap.func_230503_a_(((TranslationTextComponent) ((TranslationTextComponent) component).getFormatArgs()[i]).getKey());
                            else if (((TranslationTextComponent) component).getFormatArgs()[i] instanceof TextComponent)
                                args[i] = ((TextComponent) ((TranslationTextComponent) component).getFormatArgs()[i]).getString();
                            else
                                args[i] = ((TranslationTextComponent) component).getFormatArgs()[i];
                        }
                        StringBuilder extras = new StringBuilder();
                        if (!component.getSiblings().isEmpty()) {
                            for (ITextComponent c : component.getSiblings()) {
                                if (c instanceof TranslationTextComponent) {
                                    extras.append(clientlanguagemap.func_230503_a_(((TranslationTextComponent) c).getKey()));
                                } else {
                                    extras.append(c.getString());
                                }
                            }
                        }
                        if (config.getGeneral().getInLine()) {
                            newTooltip.add(new StringTextComponent(component.getString() + " [" + String.format(clientlanguagemap.func_230503_a_(((TranslationTextComponent) component).getKey()), args) + extras + "]").func_230530_a_(component.getStyle()));
                        } else {
                            newTooltip.add(component);
                            newTooltip.add(new StringTextComponent("[" + String.format(clientlanguagemap.func_230503_a_(((TranslationTextComponent) component).getKey()), args) + extras + "]").func_230530_a_(component.getStyle()));
                        }
                    } else if (component instanceof TextComponent) {
                        if (!component.getSiblings().isEmpty()) {
                            for (ITextComponent component1 : component.getSiblings()) {
                                if (component1 instanceof TranslationTextComponent) {
                                    Object[] args = new Object[((TranslationTextComponent) component1).getFormatArgs().length];
                                    for (int i = 0; i < args.length; i++) {
                                        if (((TranslationTextComponent) component1).getFormatArgs()[i] instanceof TranslationTextComponent)
                                            args[i] = clientlanguagemap.func_230503_a_(((TranslationTextComponent) ((TranslationTextComponent) component1).getFormatArgs()[i]).getKey());
                                        else if (((TranslationTextComponent) component1).getFormatArgs()[i] instanceof TextComponent)
                                            args[i] = ((TextComponent) ((TranslationTextComponent) component1).getFormatArgs()[i]).getString();
                                        else
                                            args[i] = ((TranslationTextComponent) component1).getFormatArgs()[i];
                                    }
                                    if (config.getGeneral().getInLine()) {
                                        newTooltip.add(new StringTextComponent(component.getString() + " [" + String.format(clientlanguagemap.func_230503_a_(((TranslationTextComponent) component1).getKey()), args) + "]").func_230530_a_(component.getStyle()));
                                    } else {
                                        newTooltip.add(component);
                                        newTooltip.add(new StringTextComponent("[" + String.format(clientlanguagemap.func_230503_a_(((TranslationTextComponent) component1).getKey()), args) + "]").func_230530_a_(component.getStyle()));
                                    }
                                }
                            }
                        }
                    } else {
                        newTooltip.add(component);
                    }

                    if (config.getGeneral().getDebugger()) {
                        String[] textCom = component.toString().split(",");
                        for (String s : textCom) {
                            if (!s.contains("=null") && !s.contains("=[]")) {
                                Minecraft.getInstance().fontRenderer.func_238405_a_(new MatrixStack(), s + ",", 10, (10) + (10 * posMul), 0xFFFFFF);
                                posMul++;
                            }
                        }
                        posMul++;
                    }
                }
                event.getToolTip().clear();
                event.getToolTip().addAll(newTooltip);
            }
        }
    }

    public static class Config {
        private ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        private ForgeConfigSpec spec;

        public ForgeConfigSpec getSpec() {
            return spec;
        }

        private General general;

        public Config() {
            general = new General();

            spec = builder.build();
        }

        public General getGeneral() {
            return general;
        }

        public class General {
            private final ForgeConfigSpec.ConfigValue<String> language;
            private final ForgeConfigSpec.BooleanValue inLine;
            private final ForgeConfigSpec.BooleanValue debugger;

            public General() {
                builder.push("general");

                language = builder.comment("Put the secondary language code here.").define("Language Code", "en_us");
                inLine = builder.comment("Enable this if you wanna render the translation on the same line.").define("In Line", false);
                debugger = builder.comment("Enables the debugger when rendering tooltip. For normal gameplay you want to leave this off.").define("Debugger", false);

                builder.pop();
            }

            public String getLanguage() {
                return language.get();
            }

            public boolean getInLine() {
                return inLine.get();
            }

            public boolean getDebugger() {
                return debugger.get();
            }
        }
    }
}
