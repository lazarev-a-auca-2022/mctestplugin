package ru.test.la.testplugin2;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class DiamondZombie {

    public DiamondZombie(Location location){
        location = location.clone().add(0,2,0);

        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        zombie.setCustomName(ChatColor.BLUE + "Diamond Zombie");
        zombie.setCustomNameVisible(true);
        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }
}