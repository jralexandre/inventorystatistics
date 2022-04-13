package br.com.azimusb.inventorystatistics.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemSpeedAltarRenderer extends TileEntityRenderer<ItemSpeedAltarTileEntity> {
    private final Minecraft mc = Minecraft.getInstance();

    public ItemSpeedAltarRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(ItemSpeedAltarTileEntity te, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.getDisplayed().equals(ItemStack.EMPTY) || te.getDisplayed().getItem().equals(Items.AIR))
            return;

        ItemStack item = te.getDisplayed();

        ClientPlayerEntity player = mc.player;

        int lightLevel = getLightLevel(te.getWorld(), te.getPos().up());

        renderItem(item, new double[] { 0.5d, 0.5d, 0.5d },
                Vector3f.YP.rotationDegrees(180f - player.rotationYaw), matrixStackIn, bufferIn, partialTicks,
                combinedOverlayIn, lightLevel, 0.8f);

        ITextComponent label = new StringTextComponent(te.getLastHumanReadableText());

        int color = 0xffffff;
        if (te.getLastSpeed() > 0.0)
            color = 0x00ff00;
        else if (te.getLastSpeed() < 0.0)
            color = 0xff0000;

        renderLabel(matrixStackIn, bufferIn, lightLevel, new double[] { .5d, 0.6d, .5d }, label, color);
    }

    private void renderItem(ItemStack stack, double[] translation, Quaternion rotation, MatrixStack matrixStack,
                            IRenderTypeBuffer buffer, float partialTicks, int combinedOverlay, int lightLevel, float scale) {
        matrixStack.push();
        matrixStack.translate(translation[0], translation[1], translation[2]);
        matrixStack.rotate(rotation);
        matrixStack.scale(scale, scale, scale);

        IBakedModel model = mc.getItemRenderer().getItemModelWithOverrides(stack, null, null);
        mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, true, matrixStack, buffer,
                lightLevel, combinedOverlay, model);
        matrixStack.pop();
    }

    private void renderLabel(MatrixStack stack, IRenderTypeBuffer buffer, int lightLevel, double[] corner,
                             ITextComponent text, int color) {

        FontRenderer font = mc.fontRenderer;

        stack.push();
        float scale = 0.01f;
        int opacity = (int) (.4f * 255.0f) << 24;
        float offset = (float) (-font.getStringPropertyWidth(text) / 2);
        Matrix4f matrix = stack.getLast().getMatrix();

        stack.translate(corner[0], corner[1] + .4f, corner[2]);
        stack.scale(scale, scale, scale);
        stack.rotate(mc.getRenderManager().getCameraOrientation());
        stack.rotate(Vector3f.ZP.rotationDegrees(180f));

        font.func_243247_a(text, offset, 0, color, false, matrix, buffer, false, opacity, lightLevel);
        stack.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightFor(LightType.BLOCK, pos);
        int sLight = world.getLightFor(LightType.SKY, pos);
        return LightTexture.packLight(bLight, sLight);
    }
}
