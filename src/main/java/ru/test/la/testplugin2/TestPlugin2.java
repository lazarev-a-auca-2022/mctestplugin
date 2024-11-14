package ru.test.la.testplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class TestPlugin2 extends JavaPlugin implements Listener {

    private Map<Zombie, Double> bossHealthMap = new HashMap<>();
    private Random random = new Random();
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective bossObjective;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("TestPlugin2 started");

        scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();

        bossObjective = scoreboard.registerNewObjective("bossHealth", "health");
        bossObjective.setDisplayName(ChatColor.RED + "Boss Health");
        bossObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        scheduleDailyBossSpawn();
    }

    private void scheduleDailyBossSpawn() {
        long ticksPerDay = 24000L;
        long currentTime = Bukkit.getWorlds().get(0).getTime();
        long targetTime = 12000L;
        long delay = (targetTime - currentTime + ticksPerDay) % ticksPerDay;
        Bukkit.getScheduler().runTaskTimer(this, this::spawnBossForAllPlayers, delay, ticksPerDay);
    }

    private void spawnBossForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location spawnLocation = player.getLocation().clone().add(0, 2, 0);
            Zombie boss = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
            boss.setCustomName(ChatColor.RED + "Fear of " + player.getName());
            boss.setCustomNameVisible(true);
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            boss.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            boss.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            boss.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            boss.setHealth(100.0);

            bossHealthMap.put(boss, 100.0);

            boss.getScoreboardTags().add("bossHealth");
            Score score = bossObjective.getScore(boss.getCustomName());
            score.setScore((int) boss.getHealth());

            startBossSkills(boss);
        }
    }

    private void startBossSkills(Zombie boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.getHealth() <= 0) {
                    this.cancel();
                    bossHealthMap.remove(boss);
                    return;
                }

                boolean pull = random.nextBoolean();
                Location bossLocation = boss.getLocation();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(boss.getWorld())) {
                        double distance = player.getLocation().distance(bossLocation);
                        if (distance <= 5) {
                            Location playerLocation = player.getLocation();
                            double dx = bossLocation.getX() - playerLocation.getX();
                            double dz = bossLocation.getZ() - playerLocation.getZ();
                            Vector direction = new Vector(dx, 0, dz).normalize();

                            if (pull) {
                                player.setVelocity(direction.multiply(0.5));
                            } else {
                                player.setVelocity(direction.multiply(-0.5));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args){
        if (command.getName().equalsIgnoreCase("createzombie")){
            if (sender instanceof Player){
                Player plr = (Player) sender;

                ItemStack zombieSpawnEgg = new ItemStack(Material.MONSTER_EGG, 1);
                ItemMeta eggMeta = zombieSpawnEgg.getItemMeta();
                eggMeta.setDisplayName("Diamond Zombie Spawn Egg");
                zombieSpawnEgg.setItemMeta(eggMeta);
                plr.getInventory().addItem(zombieSpawnEgg);
                plr.sendMessage(ChatColor.GREEN + "Diamond Zombie Spawn Egg added to your inventory.");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onEggClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getItem() != null && event.getItem().getItemMeta() != null &&
                event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("Diamond Zombie Spawn Egg")) {
            Location spawnLocation = event.getClickedBlock().getLocation().add(0, 1, 0);
            Zombie boss = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);

            boss.setCustomName(ChatColor.RED + "Eblan");
            boss.setCustomNameVisible(true);

            boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            boss.setHealth(100.0);

            bossHealthMap.put(boss, 100.0);

            // Assign scoreboard to display health
            boss.getScoreboardTags().add("bossHealth");
            Score score = bossObjective.getScore(boss.getCustomName());
            score.setScore((int) boss.getHealth());

            startBossSkills(boss);

            if(!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                int amount = itemStack.getAmount();
                if(amount > 1) {
                    itemStack.setAmount(amount - 1);
                    event.getPlayer().getInventory().setItemInMainHand(itemStack);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Zombie)) return;
        Zombie boss = (Zombie) event.getEntity();
        if(!bossHealthMap.containsKey(boss)) return;

        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();

        double damage = event.getFinalDamage();
        double newHealth = boss.getHealth() - damage;
        if(newHealth < 0) newHealth = 0;
        boss.setHealth(newHealth);
        bossHealthMap.put(boss, newHealth);
        Score score = bossObjective.getScore(boss.getCustomName());
        score.setScore((int) newHealth);
        sendActionBar(damager, ChatColor.YELLOW + "Damage: " + (int) damage);

        if(newHealth <= 0){
            bossHealthMap.remove(boss);
            boss.getWorld().playEffect(boss.getLocation(), Effect.ENDER_SIGNAL, 1);
            boss.remove();
        }
    }

    private void sendActionBar(Player player, String message){
        player.sendMessage(ChatColor.RESET.toString() + "\n" + message);
    }

    @Override
    public void onDisable() {
        getLogger().info("TestPlugin2 stopped");
    }
}