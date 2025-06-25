package com.hypericats.oregoatsr.client.config;

import com.hypericats.oregoatsr.client.features.BlockEsp;
import com.hypericats.oregoatsr.client.features.EntityEsp;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.impl.controller.BooleanControllerBuilderImpl;
import dev.isxander.yacl3.impl.controller.StringControllerBuilderImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Oreconfig {
    private static Oreconfig INSTANCE;

    private YetAnotherConfigLib.Builder builder;
    private Stack<OptionAddable> optionStack;

    private Oreconfig() {

    }

    public static void init() {
        if (INSTANCE != null) return;
        INSTANCE = new Oreconfig();
    }

    public static Oreconfig getInstance() {
        return INSTANCE;
    }

    public void open() {
        if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) return; // Make sure we dont inv walk

        MinecraftClient.getInstance().currentScreen = getScreen(null);
    }

    public Screen getScreen(Screen parent) {
        optionStack = new Stack<>();
        this.builder = YetAnotherConfigLib.createBuilder().title(Text.literal("Ore Config!"));

        addCategory("General", "General features");
        finish();
        addCategory("Foraging", "Foraging features");
        // TODO: Only show nearest

        addGroup("Block ESP", "Highlights certain blocks");
        addOption("Toggle", "Toggles Block ESP", false, BlockEsp::isEnabled, BlockEsp::setEnabled, BooleanControllerBuilderImpl::new);
        addOption("Show Outline", "Show ESP block outline", true, BlockEsp::isShowOutline, BlockEsp::setShowOutline, BooleanControllerBuilderImpl::new);
        addOption("Show Tracer", "Show ESP block tracer", false, BlockEsp::isShowTracer, BlockEsp::setShowTracer, BooleanControllerBuilderImpl::new);
        addOption("Block Name Filter", "Filters block names to match this string. Separate blocks with ;", "", BlockEsp::getNameFilter, BlockEsp::setNameFilter, StringControllerBuilderImpl::new);
        finish();


        addGroup("Entity ESP", "Highlights certain entities");
        addOption("Toggle", "Toggles Entity ESP", false, EntityEsp::isEnabled, EntityEsp::setEnabled, BooleanControllerBuilderImpl::new);
        addOption("Show Outline", "Show ESP entity outline", true, EntityEsp::isShowOutline, EntityEsp::setShowOutline, BooleanControllerBuilderImpl::new);
        addOption("Show Tracer", "Show ESP entity tracer", false, EntityEsp::isShowTracer, EntityEsp::setShowTracer, BooleanControllerBuilderImpl::new);
        addOption("Entity Type Filter", "Filters types to match this string. Separate types with ;", "", EntityEsp::getEntityTypeFilter, EntityEsp::setEntityTypeFilter, StringControllerBuilderImpl::new);
        addOption("Entity Name Filter", "Filters entity names to match this string. Separate names with ;", "", EntityEsp::getEntityNameFilter, EntityEsp::setEntityNameFilter, StringControllerBuilderImpl::new);
        finish();


        finishAll();
        Screen screen = builder.build().generateScreen(parent);
        this.builder = null;
        return screen;
    }

    private void addOption(Option<?> option) {
        optionStack.peek().option(option);
    }

    private void addGroup(String name, String description) {
        optionStack.push(OptionGroup.createBuilder().name(Text.of(name)).description(OptionDescription.of(Text.of(description))));
    }

    private void addCategory(String name, String description) {
        optionStack.push(ConfigCategory.createBuilder()
                .name(Text.of(name))
                .tooltip(Text.of(description)));

    }

    private void finishAll() {
        finish(Integer.MAX_VALUE);
    }

    private void finish(int count) {
        for (int i = 0; i < count; i++) {
            if (!finish()) return;
        }
    }

    private boolean finish() {
        if (optionStack.isEmpty()) return false;

        OptionAddable addable = optionStack.pop();

        if (addable instanceof ConfigCategory.Builder configBuilder) {
            ConfigCategory category = configBuilder.build();

            if (optionStack.isEmpty()) {
                builder.category(category);
                return true;
            }

            throw new IllegalStateException("Category was not last object!");
        }

        if (addable instanceof OptionGroup.Builder groupBuilder) {
            OptionGroup group = groupBuilder.build();

            if (optionStack.isEmpty()) {
                throw new IllegalStateException("Last object in stack not category!");
            }

            if (optionStack.peek() instanceof ConfigCategory.Builder category) {
                category.group(group);
                return true;
            }

            throw new IllegalStateException("Cannot add group to group!");
        }

        throw new IllegalStateException("Invalid Addable found!");
    }

    private<T> void addOption(String name, String description, T defaultValue, Supplier<T> getter, Consumer<T> listener, Function<Option<T>, ControllerBuilder<T>> controller) {
         addOption(Option.<T>createBuilder()
                .name(Text.of(name))
                .description(OptionDescription.of(Text.of(description)))
                .binding(
                        defaultValue,
                        getter,
                        listener
                )
                .controller(controller)
                .build());
    }

    private Option.Builder<Boolean> getString(String name, String description, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> listener) {
        return Option.<Boolean>createBuilder()
                .name(Text.of(name))
                .description(OptionDescription.of(Text.of(description)))
                .binding(
                        defaultValue,
                        getter,
                        listener
                )
                .controller(TickBoxControllerBuilder::create);
    }



}
