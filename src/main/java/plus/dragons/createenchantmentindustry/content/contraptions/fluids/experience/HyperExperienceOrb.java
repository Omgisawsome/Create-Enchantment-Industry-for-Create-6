package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import io.github.fabricators_of_create.porting_lib.entity.IEntityAdditionalSpawnData;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public class HyperExperienceOrb extends ExperienceOrb implements IEntityAdditionalSpawnData {

    public HyperExperienceOrb(Level level, double x, double y, double z, int value) {
        this(CeiEntityTypes.HYPER_EXPERIENCE_ORB.get(), level);
        this.setPos(x, y, z);
        this.setYRot((float)(this.random.nextDouble() * 360.0D));
        this.setDeltaMovement((this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D);
        this.value = value;
    }

    public HyperExperienceOrb(EntityType<? extends HyperExperienceOrb> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("unchecked")
    public static FabricEntityTypeBuilder<?> build(FabricEntityTypeBuilder<?> builder) {
        return builder.dimensions(EntityDimensions.fixed(.5f, .5f));
    }

    public void applyPlayerEffects(Player player, int expAmount) {
        CeiFluids.HYPER_EXPERIENCE.get().applyAdditionalEffects(player, expAmount);
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide) {
            if (player.takeXpDelay == 0) {
                player.takeXpDelay = 2;
                player.take(this, 1);
                int i = this.repairPlayerItems(player, this.value);
                if (i > 0) {
                    player.giveExperiencePoints(i);
                    applyPlayerEffects(player, i);
                }

                --this.count;
                if (this.count == 0) {
                    this.discard();
                }
            }
        }
    }

    public int getIcon() {
        int value = this.value / 10;
        if (value >= 2477) {
            return 10;
        } else if (value >= 1237) {
            return 9;
        } else if (value >= 617) {
            return 8;
        } else if (value >= 307) {
            return 7;
        } else if (value >= 149) {
            return 6;
        } else if (value >= 73) {
            return 5;
        } else if (value >= 37) {
            return 4;
        } else if (value >= 17) {
            return 3;
        } else if (value >= 7) {
            return 2;
        } else {
            return value >= 3 ? 1 : 0;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

	@Override
	public void readSpawnData(FriendlyByteBuf buf) {
		value = buf.readInt();
		setPos(buf.readDouble(),buf.readDouble(),buf.readDouble());
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buf) {
		buf.writeInt(value);
		buf.writeDouble(position().x);
		buf.writeDouble(position().y);;
		buf.writeDouble(position().z);
	}
}
