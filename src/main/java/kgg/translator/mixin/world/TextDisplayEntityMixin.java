package kgg.translator.mixin.world;

import kgg.translator.handler.TranslateHelper;
import kgg.translator.option.Options;
import kgg.translator.translator.Source;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayEntityMixin extends DisplayEntity {
    @Shadow @Nullable private DisplayEntity.TextDisplayEntity.@Nullable TextLines textLines;
    @Unique
    private boolean translated = false;
    @Unique
    private boolean updated = false;

    public TextDisplayEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyVariable(method = "splitLines", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public DisplayEntity.TextDisplayEntity.LineSplitter getLineSplitter(DisplayEntity.TextDisplayEntity.LineSplitter splitter) {
        if (translated != Options.autoEntityName.getValue()) {
            translated = Options.autoEntityName.getValue();
            updated = true;
        }

        if (updated) {
            updated = false;
            textLines = null;
        }

        return (text, w) -> {
            if (translated && Options.inRange(getPos())){
                text = TranslateHelper.translateNoWait(text, t -> {
                    updated = true;
                }, Source.ENTITY_NAME);
            }
            return splitter.split(text, w);
        };
    }
}
