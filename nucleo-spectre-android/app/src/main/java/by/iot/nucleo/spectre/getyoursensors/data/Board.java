package by.iot.nucleo.spectre.getyoursensors.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import by.iot.nucleo.spectre.getyoursensors.R;

public class Board implements Serializable {
    @SerializedName("topic") private String mqttTopic;
    @SerializedName("id") private String boardId;
    @SerializedName("name") private String boardName;
    @SerializedName("board_img_url") private String boardImageUrl;

    public String getMqttTopic()
    {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic)
    {
        this.mqttTopic = mqttTopic;
    }

    public String getBoardId()
    {
        return boardId;
    }

    public void setBoardId(String boardId)
    {
        this.boardId = boardId;
    }

    public String getBoardName()
    {
        return boardName;
    }

    public void setBoardName(String boardName)
    {
        this.boardName = boardName;
    }

    public String getBoardImageUrl() {
        return boardImageUrl;
    }

    public boolean hasBoardImageUrl() {
        return getBoardImageUrl() != null && !getBoardImageUrl().isEmpty();
    }

    public int getDefaultBoardImageId() {
        return R.drawable.mfg_nucleo;
    }

    public void setBoardImageUrl(String boardImageUrl) {
        this.boardImageUrl = boardImageUrl;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [mqttTopic = "+ mqttTopic +", boardId = "+ boardId +", boardName = "+ boardName +"]";
    }
}
