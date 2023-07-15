package org.example.plugin.enemydown.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * EnemyDownのゲームを実行する際のスコア情報を扱うオブジェクト
 *
 */

@Getter
@Setter
public class PlayerScore {
  private String playerName;
  private int score;
  private int gameTime;
}
