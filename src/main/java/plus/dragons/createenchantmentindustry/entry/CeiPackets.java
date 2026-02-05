package plus.dragons.createenchantmentindustry.entry;

import java.util.function.Function;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterEditPacket;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideEditPacket;


public enum CeiPackets {

    // Client to Server
    CONFIGURE_ENCHANTING_GUIDE_FOR_BLAZE(EnchantingGuideEditPacket.class, EnchantingGuideEditPacket::new, SimplePacketBase.NetworkDirection.PLAY_TO_SERVER),
    CONFIGURE_BLAZE_ENCHANTER(BlazeEnchanterEditPacket.class, BlazeEnchanterEditPacket::new, SimplePacketBase.NetworkDirection.PLAY_TO_SERVER);

    public static final ResourceLocation CHANNEL_NAME = EnchantmentIndustry.genRL("main");
    public static final int NETWORK_VERSION = 1;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    public static SimpleChannel channel;

    private final CeiPackets.PacketType<?> packetType;

    <T extends SimplePacketBase> CeiPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
											SimplePacketBase.NetworkDirection direction) {
		packetType = new CeiPackets.PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
		channel = new SimpleChannel(CHANNEL_NAME);
        for (CeiPackets packet : values())
            packet.packetType.register();
    }

	public static SimpleChannel getChannel() {
		return channel;
	}

	public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
		getChannel().sendToClientsAround((S2CPacket) message, (ServerLevel) world, pos, range);
	}

    private static class PacketType<T extends SimplePacketBase> {
		private static int index = 0;

		private Function<FriendlyByteBuf, T> decoder;
		private Class<T> type;
		private SimplePacketBase.NetworkDirection direction;

		private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, SimplePacketBase.NetworkDirection direction) {
			decoder = factory;
			this.type = type;
			this.direction = direction;
		}

		private void register() {
			switch (direction) {
				case PLAY_TO_CLIENT -> getChannel().registerS2CPacket(type, index++, decoder);
				case PLAY_TO_SERVER -> getChannel().registerC2SPacket(type, index++, decoder);
			}
		}
    }
}
