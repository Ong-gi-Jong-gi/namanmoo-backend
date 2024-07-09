package ongjong.namanmoo.domain;

import lombok.Getter;

@Getter
public enum ChallengeLength {

    FIFTEEN_DAYS(15, "15일"),
    THIRTY_DAYS(30, "30일");

    private final int days;
    private final String description;

    ChallengeLength(int days, String description) {
        this.days = days;
        this.description = description;
    }
}