package com.xFaraday.lobbyPvP;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftCreeper;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.connorlinfoot.bountifulapi.BountifulAPI;

import net.minecraft.server.v1_11_R1.EntityCreeper;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.projectiles.ProjectileSource;

public class mainLobbyPvP extends JavaPlugin implements Listener {

    Plugin plugin = this;

    @Override
    public void onEnable() {
        getLogger().info("LobbyPvP was started!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("LobbyPvP was stoped!");
    }

    @EventHandler
    public void onDamagePlayers(EntityDamageByEntityEvent e) {
        if ((e.getDamager() instanceof Player)
                && ((e.getEntity() instanceof Player))) {
            Player p = (Player) e.getEntity(), d = (Player) e.getDamager();
            double damagePlayers = (int) p.getHealth() - e.getDamage();
            double damage = e.getDamage();
            damagePlayers = damagePlayers < 0 ? 0 : damagePlayers;
            BountifulAPI.sendActionBar(d, p.getDisplayName().replace('&', '§') + " &7➠ &4".replace('&', '§') + "&4❤ &6".replace('&', '§') + String.format("%.1f", damagePlayers) + "&7(&e".replace('&', '§') + String.format("%.1f", damage) + "&7)".replace('&', '§'), -1);
        }
    }

    @EventHandler
    public void onShootBow(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player && e.getCause().equals(DamageCause.PROJECTILE)) {
            Entity damager = e.getDamager();
            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                Player p = (Player) e.getEntity();
                ProjectileSource shoot = arrow.getShooter();
                if (!(shoot instanceof Player)) {
                    return;
                }
                if (p instanceof Player) {
                    Player h = (Player) entity;
                    double damage = e.getFinalDamage();
                    double hp = h.getHealth() - damage;
                    hp = hp < 0 ? 0 : hp;
                    BountifulAPI.sendActionBar((Player) shoot, h.getDisplayName().replace('&', '§') + " &7➠ &4".replace('&', '§') + "&4❤ &6 ".replace('&', '§') + String.format("%.1f", hp) + "&7(&e".replace('&', '§') + String.format("%.1f", damage) + "&7)".replace('&', '§'), -1);
                }
            }
        }
    }

    @EventHandler
    public void ArrowBreakGlassThree(ProjectileHitEvent ev) {
        String type = "normal";
        if (ev.getHitEntity() != null) {
            return;
        }
        if (ev.getEntity() instanceof Arrow || ev.getEntity() instanceof SpectralArrow) {
            if (ev.getEntity() instanceof SpectralArrow) {
                type = "spectral";
            }
            if (ev.getEntity() instanceof TippedArrow) {
                type = "tipped";
            }
            Material material = ev.getHitBlock().getType();
            if (material.equals(Material.THIN_GLASS) || material.equals(Material.GLASS)) {
                Block block = ev.getHitBlock();
                block.breakNaturally();
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 4, 0);
                block.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 9);
                block.getWorld().spawnParticle(Particle.CRIT_MAGIC, block.getLocation(), 15);
                block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation(), 9);
                Arrow arrow = (Arrow) ev.getEntity();
                Player player = (Player) arrow.getShooter();
                arrow.remove();
                switch (type) {
                    case "spectral":
                        SpectralArrow spec = block.getWorld().spawnArrow(arrow.getLocation(), arrow.getVelocity().multiply(4), 1, 1, SpectralArrow.class);
                        spec.setPickupStatus(Arrow.PickupStatus.ALLOWED);
                        spec.setShooter(player);
                        break;
                    case "tipped":
                        TippedArrow tipped = block.getWorld().spawnArrow(arrow.getLocation(), arrow.getVelocity().multiply(4), 1, 1, TippedArrow.class);
                        tipped.setPickupStatus(Arrow.PickupStatus.ALLOWED);
                        tipped.setShooter(player);
                        TippedArrow tip = (TippedArrow) ev.getEntity();
                        PotionData potion = tip.getBasePotionData();
                        tipped.setBasePotionData(potion);
                        break;
                    default:
                        Arrow normal = block.getWorld().spawnArrow(arrow.getLocation(), arrow.getVelocity().multiply(4), 1, 1);
                        normal.setPickupStatus(Arrow.PickupStatus.ALLOWED);
                        normal.setShooter(player);
                        break;

                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(material);
                    }
                }.runTaskLater(plugin, 200);
            }
        }
    }

    @EventHandler
    public void DeathMessage(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (event.getDeathMessage().endsWith("was blown up by Міна")) {
            event.setDeathMessage("&c".replace('&', '§') + player.getName() + "&f підірвався на міні".replace('&', '§'));
        } else if (event.getDeathMessage().endsWith("was blown up by Граната")) {
            event.setDeathMessage("&c".replace('&', '§') + player.getName() + "&f вбитий гранатою".replace('&', '§'));
        }
    }

    @EventHandler
    public void Granades(PlayerEggThrowEvent ev) {
        Egg e = ev.getEgg();
        CraftCreeper creep = e.getWorld().spawn(e.getLocation(), CraftCreeper.class);
        EntityCreeper nms = creep.getHandle();
        NBTTagCompound nbttag = new NBTTagCompound();
        nms.c(nbttag);
        creep.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 60));
        creep.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 60));
        creep.setCustomName("Граната");
        nbttag.setInt("Fuse", 0);
        EntityLiving livingcreeper = (EntityLiving) nms;
        livingcreeper.a(nbttag);
    }

    @EventHandler
    public void Mine(PlayerInteractEvent ev) {
        if (ev.getAction().equals(Action.PHYSICAL)) {
            Block b = ev.getClickedBlock();
            if (b.getType() == Material.STONE_PLATE) {
                CraftCreeper e = b.getWorld().spawn(b.getLocation().add(0.5, 0, 0.5), CraftCreeper.class);
                EntityCreeper nms = e.getHandle();
                NBTTagCompound nbttag = new NBTTagCompound();
                nms.c(nbttag);
                e.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 60));
                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 60));
                e.setCustomName("Міна");
                nbttag.setInt("Fuse", 0);
                EntityLiving livingcreeper = (EntityLiving) nms;
                livingcreeper.a(nbttag);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        b.setType(Material.AIR);
                    }
                }.runTaskLater(plugin, 1);
            }
        }
    }
}
