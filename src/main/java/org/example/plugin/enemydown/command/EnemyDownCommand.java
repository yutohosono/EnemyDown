package org.example.plugin.enemydown.command;

import java.net.http.WebSocket.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.example.plugin.enemydown.EnemyDown;
import org.example.plugin.enemydown.data.PlayerScore;

public class EnemyDownCommand implements CommandExecutor, Listener, org.bukkit.event.Listener {


  private final EnemyDown enemyDown;
  private List<PlayerScore> playerScoreList = new ArrayList<>();

  public EnemyDownCommand(EnemyDown enemyDown) {
    this.enemyDown =enemyDown;
  }


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender instanceof Player player) {
      PlayerScore nowPlayer = getPlayerScore(player);
      nowPlayer.setGameTime(20);

      World world = player.getWorld();

      initPlayerStatus(player);

      Bukkit.getScheduler().runTaskTimer(enemyDown, Runnable -> {
        if(nowPlayer.getGameTime() <= 0) {
          Runnable.cancel();
          player.sendTitle("ゲームが終了しました。",
              nowPlayer.getPlayerName() + "合計 " + nowPlayer.getScore() + "点",
              0,30,0);
          nowPlayer.setScore(0);
          return;
        }
        player.getWorld().spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
        nowPlayer.setGameTime(nowPlayer.getGameTime() - 5);
      },0,5 * 20);

    }
    return false;
  }

  /**
   * 現在実行しているプレイヤーのプレイスコア情報を取得
   *
   * @param player
   * @return
   */
  private PlayerScore getPlayerScore(Player player) {
    if(playerScoreList.isEmpty()) {
      return addNewPlayer(player);
    } else {
      for (PlayerScore playerScore : playerScoreList) {
        if (!playerScore.getPlayerName().equals(player.getName())) {
          return addNewPlayer(player);
        } else {
          return playerScore;
        }
      }
    }
    return null;
  }

  /**
   * 新規のプレイヤー情報をリストに追加します
   * @param player　コマンドを実行したプレイヤー
   * @return 新規プレイヤー
   */
  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    Player player = e.getEntity().getKiller();
    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
        playerScore.setScore(playerScore.getScore() + 10);
        player.sendMessage("敵を倒した！現在のスコアは" + playerScore.getScore() + "点");
      }
    }
  }

  private void initPlayerStatus(Player player) {
    player.setFoodLevel(20);
    player.setHealth(20);
    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
  }


  /**
   * 敵の出現エリアを取得
   * 出現エリアはX軸とZ軸は自分の位置からプラス、ランダムで−10〜9
   * Y軸はプレイヤーと同じ位置になります
   *
   * @param player　コマンドを実行したプレイヤー
   * @param world　コマンドを実行したプレイヤーが所属するワールド
   * @return　敵の出現場所
   */
  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y, z);
  }

  /**
   * 敵をランダムに取得します
   *
   * @return　敵
   */
  private EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE,EntityType.SKELETON);
    return enemyList.get(new SplittableRandom().nextInt(2));
  }
}
