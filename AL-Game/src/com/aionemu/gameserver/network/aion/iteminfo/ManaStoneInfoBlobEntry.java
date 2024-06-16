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

package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about mana stones.
 *
 * @author -Nemesiss-
 * @modified Rolandas
 * @modified Alcapwnd
 * @Reworked GiGatR00n
 */
public class ManaStoneInfoBlobEntry extends ItemBlobEntry {

    ManaStoneInfoBlobEntry() {
        super(ItemBlobType.MANA_SOCKETS);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = ownerItem;

        writeC(buf, item.isSoulBound() ? 1 : 0);
        writeC(buf, item.getEnchantLevel()); // enchant (1-15)
        writeD(buf, item.getItemSkinTemplate().getTemplateId());
        writeC(buf, item.getOptionalSocket());
        writeC(buf, 0);//maxEnchant
        writeItemStones(buf);

        ItemStone god = item.getGodStone();
        writeD(buf, god == null ? 0 : god.getItemId());
        int itemColor = item.getItemColor();
        int dyeExpiration = item.getColorTimeLeft();
        // expired dyed items
        if ((dyeExpiration > 0 && item.getColorExpireTime() > 0 || dyeExpiration == 0 && item.getColorExpireTime() == 0) && item.getItemTemplate().isItemDyePermitted()) {
            writeC(buf, itemColor == 0 ? 0 : 1);
            writeD(buf, itemColor);
            writeD(buf, 0); // unk 1.5.1.9
            writeD(buf, dyeExpiration); // seconds until dye expires
        } else {
            writeC(buf, 0);
            writeD(buf, 0);
            writeD(buf, 0); // unk 1.5.1.9
            writeD(buf, 0);
        }

        IdianStone idianStone = item.getIdianStone();
        if (idianStone != null && idianStone.getPolishNumber() > 0) {
            writeD(buf, idianStone.getItemId()); // Idian Stone template ID
            writeC(buf, idianStone.getPolishNumber()); // polish statset ID
        } else {
            writeD(buf, 0); // Idian Stone template ID
            writeC(buf, 0); // polish statset ID
        }
        
        writeC(buf, item.getAuthorize());
        writeH(buf, 0);
        writePlumeStats(buf); // 64-bytes
        writeB(buf, new byte[36]);
        writeAmplification(buf); // 6-bytes
		writeB(buf, new byte[12]);
    }
    
    /**
     * Writes amplification data
     * @param item
     */
    private void writeAmplification(ByteBuffer buf) {
    	Item item = ownerItem;
    	writeC(buf, item.isAmplified() ? 1 : 0);
        writeD(buf, item.getBuffSkill());
    }
    
    /**
     * Writes plume stats
     * @param item
     */
    private void writePlumeStats(ByteBuffer buf) {
    	Item item = ownerItem;
    	if (item.getItemTemplate().isPlume()) {
    		writeD(buf, 0);//unk plume stat
            writeD(buf, 0);//value    		
    		writeD(buf, 0);//unk plume stat
            writeD(buf, 0);//value
            writeD(buf, 42);
            writeD(buf, item.getAuthorize() * 150); //HP Boost for Tempering Solution    
            if (item.getItemTemplate().getAuthorizeName() == 10051) {
            	writeD(buf, 30);
                writeD(buf, item.getAuthorize() * 4);//Physical Attack
                writeD(buf, 0);//New Plume Stat 4.7.5.6 (NcSoft will implement it at future)
                writeD(buf, 0);//it's Value                
			} else if (item.getItemTemplate().getAuthorizeName() == 10052){
				writeD(buf, 35);
				writeD(buf, item.getAuthorize() * 20); //Magic Boost
				writeD(buf, 0);
				writeD(buf, 0);
			} else if (item.getItemTemplate().getAuthorizeName() == 10056){
				writeD(buf, 33);
				writeD(buf, item.getAuthorize() * 12); //Physical Critical
				writeD(buf, 0);
				writeD(buf, 0);
			} else if (item.getItemTemplate().getAuthorizeName() == 10057) {
				writeD(buf, 36);
				writeD(buf, item.getAuthorize() * 8); //Magical Accuracy
				writeD(buf, 0);
				writeD(buf, 0);
			}
            //Some Padding for future.
            writeD(buf, 0);//unk plume stat
            writeD(buf, 0);//value
            writeD(buf, 0);//unk plume stat
            writeD(buf, 0);//value
            writeD(buf, 0);//unk plume stat
            writeD(buf, 0);//value
        } else {
            writeB(buf, new byte[64]);
        }
    }
    /**
     * Writes manastones
     *
     * @param item
     */
    private void writeItemStones(ByteBuffer buf) {
        Item item = ownerItem;
        int count = 0;

        if (item.hasManaStones()) {
            Set<ManaStone> itemStones = item.getItemStones();
            ArrayList<ManaStone> basicStones = new ArrayList<ManaStone>();
            ArrayList<ManaStone> ancientStones = new ArrayList<ManaStone>();

            for (ManaStone itemStone : itemStones) {
                if (itemStone.isBasic()) {
                    basicStones.add(itemStone);
                } else {
                    ancientStones.add(itemStone);
                }
            }

            if (item.getItemTemplate().getSpecialSlots() > 0) {
                if (ancientStones.size() > 0) {
                    for (ManaStone ancientStone : ancientStones) {
                        if (count == 6) {
                            break;
                        }
                        writeD(buf, ancientStone.getItemId());
                        count++;
                    }
                }

                for (int i = count; i < item.getItemTemplate().getSpecialSlots(); i++) {
                    writeD(buf, 0);
                    count++;
                }
            }

            for (ManaStone basicStone : basicStones) {
                if (count == 6) {
                    break;
                }
                writeD(buf, basicStone.getItemId());
                count++;
            }
            skip(buf, (6 - count) * 4);
        } else {
            skip(buf, 24);
        }
    }

    @Override
    public int getSize() {
		//return 12 * 2 + 50 + 32 * 2 + 36; // 4.9
    	return 174; // 4.9
    }
}
