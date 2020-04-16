package me.saiintbrisson.tempmute.mute;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class MuteModel {

    private UUID id;

    @Setter
    private String name;

    @Setter
    private String author;

    @Setter
    private String reason;

    @Setter
    private Timestamp expirationDate;

    @Setter
    private Timestamp muteDate;

    public boolean isActive() {
        return !isTemporary() || expirationDate.after(Timestamp.from(Instant.now()));
    }

    public boolean isTemporary() {
        return expirationDate != null;
    }

    public String parseMessage(String message) {
        String expiration = expirationDate == null ? "never" : expirationDate.toString();
        expiration = expiration
            .replace("-", "/");

        if(expiration.contains(".")) {
            expiration = expiration.substring(0, expiration.indexOf('.'));
        }

        String date = muteDate.toString();
        date = date
            .replace("-", "/");

        if(date.contains(".")) {
            date = date.substring(0, date.indexOf('.'));
        }

        return message.replace("{id}", id.toString())
            .replace("{name}", name)
            .replace("{author}", author)
            .replace("{reason}", reason == null ? "No reason provided" : reason)
            .replace("{expiration}", expiration)
            .replace("{date}", date);
    }

}
