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
package com.aionemu.gameserver.services;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerPassportsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATREIAN_PASSPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;


/**
 * @author Alcapwnd
 * @reworked Lyras
 */
public class AtreianPassportService {
    private static final Logger log = LoggerFactory.getLogger(AtreianPassportService.class);
    private Map<Integer, AtreianPassport> cumu = new HashMap<Integer, AtreianPassport>(1);
    private Map<Integer, AtreianPassport> daily = new HashMap<Integer, AtreianPassport>(1);
    private Map<Integer, AtreianPassport> anny = new HashMap<Integer, AtreianPassport>(1);
    public Map<Integer, AtreianPassport> data = new HashMap<Integer, AtreianPassport>(1);

    public Map<Integer, AtreianPassport> getCurrentCumuPassports() {
        Map<Integer, AtreianPassport> passports = new HashMap<Integer, AtreianPassport>();
        for(AtreianPassport atp : cumu.values())
        {
            if(atp.getPeriodStart().isBeforeNow() && atp.getPeriodEnd().isAfterNow()) {
                passports.put(atp.getId(), atp);
            }
        }
        return passports;
    }

    public int getMonthsSinceAscension(Player player)
    {
        Timestamp firstDate = null;
        Iterator<PlayerAccountData> it = player.getPlayerAccount().iterator();
        while(it.hasNext())
        {
            PlayerAccountData data = it.next();
            if(firstDate == null) {
                firstDate = data.getCreationDate();
            }
            else {
                if(data.getCreationDate().before(firstDate)) {
                    firstDate = data.getCreationDate();
                }
            }
        }
        if(firstDate != null)
        {
            Calendar first = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            first.setTimeInMillis(firstDate.getTime());
            now.setTimeInMillis(System.currentTimeMillis());

            int diffYear = now.get(Calendar.YEAR) - first.get(Calendar.YEAR);

            return diffYear * 12 + now.get(Calendar.MONTH) - first.get(Calendar.MONTH);
        } else {
            log.error("FIRST DATE == NULL");
            return 0;
        }
    }

    public Map<Integer, AtreianPassport> getPlayerPassports(int accountId) {
        Map<Integer, AtreianPassport> passports = new HashMap<Integer, AtreianPassport>();
        List<Integer> ids = DAOManager.getDAO(PlayerPassportsDAO.class).getPassports(accountId);
        for(Integer i : ids) {
            passports.put(i, data.get(i));
        }
        return passports;
    }

    /**
     * Checks if laststamp is on the previous day and only gives the stamp if it is after 9 AM
     * @param lastStamp
     * @return boolean
     */
    private boolean checkLastStamp(Timestamp lastStamp)
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lastStamp.getTime());
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        Timestamp nextStamp = new Timestamp(c.getTime().getTime());
        return now.after(nextStamp);
    }

    public void onLogin(Player player) {
        if(player == null)
            return;
        boolean newPassport = false;
        int accountId = player.getPlayerAccount().getId();
        PlayerPassportsDAO dao = DAOManager.getDAO(PlayerPassportsDAO.class);
        Map<Integer, AtreianPassport> currentCumuPassports = getCurrentCumuPassports();
        Map<Integer, AtreianPassport> playerPassports = getPlayerPassports(accountId);

        //ANNIVERSARY
        int annyMonths = getMonthsSinceAscension(player);
        for(int i = 14; i < annyMonths + 14; i++) {
            if(!playerPassports.containsKey(i))
            {
                dao.insertPassport(accountId, anny.get(i).getId(), anny.get(i).getAttendNum(), new Timestamp(System.currentTimeMillis()));
                newPassport = true;
            }
        }

        //Daily
        for(AtreianPassport atp : daily.values()) {
            if (atp.getPeriodStart().isBeforeNow() && atp.getPeriodEnd().isAfterNow()) {
                if(!playerPassports.containsKey(atp.getId())) {
                    dao.insertPassport(accountId, atp.getId(), 1, new Timestamp(System.currentTimeMillis()));
                    newPassport = true;
                }
                else {
                    Timestamp lastStamp = dao.getLastStamp(accountId, atp.getId());
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(lastStamp.getTime());
                    Calendar now = Calendar.getInstance();
                    now.setTimeInMillis(System.currentTimeMillis());
                    if(cal.get(Calendar.DAY_OF_MONTH) + 1 <= now.get(Calendar.DAY_OF_MONTH))
                        dao.updatePassport(accountId, atp.getId(), 1, false);
                }
            }
        }


        if(newPassport)
        {
            playerPassports = getPlayerPassports(accountId);
            newPassport = false;
        }


        for(AtreianPassport atp : currentCumuPassports.values()) {
            if(playerPassports.containsKey(atp.getId())) {
                AtreianPassport atp2 = playerPassports.get(atp.getId());
                if(checkLastStamp(dao.getLastStamp(accountId, atp2.getId()))) {
                    int stamps = dao.getStamps(accountId, atp2.getId());
                    if ((stamps + 1) > atp.getAttendNum())
                        continue;
                    if ((stamps + 1) <= atp.getAttendNum())
                        dao.updatePassport(accountId, atp.getId(), stamps + 1, false);
                }
            } else {
                dao.insertPassport(accountId, atp.getId(), 1, new Timestamp(System.currentTimeMillis()));
                newPassport = true;
            }
        }

        for(AtreianPassport atp : playerPassports.values()) {
            if(atp.getPeriodEnd().isBeforeNow() && dao.isRewarded(accountId, atp.getId())) {
                dao.deletePassport(accountId, atp.getId());
            }
            atp.setStamps(dao.getStamps(accountId, atp.getId()));
            atp.setLastStamp(dao.getLastStamp(accountId, atp.getId()));
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(player.getPlayerAccount().getPlayerAccountData(player.getCommonData().getPlayerObjId()).getCreationDate().getTime());
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        if (newPassport) {
            PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NEW_PASSPORT_AVAIBLE);
        }
        PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(playerPassports, month, year));
    }

    public void onStart() {
        Map<Integer, AtreianPassport> raw = DataManager.ATREIAN_PASSPORT_DATA.getAll();
        if (raw.size() != 0) {
            getPassports(raw);
        } else {
            log.warn("[AtreianPassportService] passports from static data = 0");
        }
        log.info("[AtreianPassportService] AtreianPassportService initialized");
    }

    /**
     * @param count
     * @param timestamp
     */
    public void onGetReward(Player player, int timestamp, List<Integer> passportId) {
        int accountId = player.getPlayerAccount().getId();
        Map<Integer, AtreianPassport> playerPassports = getPlayerPassports(accountId);
    	for (Integer i : passportId) {
            if(playerPassports.containsKey(i)
                    && DAOManager.getDAO(PlayerPassportsDAO.class).getStamps(accountId, playerPassports.get(i).getId()) == playerPassports.get(i).getAttendNum()
                    && !DAOManager.getDAO(PlayerPassportsDAO.class).isRewarded(accountId, playerPassports.get(i).getId())) {
                ItemService.addItem(player, playerPassports.get(i).getRewardItem(), playerPassports.get(i).getRewardItemNum(), new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_PASSPORT_ADD));
                DAOManager.getDAO(PlayerPassportsDAO.class).updatePassport(accountId, playerPassports.get(i).getId(), playerPassports.get(i).getStamps(), true);
            }
    	}
        onLogin(player);
    }

    public void getPassports(Map<Integer, AtreianPassport> raw) {
        data.putAll(raw);
        for (AtreianPassport atp : data.values()) {
            switch (atp.getAttendType()) {
                case DAILY:
                    getDailyPassports(atp.getId(), atp);
                    break;
                case CUMULATIVE:
                    getCumulativePassports(atp.getId(), atp);
                    break;
                case ANNIVERSARY:
                    getAnniversaryPassports(atp.getId(), atp);
                    break;
            }
        }
        log.info("[AtreianPassportService] Loaded " + daily.size() + " daily passports");
        log.info("[AtreianPassportService] Loaded " + cumu.size() + " cumulative passports");
        log.info("[AtreianPassportService] Loaded " + anny.size() + " anniversary passports");
    }

    public void getDailyPassports(int id, AtreianPassport atp) {
        if (daily.containsValue(id))
            return;
        daily.put(id, atp);
    }

    public void getCumulativePassports(int id, AtreianPassport atp) {
        if (cumu.containsValue(id))
            return;
        cumu.put(id, atp);
    }

    public void getAnniversaryPassports(int id, AtreianPassport atp) {
        if (anny.containsValue(id))
            return;
        anny.put(id, atp);
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final AtreianPassportService instance = new AtreianPassportService();
    }

    public static AtreianPassportService getInstance() {
        return SingletonHolder.instance;
    }
}