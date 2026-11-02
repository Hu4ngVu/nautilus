package vn.io.huangnosimp.nautilusserver.events;

public record UsernameUpdatedEvent(String authUserId, String newUsername) {}
