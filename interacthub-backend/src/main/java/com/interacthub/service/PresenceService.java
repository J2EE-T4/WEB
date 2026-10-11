package com.interacthub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PresenceService {

    public boolean isUserOnline(String userId) {
        return false;
    }

    public void setUserOnline(String userId, String connectionId) {
        log.debug("User {} online with conn {}", userId, connectionId);
    }

    public void setUserOffline(String userId, String connectionId) {
        log.debug("User {} offline with conn {}", userId, connectionId);
    }
}
