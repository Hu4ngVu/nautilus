package vn.io.huangnosimp.nautilusserver.events;

public record UserRegisteredEvent(String authUserId, String username) {}
