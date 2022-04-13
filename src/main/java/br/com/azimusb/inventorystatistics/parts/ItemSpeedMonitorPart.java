package br.com.azimusb.inventorystatistics.parts;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.client.render.TesrRenderHelper;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.StorageMonitorPart;
import br.com.azimusb.inventorystatistics.helpers.SpeedHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ItemSpeedMonitorPart extends StorageMonitorPart implements IGridTickable {
    private final AENetworkProxy gridProxy = this.getProxy();

    private final SpeedHelper speedHelper = new SpeedHelper();

    private String lastHumanReadableText = "0.0/" + speedHelper.getUnit();
    private double lastSpeed = 0.0;

    private boolean skippedFirstUpdate = false;

    public ItemSpeedMonitorPart(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(PlayerEntity player, Hand hand, Vector3d pos) {
        Item prevItem = getDisplayed() != null ? getDisplayed().getItem() : null;
        boolean ret = super.onPartActivate(player, hand, pos);

        if (isRemote()) {
            return ret;
        }

        if (!player.getHeldItem(hand).isEmpty()) {
            if (player.getHeldItem(hand).getItem().equals(prevItem)) {
                speedHelper.setNextUnit();
                player.sendMessage(new StringTextComponent("Changed unit to " + speedHelper.getUnitFullName() + "."), player.getUniqueID());
                lastHumanReadableText = getRenderedItemSpeed(lastSpeed);
                this.getHost().markForUpdate();
            } else {
                this.speedHelper.reset();

                this.lastSpeed = 0.0;
                this.lastHumanReadableText = "0.0/" + speedHelper.getUnit();
                //buffer.clear();
                this.getHost().markForUpdate();
            }
        }

        return ret;
    }

    @Override
    public void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);

        data.writeString(this.lastHumanReadableText);
        data.writeDouble(this.lastSpeed);
    }

    @Override
    public boolean readFromStream(final PacketBuffer data) throws IOException {
        boolean needRedraw = super.readFromStream(data);

        lastHumanReadableText = data.readString();
        lastSpeed = data.readDouble();

        return needRedraw;
    }

    @Override
    public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, IActionSource src, IStorageChannel chan) {
        if (!skippedFirstUpdate) {
            skippedFirstUpdate = true;
            return;
        }
        long stackSize = fullStack.getStackSize();
        long gameTime = getLocation().getWorld().getGameTime();

        speedHelper.add(stackSize, gameTime);

        if (speedHelper.getStartTime() == -1) {
            speedHelper.setStart(
                    stackSize - diffStack.getStackSize(), // Use previous value
                    gameTime);
        }

        try {
            this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
        } catch (GridAccessException e) {
            //
        }

        super.onStackChange(o, fullStack, diffStack, src, chan);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffers,
                              int combinedLightIn, int combinedOverlayIn) {

        if ((this.getClientFlags() & (PanelPart.POWERED_FLAG | PanelPart.CHANNEL_FLAG)) != (PanelPart.POWERED_FLAG
                | PanelPart.CHANNEL_FLAG)) {
            return;
        }

        final IAEItemStack ais = this.getDisplayed();

        if (ais == null) {
            return;
        }

        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5); // Move into the center of the block

        Direction facing = this.getSide().getFacing();

        TesrRenderHelper.rotateToFace(matrixStack, facing, this.getSpin());

        matrixStack.translate(0, 0.05, 0.5);

        TesrRenderHelper.renderItem2d(matrixStack, buffers, ais.asItemStackRepresentation(), 0.4f, 15728880, combinedOverlayIn);

        final FontRenderer fr = Minecraft.getInstance().fontRenderer;

        final double speed = lastSpeed; //getItemSpeed();
        final String renderedItemSpeed = lastHumanReadableText;// getRenderedItemSpeed(speed);

        final int width = fr.getStringWidth(renderedItemSpeed);
        matrixStack.push();
        matrixStack.translate(0.0f, -0.23f, 0.02f);
        matrixStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        matrixStack.scale(0.5f, 0.5f, 0);
        matrixStack.translate(-0.5f * width, 0.0f, 0.5f);

        int color = AEColor.BLACK.mediumVariant;
        if (speed > 0.0) {
            color = AEColor.GREEN.blackVariant;
        } else if (speed < 0.0) {
            color = AEColor.RED.mediumVariant;
        }

        fr.renderString(renderedItemSpeed, 0, 0, color, false, matrixStack.getLast().getMatrix(), buffers, false, 0,
                15728880);
        matrixStack.pop();

        matrixStack.pop();
    }

    private String getRenderedItemSpeed(double itemSpeed) {
        return speedHelper.getRenderedItemSpeed(itemSpeed);
    }

    private double getItemSpeed() {
        return speedHelper.getItemSpeed();
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(5, 40, speedHelper.getStartTime() == -1,
                true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!this.gridProxy.isActive() || this.getDisplayed() == null || speedHelper.getStartTime() == -1) {
            return TickRateModulation.SLEEP;
        }

        speedHelper.setCurrent(this.getDisplayed().getStackSize(), getLocation().getWorld().getGameTime());

        if (speedHelper.getStartTime() == -1) {
            speedHelper.setStartToCurrent();
            return TickRateModulation.SAME;
        }

        final double speed = getItemSpeed();

        if (speedHelper.passedTicks() > 200) {
            speedHelper.reset();

            this.lastHumanReadableText = "0.0/" + speedHelper.getUnit();
            this.lastSpeed = 0.0;
            this.getHost().markForUpdate();

            return TickRateModulation.SLEEP;
        }

        if (speedHelper.passedTicks() >= 5) {
            this.lastHumanReadableText = getRenderedItemSpeed(speed);
            this.lastSpeed = speed;
            this.getHost().markForUpdate();
        }

        return TickRateModulation.SAME;
    }
}
