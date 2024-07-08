package org.tomek.engine.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.tomek.engine.enums.QuestState;

import java.util.List;

/**
 * This class represents a quest in the game.
 * A quest has dialogs, a quest item, a state, and a reward.
 */
@Getter
@Setter
public class Quest {

    private List<String> dialogs;

    private Item questItem;
    @Setter
    private int dialogIndex;
    @Setter
    private QuestState state;

    private int indexOfChange;

    private boolean sideQuest;

    private Item reward;

    @JsonIgnore
    public String getCurrentDialog(){
        return dialogs.get(dialogIndex);
    }

    @JsonCreator
    public Quest(@JsonProperty("questItem")Item questItem,
                 @JsonProperty("Dialogs")List<String> dialogs,
                 @JsonProperty("indexOfChange")int indexOfChange,
                 @JsonProperty("dialogIndex")int dialogIndex,
                 @JsonProperty("questState")QuestState state,
                 @JsonProperty("sideQuest")boolean sideQuest,
                 @JsonProperty("reward")Item reward){
        this.dialogIndex = dialogIndex;
        this.state = state;
        this.dialogs = dialogs;
        this.questItem = questItem;
        this.indexOfChange = indexOfChange;
        this.sideQuest = sideQuest;
        this.reward = reward;
    }
}
