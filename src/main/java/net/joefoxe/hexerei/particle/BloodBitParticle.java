package net.joefoxe.hexerei.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.joefoxe.hexerei.Hexerei;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BloodBitParticle extends TextureSheetParticle {

    private final ResourceLocation TEXTURE = new ResourceLocation(Hexerei.MOD_ID,
            "textures/particle/cauldron_boil_particle.png");
    // thanks to understanding simibubi's code from the Create mod for rendering particles I was able to render my own :D
    public static final Vec3[] CUBE = {

            //middle top inside
            new Vec3(0.1, -0.01, -0.1),
            new Vec3(0.1, -0.01, 0.1),
            new Vec3(-0.1, -0.01, 0.1),
            new Vec3(-0.1, -0.01, -0.1),
            // middle bottom render
            new Vec3(-0.1, 0.01, -0.1),
            new Vec3(-0.1, 0.01, 0.1),
            new Vec3(0.1, 0.01, 0.1),
            new Vec3(0.1, 0.01, -0.1),


    };

    public static final Vec3[] CUBE_NORMALS = {
            // modified normals for the sides
            new Vec3(0, 0, 0.5),
            new Vec3(0, 0, 0.5),
    };

    public final static ResourceLocation TEXTURE_BLANK =
            new ResourceLocation(Hexerei.MOD_ID, "textures/block/blank.png");
    private static final ParticleRenderType renderType = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.setShaderTexture(0, TEXTURE_BLANK);

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    };

    protected float scale;
    protected float rotationDirection;
    protected float rotation;
    protected float rotationOffsetYaw;
    protected float rotationOffsetPitch;
    protected float rotationOffsetRoll;
    protected float colorOffset;


    public BloodBitParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.rotation = 0;

        averageAge(80);

        Random random = new Random();

        this.colorOffset = (random.nextFloat() * 0.25f);
        this.rotationOffsetYaw = random.nextFloat();
        this.rotationOffsetPitch = random.nextFloat();
        this.rotationOffsetRoll = random.nextFloat();

        setScale(0.2F);
        setRotationDirection(random.nextFloat() - 0.5f);
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.setSize(scale * 0.5f, scale * 0.5f);
    }

    public void averageAge(int age) {
        Random random = new Random();
        this.lifetime = (int) (age + (random.nextDouble() * 2D - 1D) * 8);
    }

    public void setRotationDirection(float rotationDirection) {
        this.rotationDirection = rotationDirection;
    }


    @Override
    public void tick() {

        this.rotation = (this.rotationDirection * 0.1f) + this.rotation;

        super.tick();
    }

    @Override
    public void render(VertexConsumer builder, Camera renderInfo, float p_225606_3_) {
        Vec3 projectedView = renderInfo.getPosition();
        float lerpX = (float) (Mth.lerp(p_225606_3_, this.xo, this.x) - projectedView.x());
        float lerpY = (float) (Mth.lerp(p_225606_3_, this.yo, this.y) - projectedView.y());
        float lerpZ = (float) (Mth.lerp(p_225606_3_, this.zo, this.z) - projectedView.z());

        int light = 15728880;
        double ageMultiplier = 1 - Math.pow(Mth.clamp(age + p_225606_3_, 0, lifetime), 3) / Math.pow(lifetime, 3);

        RenderSystem._setShaderTexture(0, TEXTURE);

        for (int i = 0; i < CUBE.length / 4; i++) {
            for (int j = 0; j < 4; j++) {
                Vec3 vec = CUBE[i * 4 + j];
                vec = vec
                        .yRot(this.rotation + this.rotationOffsetYaw)
                        .xRot(this.rotation + this.rotationOffsetPitch)
                        .zRot(this.rotation + this.rotationOffsetRoll)
                        .scale(scale * ageMultiplier)
                        .add(lerpX, lerpY, lerpZ);

                Vec3 normal = CUBE_NORMALS[i];

                builder.vertex(vec.x, vec.y, vec.z)
                        .uv(0, 0)
                        .color(Mth.clamp(rCol * 0.8f, 0, 1.0f), Mth.clamp(gCol * 0.8f, 0, 1.0f), Mth.clamp(bCol * 0.8f, 0, 1.0f), alpha)
                        .normal((float) normal.x, (float) normal.y, (float) normal.z)
                        .uv2(light)
                        .endVertex();

            }
        }
    }



    @Override
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodBitParticle cauldronParticle = new BloodBitParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            Random random = new Random();

            float colorOffset = (random.nextFloat() * 0.10f);
            cauldronParticle.setColor(0.025f + colorOffset, 0.05f, 0.05f);

            cauldronParticle.setAlpha(1.0f);


            cauldronParticle.pickSprite(this.spriteSet);
            return cauldronParticle;

        }
    }


}
