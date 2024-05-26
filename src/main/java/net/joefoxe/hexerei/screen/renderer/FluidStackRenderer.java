package net.joefoxe.hexerei.screen.renderer;


import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CREDIT: https://github.com/mezz/JustEnoughItems by mezz
// Under MIT-License: https://github.com/mezz/JustEnoughItems/blob/1.18/LICENSE.txt
public class FluidStackRenderer<T> implements IIngredientRenderer<T> {
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    private static final int TEXTURE_SIZE = 16;
    private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

    public final int capacityMb;
    private final TooltipMode tooltipMode;
    @SuppressWarnings({"DeprecatedIsStillUsed"})
    @Nullable
    @Deprecated
    private final IDrawable overlay;
    private final int width;
    private final int height;

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }

    public FluidStackRenderer() {
        this(FluidType.BUCKET_VOLUME, TooltipMode.ITEM_LIST, 16, 16, null);
    }

    public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height) {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height, null);
    }

    @Deprecated
    public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height, overlay);
    }

    private FluidStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        Preconditions.checkArgument(capacityMb > 0, "capacity must be > 0");
        Preconditions.checkArgument(width > 0, "width must be > 0");
        Preconditions.checkArgument(height > 0, "height must be > 0");
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull T fluidStack) {
        RenderSystem.enableBlend();

        drawFluid(guiGraphics, width, height, fluidStack);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (overlay != null) {
            guiGraphics.pose().pushPose();
            {
                guiGraphics.pose().translate(0, 0, 200);
                overlay.draw(guiGraphics);
            }
            guiGraphics.pose().popPose();
        }
        RenderSystem.disableBlend();
    }

    public void render(GuiGraphics guiGraphics, int xPosition, int yPosition, @Nullable T ingredient) {
        if (ingredient != null) {
            guiGraphics.pose().pushPose();
            {
                guiGraphics.pose().translate(xPosition, yPosition, 0);
                render(guiGraphics, ingredient);
            }
            guiGraphics.pose().popPose();
        }
    }

    private void drawFluid(GuiGraphics guiGraphics, final int width, final int height, T fluidStack) {


        if (fluidStack instanceof FluidStack fluidStack1) {
            if (fluidStack1.getFluid().isSame(Fluids.EMPTY)) {
                return;
            }
            getStillFluidSprite(fluidStack1)
                    .ifPresent(fluidStillSprite -> {
                        int fluidColor = getColorTint(fluidStack1);

                        long amount = fluidStack1.getAmount();
                        long scaledAmount = (amount * height) / capacityMb;
                        if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
                            scaledAmount = MIN_FLUID_HEIGHT;
                        }
                        if (scaledAmount > height) {
                            scaledAmount = height;
                        }

                        drawTiledSprite(guiGraphics, width, height, fluidColor, scaledAmount, fluidStillSprite);
                    });
        }
    }

    public Optional<TextureAtlasSprite> getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidStill);
        return Optional.of(sprite)
                .filter(s -> s.atlasLocation() != MissingTextureAtlasSprite.getLocation());
    }

    public int getColorTint(FluidStack ingredient) {
        Fluid fluid = ingredient.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        return renderProperties.getTintColor(ingredient);
    }

    private static void drawTiledSprite(GuiGraphics guiGraphics, final int tiledWidth, final int tiledHeight, int color, long scaledAmount, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        setGLColorFromInt(color);

        final int xTileCount = tiledWidth / TEXTURE_SIZE;
        final int xRemainder = tiledWidth - (xTileCount * TEXTURE_SIZE);
        final int yTileCount = (int)(scaledAmount / TEXTURE_SIZE);
        final int yRemainder = (int)(scaledAmount - (yTileCount * TEXTURE_SIZE));

        final int yStart = tiledHeight;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = (xTile == xTileCount) ? xRemainder : TEXTURE_SIZE;
                int height = (yTile == yTileCount) ? yRemainder : TEXTURE_SIZE;
                int x = (xTile * TEXTURE_SIZE);
                int y = yStart - ((yTile + 1) * TEXTURE_SIZE);
                if (width > 0 && height > 0) {
                    int maskTop = TEXTURE_SIZE - height;
                    int maskRight = TEXTURE_SIZE - width;

                    drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
                }
            }
        }
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMin = uMin + (maskRight / 16F * (uMax - uMin));
        vMin = vMin + (maskTop / 16F * (vMax - vMin));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
        bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    @Override
    public List<Component> getTooltip(T fluidStack, TooltipFlag tooltipFlag) {
        if (fluidStack instanceof FluidStack fluidStack1){
            List<Component> tooltip = new ArrayList<>();
            Fluid fluidType = fluidStack1.getFluid();
            if (fluidType == null) {
                return tooltip;
            }

            Component displayName = fluidStack1.getDisplayName();
            if(fluidStack1.isEmpty())
                displayName = Component.translatable("book.hexerei.tooltip.empty");
            tooltip.add(displayName);

            int amount = fluidStack1.getAmount();
            if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
                MutableComponent amountString = Component.translatable("book.hexerei.tooltip.liquid.amount.with.capacity", nf.format(amount), nf.format(capacityMb));
                tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
            } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
                MutableComponent amountString = Component.translatable("book.hexerei.tooltip.liquid.amount", nf.format(amount));
                tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
            }

            return tooltip;
        }




        return null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
