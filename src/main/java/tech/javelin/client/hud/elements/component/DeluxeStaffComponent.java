package tech.javelin.client.hud.elements.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

/**
 * Модераторы в стиле Deluxe: хедер Staffs, ряды с головой игрока,
 * префиксом-рангом, ником и чипом времени на сервере.
 */
public class DeluxeStaffComponent extends DraggableHudElement {
   public static final Pattern STAFF_PREFIX_PATTERN = Pattern.compile(
         ".*(mod|der|adm|help|wne|мод|хелп|помо|адм|владе|отри|таф|taf|curat|курато|dev|раз|supp|сапп|yt|ютуб).*");

   private final Map<String, Row> rowsByName = new LinkedHashMap<>();
   private final Map<String, Long> firstSeenMillis = new HashMap<>();
   private final Map<String, Identifier> skinCache = new HashMap<>();
   private final Set<String> presentStaffNames = new HashSet<>();
   private final Animation panelAlpha = new Animation(250L, Easing.QUARTIC_OUT);
   private long lastRefreshMillis;

   public DeluxeStaffComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      long now = System.currentTimeMillis();
      if (now - this.lastRefreshMillis >= 250L) {
         this.lastRefreshMillis = now;
         this.refreshStaffEntries();
      }

      List<Row> visible = new ArrayList<>();
      for (Row row : this.rowsByName.values()) {
         if (row.active || row.animation.getValue() > 0.003F) {
            visible.add(row);
         }
      }
      this.panelAlpha.update(!visible.isEmpty() || mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen ? 1.0F : 0.0F);
      float alpha = this.panelAlpha.getValue();
      if (alpha <= 0.003F) {
         return;
      }

      float timeColumn = 0.0F;
      float nameColumn = 0.0F;
      float height = DeluxeHudKit.HEADER_HEIGHT;
      for (Row row : visible) {
         timeColumn = Math.max(timeColumn, DeluxeHudKit.iconChipWidth(6.0F, Fonts.SEMIBOLD.getWidth(row.elapsedText, 6.0F)));
         nameColumn = Math.max(nameColumn, 15.0F + prefixWidth(row) + Fonts.SEMIBOLD.getWidth(row.name, 6.0F) + 4.0F);
         height += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }
      float width = Math.max(DeluxeHudKit.headerWidth("Staffs"), timeColumn + (timeColumn > 0.0F && nameColumn > 0.0F ? 2.0F : 0.0F) + nameColumn);

      float x = this.getX();
      float y = this.getY();
      DeluxeHudKit.drawHeader(ctx, x, y, width, alpha, tech.javelin.Javelin.id("icons/hud/hud_staff.png"), "Staffs");

      float rowY = y + DeluxeHudKit.HEADER_TO_ROWS;
      for (Row row : visible) {
         float rowAlpha = row.animation.getValue() * alpha;
         if (rowAlpha > 0.003F) {
            float offset = DeluxeHudKit.openingOffset(rowAlpha);
            float nameX = x - offset;
            float timeX = x + nameColumn + 2.0F - offset;

            DeluxeHudKit.drawPanel(ctx.getMatrices(), nameX, rowY, nameColumn, DeluxeHudKit.ROW_HEIGHT, rowAlpha);
            Identifier skin = this.resolveSkin(row.name);
            DrawUtil.drawPlayerHeadWithRoundedShader(ctx.getMatrices(), skin, nameX + 4.0F, rowY + 1.5F, 8.0F, BorderRadius.all(3.5F),
                  ColorRGBA.WHITE.withAlpha(rowAlpha));
            float textY = rowY + 2.85F;
            float nameStart = nameX + 15.0F;
            if (!row.rankText.isEmpty()) {
               ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), row.rankText, nameStart, textY, DeluxeHudKit.secondaryText(rowAlpha));
            }
            ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), row.name, nameStart + prefixWidth(row), textY, DeluxeHudKit.primaryText(rowAlpha));

            float timeChipWidth = DeluxeHudKit.iconChipWidth(6.0F, Fonts.SEMIBOLD.getWidth(row.elapsedText, 6.0F));
            boolean spectator = row.gameMode == GameMode.SPECTATOR;
            DeluxeHudKit.drawIconChip(ctx, timeX, rowY, timeChipWidth, rowAlpha, 6.0F, (c, left, centerY, a) -> {
               float dotX = left + 3.0F;
               DrawUtil.drawRoundedRect(c.getMatrices(), dotX, centerY - 1.25F, 2.5F, 2.5F, BorderRadius.all(1.25F),
                     (spectator ? new ColorRGBA(255, 200, 60, 255) : new ColorRGBA(70, 255, 120, 255)).withAlpha(a));
            }, row.elapsedText);
         }
         rowY += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }

      this.width = width;
      this.height = Math.max(height, DeluxeHudKit.HEADER_HEIGHT);
   }

   private static float prefixWidth(Row row) {
      return row.rankText.isEmpty() ? 0.0F : Fonts.SEMIBOLD.getWidth(row.rankText, 6.0F) + 3.0F;
   }

   private Identifier resolveSkin(String name) {
      Identifier cached = this.skinCache.get(name);
      if (cached != null) {
         return cached;
      }
      Identifier skin = DefaultSkinHelper.getSteve().texture();
      if (mc.getNetworkHandler() != null) {
         for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile() != null && name.equals(entry.getProfile().getName())) {
               skin = entry.getSkinTextures().texture();
               break;
            }
         }
      }
      this.skinCache.put(name, skin);
      return skin;
   }

   private void refreshStaffEntries() {
      for (Row row : this.rowsByName.values()) {
         row.active = false;
      }
      this.presentStaffNames.clear();

      if (mc.getNetworkHandler() != null && mc.world != null) {
         for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile() != null ? entry.getProfile().getName() : null;
            if (name == null || name.isEmpty()) {
               continue;
            }
            Team team = mc.world.getScoreboard().getScoreHolderTeam(name);
            String teamPrefix = team == null ? "" : team.getPrefix().getString().trim();
            String rankText = !teamPrefix.isEmpty() ? teamPrefix
                    : (entry.getDisplayName() == null ? "" : entry.getDisplayName().getString().replace(name, "").trim());
            boolean isStaff = !rankText.isEmpty()
                    && STAFF_PREFIX_PATTERN.matcher(rankText.toLowerCase(Locale.ROOT)).matches();
            if (!isStaff) {
               continue;
            }

            this.presentStaffNames.add(name);
            long joinedAt = this.firstSeenMillis.computeIfAbsent(name, n -> System.currentTimeMillis());
            Row row = this.rowsByName.computeIfAbsent(name, Row::new);
            row.rankText = rankText.isEmpty() ? "Staff" : rankText;
            long elapsed = System.currentTimeMillis() - joinedAt;
            row.elapsedText = String.format(Locale.US, "%02d:%02d", elapsed / 60000L, elapsed / 1000L % 60L);
            row.gameMode = entry.getGameMode();
            row.active = true;
            this.skinCache.remove(name);
         }
         this.firstSeenMillis.keySet().retainAll(this.presentStaffNames);
      } else {
         this.firstSeenMillis.clear();
      }

      this.rowsByName.values().removeIf(row -> !row.active && row.animation.getValue() == 0.0F);
      for (Row row : this.rowsByName.values()) {
         row.animation.update(row.active ? 1.0F : 0.0F);
      }
   }

   private static final class Row {
      final String name;
      final Animation animation = new Animation(600L, Easing.QUARTIC_OUT);
      String rankText = "";
      String elapsedText = "";
      GameMode gameMode = GameMode.SURVIVAL;
      boolean active = true;

      Row(String name) {
         this.name = name;
      }
   }
}
