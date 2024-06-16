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
package quest.reshanta;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author pralinka
 */
public class _14045RumorsOnWings extends QuestHandler {

    private final static int questId = 14045;
    private boolean killed = false;

    public _14045RumorsOnWings() {
        super(questId);
    }

    @Override
    public void register() {
        int[] npcs = {278506, 279023, 279006};
        qe.registerOnEnterZoneMissionEnd(questId);
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(214102).addOnKillEvent(questId);
        for (int npc : npcs) {
            qe.registerQuestNpc(npc).addOnTalkEvent(questId);
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) {
            return false;
        }
        int var = qs.getQuestVarById(0);
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (qs.getStatus() == QuestStatus.START) {
            switch (targetId) {
                case 278506: {
                    switch (dialog) {
                        case QUEST_SELECT: {
                            if (var == 0) {
                                return sendQuestDialog(env, 1011);
                            }
                        }
                        case SELECT_ACTION_1013: {
                            playQuestMovie(env, 272);
                            break;
                        }
                        case SETPRO1: {
                            return defaultCloseDialog(env, 0, 1);
                        }
					default:
						break;
                    }
                    break;
                }
                case 279023: {
                    switch (dialog) {
                        case QUEST_SELECT: {
                            if (var == 1) {
                                return sendQuestDialog(env, 1352);
                            }
                        }
                        case SETPRO2: {
							giveQuestItem(env, 182215918, 1);
                            return defaultCloseDialog(env, 1, 2);
                        }
					default:
						break;
                    }
                    break;
                }
                case 279006: {
                    switch (dialog) {
                        case QUEST_SELECT: {
                            if (var == 2) {
                                return sendQuestDialog(env, 1693);
                            } else if (var == 3) {
                                if (killed) {
                                    return sendQuestDialog(env, 2034);
                                }
                            }
                        }
						case SELECT_ACTION_1694: {
							removeQuestItem(env, 182215918, 1);
							return sendQuestDialog(env, 1694);
						}
                        case SETPRO3: {
                            if (var == 2) {
                                qs.setQuestVarById(0, 12);
                                qs.setStatus(QuestStatus.REWARD);
                                updateQuestStatus(env);
                                return sendQuestSelectionDialog(env);
                            }
                        }
					default:
						break;
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 279023) {
                if (dialog == DialogAction.USE_OBJECT) {
                    return sendQuestDialog(env, 10002);
                } else {
                    return sendQuestEndDialog(env);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onZoneMissionEndEvent(QuestEnv env) {
        return defaultOnZoneMissionEndEvent(env, 14040);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        int[] quests = {14041, 14040};
        return defaultOnLvlUpEvent(env, quests, true);
    }
}
