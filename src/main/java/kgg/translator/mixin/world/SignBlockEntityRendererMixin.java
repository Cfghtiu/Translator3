package kgg.translator.mixin.world;

import kgg.translator.handler.SignHelper;
import kgg.translator.option.Options;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRendererMixin {
    @Inject(method = "renderText", at = @At("HEAD"))
    public void renderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth, boolean front, CallbackInfo ci) {
        SignHelper.lineWidth = lineWidth;
        SignHelper.translate = Options.inRange(pos.toCenterPos());
    }
}
