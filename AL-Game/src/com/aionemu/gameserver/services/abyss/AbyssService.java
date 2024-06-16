/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author ATracer
 */
public class AbyssService {

    // List Maps - Inggison, Cygnea, Gelkmaros, Enshar, Reshanta, Belus, Aspida, Atanatos, Disillon, Idian Dephts, Kaldor, Levinshor
    private static final int[] abyssMapList = {210050000, 210070000, 220070000, 220080000, 400010000, 400020000, 400040000, 400050000, 400060000, 600070000, 600090000, 600100000};

    /**
     * @param player
     */
    public static final boolean isOnPvpMap(Player player) {
        for (int i : abyssMapList) {
            if (i == player.getWorldId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param victim
     */
    public static final void rankedKillAnnounce(final Player victim) {

        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                if (p != victim && victim.getWorldType() == p.getWorldType() && !p.isInInstance()) {
                    PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(victim, AbyssRankEnum.getRankDescriptionId(victim)));
                }
            }
        });
    }

    public static final void rankerSkillAnnounce(final Player player, final int nameId) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                if (p != player && player.getWorldType() == p.getWorldType() && !p.isInInstance()) {
                    PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_SKILL_ABYSS_SKILL_IS_FIRED(player, new DescriptionId(nameId)));
                }
            }
        });
    }
}
