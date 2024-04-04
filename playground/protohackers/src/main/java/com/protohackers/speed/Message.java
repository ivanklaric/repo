package com.protohackers.speed;

import java.util.List;

public class Message {
    public enum MessageType {
        ERROR((char)0x10),
        PLATE((char)0x20),
        TICKET((char)0x21),
        WANT_HEARTBEAT((char)0x40),
        HEARTBEAT((char)0x41),
        I_AM_CAMERA((char)0x80),
        I_AM_DISPATCHER((char)0x81),
        INVALID((char)0x00);


        public final char typeCode;
        MessageType(char typeCode) {
            this.typeCode = typeCode;
        }
    }

    private MessageType type;
    private String errorMessage;
    private String plate;
    private long timestamp;
    private long road;
    private long mile1, mile2;
    private long timestamp1, timestamp2;
    private long speed;
    private long interval;
    private long mile;
    private long limit;
    private List<Long> dispatcherRoads;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRoad() {
        return road;
    }

    public void setRoad(long road) {
        this.road = road;
    }

    public long getMile1() {
        return mile1;
    }

    public void setMile1(long mile1) {
        this.mile1 = mile1;
    }

    public long getMile2() {
        return mile2;
    }

    public void setMile2(long mile) {
        this.mile2 = mile;
    }

    public long getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(long timestamp1) {
        this.timestamp1 = timestamp1;
    }

    public long getTimestamp2() {
        return timestamp2;
    }

    public void setTimestamp2(long timestamp2) {
        this.timestamp2 = timestamp2;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getMile() {
        return mile;
    }

    public void setMile(long mile) {
        this.mile = mile;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public List<Long> getDispatcherRoads() {
        return dispatcherRoads;
    }

    public void setDispatcherRoads(List<Long> dispatcherRoads) {
        this.dispatcherRoads = dispatcherRoads;
    }
}
