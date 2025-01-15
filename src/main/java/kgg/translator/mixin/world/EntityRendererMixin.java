package kgg.translator.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import kgg.translator.handler.TranslateHelper;
import kgg.translator.option.Options;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<S extends EntityRenderState> {
    // todo 只翻译近距离实体

    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    /**
     * 修改传入的text值
     */
    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Text getText(Text text, @Local(argsOnly = true) S state) {

        if (!Options.autoEntityName.getValue()) return text;
        if (!Options.inRange(new Vec3d(state.x, state.y, state.z))) return text;
        if (!Options.autoPlayerName.getValue()) {
            if (state instanceof PlayerEntityRenderState) {
                return text;
            }
        }
        return TranslateHelper.translateNoWait(text);
    }
}
