package de.donxs.pinghandler;

import lombok.Data;


@Data
public class PingResponse {

    private String plainjson;

    private Version version;
    private Players players;
    private String description;
    private Modinfo modinfo;
    private String favicon;
    private int time;

    @Data
    public static class Version {

        private String name;
        private int protocol;

    }

    @Data
    public static class Players {

        private String max;
        private String online;
        private Player[] players;

    }

    @Data
    public static class Player {

        private String name;
        private String id;

    }

    @Data
    public static class Modinfo {

        private String type;
        private String[] modList;

    }

}
