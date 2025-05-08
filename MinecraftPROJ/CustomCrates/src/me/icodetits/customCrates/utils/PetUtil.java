package me.icodetits.customCrates.utils;

import org.pvpingmc.pets.data.*;
import org.pvpingmc.pets.abilities.*;
import org.pvpingmc.pets.*;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

public class PetUtil {
	
	public static final EnumPet PET_TYPE_CRATE = EnumPet.CRATE;

	public static boolean petSupport() {
		return me.icodetits.customCrates.Main.getInstance().externalPetHook();
	}
	
	public static boolean petHasCrate(Player player) {
		return PetUtil.petMatchCrate(player) != null;
	}
	
	public static Object petMatchCrate(Player player) {
		return PetDataManger.getInstance().getHighestTypeInInventory(player, PET_TYPE_CRATE);
	}
	
	public static PetData cast(Object petData) {
		return PetUtil.cast(petData, false);
	}
	
	public static PetData cast(Object petData, boolean fastRetr) {
		if (!fastRetr) {
			Preconditions.checkArgument(petData instanceof PetData, "arg was %s but expected PetData", petData.getClass().getSimpleName());
		}
		return (PetData) petData;
	}
	
	/* ---
	 * Assume petData is instance already no need to take precautions
	 --- */
	
	public static String petName(Object petData) {
		return cast(petData, true).getPetName();
	}
	
	public static int petLevel(Object petData) {
		return cast(petData, true).getLevel();
	}
	
	public static int petExpMax(Object petData) {
		return cast(petData, true).getMaxExp();
	}
	
	public static int petExp(Object petData) {
		return cast(petData, true).getExp();
	}
	
	public static int petProgress(Object petData) {
		return cast(petData, true).getProgress();
	}
	
	public static UUID petUniqueId(Object petData) {
		return cast(petData, true).getUuid();
	}
	
	public static EnumPet petType(Object petData) {
		return cast(petData, true).getType();
	}
	
	public static void petCall(Object petData, Player player) {
		cast(petData, true).call(player);
	}
	
	public static void petActivate(Object petData, Player player) {
		PetUtil.petLastUse(petData, System.currentTimeMillis());
		for (int i = 0; i < 5; i++) {
			PetUtil.petCall(petData, player);
		}
		Main.getInstance().sendMessage(player, "activated", new org.pvpingmc.pets.utils.ObjectSet("%pet%", cast(petData, true).getPetName()));
		Main.getInstance().playSound(player, "activated");
	}
	
	public static void petLastUse(Object petData, long lastUse) {
		cast(petData, true).setLastUse(lastUse);
	}
}
