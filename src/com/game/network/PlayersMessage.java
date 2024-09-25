package com.game.network;

import com.game.state.Player;

import java.util.Collection;

public class PlayersMessage {
    private String type = "PLAYER_LIST";

    private Collection<Player> players;

    public PlayersMessage(Collection<Player> players) {
        this.players = players;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Collection<Player> players) {
        this.players = players;
    }
}
