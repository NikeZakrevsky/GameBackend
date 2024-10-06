package com.game.network;

import com.game.state.Tree;

import java.util.List;

public class MapMessage {
    private final String type = "MAP";

    private List<Tree> trees;

    public MapMessage(List<Tree> trees) {
        this.trees = trees;
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public void setTrees(List<Tree> trees) {
        this.trees = trees;
    }

    public String getType() {
        return type;
    }
}
