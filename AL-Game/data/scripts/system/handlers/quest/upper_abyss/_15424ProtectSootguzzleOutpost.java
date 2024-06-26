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
package quest.upper_abyss;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Phantom_KNA
 */
public class _15424ProtectSootguzzleOutpost extends QuestHandler {

	private final static int questId = 15424;

	public _15424ProtectSootguzzleOutpost() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(805395).addOnQuestStart(questId);
		qe.registerQuestNpc(805395).addOnTalkEvent(questId);
		qe.registerQuestNpc(805395).addOnTalkEvent(questId); 
		qe.registerQuestNpc(883453).addOnKillEvent(questId);
		qe.registerQuestNpc(883454).addOnKillEvent(questId);
		qe.registerQuestNpc(883455).addOnKillEvent(questId);
		qe.registerQuestNpc(883456).addOnKillEvent(questId);
		qe.registerQuestNpc(883457).addOnKillEvent(questId);
	}

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);

        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if (targetId == 805395) {
                if (env.getDialog() == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 4762);
                } else {
                    return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 805395) {
                if (env.getDialog() == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 10002);
                } else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD) {
                    changeQuestStep(env, 0, 0, true);
                    return sendQuestDialog(env, 5);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 805395) {
                return sendQuestEndDialog(env);
            }
        } 
        return false;
    }

   @Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = env.getTargetId();

		switch(targetId){
				case 883453:
				case 883454:
				case 883455:
				case 883456:
				case 883457:
					if (qs.getQuestVarById(1) != 4){
						qs.setQuestVarById(1, qs.getQuestVarById(1) + 1);
						updateQuestStatus(env);
					}
					else {
						qs.setQuestVarById(0, 5);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
					}
			}
		return false;
	}
}
