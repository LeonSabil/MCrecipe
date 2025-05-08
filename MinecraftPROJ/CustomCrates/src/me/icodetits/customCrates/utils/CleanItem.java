package me.icodetits.customCrates.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

public class CleanItem {

	@Getter private Material material;
	@Getter private int amount;
	@Getter private String name, owner;
	@Getter private List<String> lores;
	@Getter private List<Enchant> enchs;
	@Getter private short durability;
	@Getter private MaterialData data;
	@Getter private boolean unbreakable, glow;
	@Getter private Color color;

    public CleanItem(Material material) {
    	this(material, 1);
    }

    public CleanItem(Material material, int amount) {
    	this.material = material;
    	this.amount = amount;
    	
    	// * Default Values *
    	this.name = null;
    	this.lores = Lists.newArrayList();
    	this.enchs = Lists.newArrayList();
    	this.durability = Short.MAX_VALUE;
    	this.data = null;
    	this.unbreakable = false;
    	this.color = null;
    }

    public CleanItem amount(int amount) {
        this.amount = amount;
        return this;
    }

    public CleanItem withName(String name) {
        this.name = name;
        return this;
    }

    public CleanItem withLores(List<String> lores) {
        this.lores.addAll(lores);
        return this;
    }
    
    public CleanItem withLores(String... lores) {
        withLores(Arrays.asList(lores));
        return this;
    }

    public CleanItem withLore(String lore) {
        this.lores.add(lore);
        return this;
    }
    
    public CleanItem unbreakable() {
    	this.unbreakable = !this.unbreakable;
    	return this;
    }
    
    public CleanItem glow() {
    	this.glow = !this.glow;
    	return this;
    }

	public CleanItem withEnchantment(Enchantment enchant, int level) {
		this.enchs.add(new Enchant(enchant, level));
		return this;
	}

    public CleanItem withDurability(short durability) {
        this.durability = durability;
        return this;
    }

    public CleanItem withData(MaterialData data) {
        this.data = data;
        return this;
    }
    
    public CleanItem withColor(Color color) {
		if (getMaterial() == null || !getMaterial().name().contains("LEATHER")) {
			return this;
		}
    	
    	this.color = color;
    	return this;
    }
    
    public CleanItem withOwner(String owner) {
    	this.owner = owner;
		return this;
    }
    
	@Override
    public CleanItem clone() {
		CleanItem item = new CleanItem(this.material, this.amount);
		item.name = this.name;
		item.lores = this.lores;
		item.enchs = this.enchs;
		item.durability = this.durability;
		item.data = this.data;
		item.unbreakable = this.unbreakable;
		item.color = this.color;
		return item;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(getMaterial(), getAmount());
        ItemMeta meta = item.getItemMeta();
        
        // * Start setting ItemMeta tags. *
        if (getName() != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getName()));
        }
        
        if (getLores() != null && !(getLores().isEmpty())) {
        	for (int i = 0; i < getLores().size(); i++) {
        		String str = getLores().get(i);
        		str = ChatColor.translateAlternateColorCodes('&', str);
        		
        		getLores().set(i, str);
        	}
        	
            meta.setLore(getLores());
        }
        
		if (isUnbreakable()) {
			try {
				meta.spigot().setUnbreakable(unbreakable);
			} catch (Exception ignore) {
				System.out.println("Error: Can't set ItemStack as unbreakable. (Perhaps you're not running Spigot 1.8 or above?)");
			}
		}
		
		if (isGlow()) {
			try {
				getEnchs().add(new Enchant(Enchantment.DURABILITY, 10));
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			} catch (Exception ignore) {
				System.out.println("Error: Unknown ItemStack tag GLOW. (Perhaps you're not running Spigot 1.8 or above?)");
			}
		}
		
		if (getColor() != null && meta instanceof LeatherArmorMeta) {
			((LeatherArmorMeta) meta).setColor(getColor());
		}
		
		if (getOwner() != null && meta instanceof SkullMeta) {
			((SkullMeta) meta).setOwner(getOwner());
		}
		
        for (Enchant e : getEnchs()) {
        	if(meta instanceof EnchantmentStorageMeta) {
        		((EnchantmentStorageMeta) meta).addStoredEnchant(e.getEnchantment(), e.getLevel(), true);
        	} else {
        		meta.addEnchant(e.getEnchantment(), e.getLevel(), true);
        	}
        }
        
        item.setItemMeta(meta);
        // * Done setting ItemMeta tag. *
        
        if (getDurability() != Short.MAX_VALUE) {
            item.setDurability(getDurability());
        }
        
        if (getData() != null) {
            item.setData(getData());
        }
        
        return item;
    }

    public class Enchant {
    	
        @Getter @Setter private Enchantment enchantment;
        @Getter @Setter private int level;

        public Enchant(Enchantment enchantment, int level) {
	        this.enchantment = enchantment;
	        this.level = level;
        }
    }
}