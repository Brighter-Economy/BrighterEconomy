package brightspark.brightereconomy.network;

import brightspark.brightereconomy.economy.PlayerAccount;
import net.minecraft.item.ItemStack;

public record CustomerScreenDataPacket(PlayerAccount account, ItemStack stack, int cost, int stock) {}
